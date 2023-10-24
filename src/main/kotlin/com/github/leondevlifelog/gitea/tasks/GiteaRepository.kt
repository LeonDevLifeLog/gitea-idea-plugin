/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.tasks

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.tasks.CustomTaskState
import com.intellij.tasks.LocalTask
import com.intellij.tasks.Task
import com.intellij.tasks.TaskRepositoryType
import com.intellij.tasks.gitlab.GitlabRepository
import com.intellij.tasks.impl.BaseRepository
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl
import com.intellij.util.xmlb.annotations.Tag
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.gitnex.tea4j.v2.models.AddTimeOption
import org.gitnex.tea4j.v2.models.EditIssueOption
import org.gitnex.tea4j.v2.models.Issue
import org.gitnex.tea4j.v2.models.TrackedTime
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Gitea Task Repo
 * @author Leon
 */
@Tag("Gitea")
class GiteaRepository : NewBaseRepositoryImpl {

    var repoAuthor: String? = null

    var repoName: String? = null

    var assigned = false

    var skipSSLVerify = false


    companion object {
        private val GSON: Gson = TaskGsonUtil.createDefaultBuilder().create()
        private val LIST_OF_ISSUES_TYPE: TypeToken<List<Issue>> = object : TypeToken<List<Issue>>() {}
        private val LOG = Logger.getInstance(GitlabRepository::class.java)
    }

    constructor() : super() {
    }

    constructor(type: TaskRepositoryType<GiteaRepository>?) : super(type)
    constructor(other: GiteaRepository) : super(other) {
        repoAuthor = other.repoAuthor
        repoName = other.repoName
        assigned = other.assigned
    }

    override fun findTask(id: String): Task? {
        val issue = httpClient.execute(
            HttpGet(), TaskResponseUtil.GsonSingleObjectDeserializer<Issue>(GSON, Issue::class.java)
        )
        return GiteaTask(this, issue)
    }

    override fun createCancellableConnection(): CancellableConnection? {
        val issueListApi = getRestApiUrl("repos", "$repoAuthor", "$repoName", "issues")
        val url = URIBuilder(issueListApi).addParameter("access_token", password).build()
        val httpGet = HttpGet(url)
        return HttpTestConnection(httpGet)
    }

    override fun getIssues(query: String?, offset: Int, limit: Int, withClosed: Boolean): Array<Task> {
        val uri = URIBuilder(getRestApiUrl("repos", "$repoAuthor", "$repoName", "issues")).addParameter("q", query)
            .addParameter("assigned", assigned.toString()).addParameter("page", ((offset / limit) + 1).toString())
            .addParameter("access_token", password).addParameter("limit", limit.toString()).build()
        val httpGet = HttpGet(uri)
        val issues = httpClient.execute(
            httpGet, TaskResponseUtil.GsonMultipleObjectsDeserializer<Issue>(GSON, LIST_OF_ISSUES_TYPE)
        )
        return issues.map { GiteaTask(this, it) }.toTypedArray()
    }

    override fun clone(): BaseRepository {
        return GiteaRepository(this)
    }

    override fun getRestApiPathPrefix(): String {
        return "/api/v1/"
    }

    override fun isConfigured(): Boolean {
        return super.isConfigured() && StringUtil.isNotEmpty(password) && StringUtil.isNotEmpty(repoAuthor) && StringUtil.isNotEmpty(
            repoName
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as GiteaRepository

        if (repoAuthor != other.repoAuthor) return false
        if (repoName != other.repoName) return false
        return assigned == other.assigned
    }

    override fun hashCode(): Int {
        var result = repoAuthor?.hashCode() ?: 0
        result = 31 * result + (repoName?.hashCode() ?: 0)
        result = 31 * result + assigned.hashCode()
        return result
    }

    override fun updateTimeSpent(task: LocalTask, timeSpent: String, comment: String) {
        val issueTrackedTimeUrl = getRestApiUrl("repos", "$repoAuthor", "$repoName", "issues", task.id, "times")
        val url = URIBuilder(issueTrackedTimeUrl).addParameter("access_token", password).build()
        val httpPost = HttpPost(url)
        val addTimeOption = AddTimeOption()
        addTimeOption.created = Date()
        addTimeOption.userName = username
        val matcher = TIME_SPENT_PATTERN.matcher(timeSpent)
        if (!matcher.find()) {
            LOG.warn("can not get the time spent")
            return
        }
        val hour = matcher.group(0).toLong()
        val minute = matcher.group(1).toLong()
        val spendTimeInSecond = TimeUnit.HOURS.toSeconds(hour) + TimeUnit.MINUTES.toSeconds(minute)
        addTimeOption.time = spendTimeInSecond
        LOG.debug("the time spend for issue:${task} is ${spendTimeInSecond}s")
        httpPost.entity = StringEntity(GSON.toJson(addTimeOption), ContentType.APPLICATION_JSON)
        val trackedTime: TrackedTime? =
            httpClient.execute(httpPost, TaskResponseUtil.GsonSingleObjectDeserializer(GSON, TrackedTime::class.java))
        LOG.debug("response is $trackedTime")
        trackedTime ?: return
    }

    override fun setTaskState(task: Task, state: CustomTaskState) {
        val updateTaskState = getRestApiUrl("repos", "$repoAuthor", "$repoName", "issues", task.id)
        val url = URIBuilder(updateTaskState).addParameter("access_token", password).build()
        val httpPatch = HttpPatch(url)
        val editIssueOption = EditIssueOption()
        editIssueOption.state = state.id
        httpPatch.entity = StringEntity(GSON.toJson(editIssueOption), ContentType.APPLICATION_JSON)
        val issue =
            httpClient.execute(httpPatch, TaskResponseUtil.GsonSingleObjectDeserializer(GSON, Issue::class.java))
        issue ?: return
    }

    override fun getAvailableTaskStates(task: Task): MutableSet<CustomTaskState> {
        return mutableSetOf(CustomTaskState("open", "Open"), CustomTaskState("closed", "Close"))
    }

    override fun getFeatures(): Int {
        return super.getFeatures() or TIME_MANAGEMENT or STATE_UPDATING
    }

    override fun isUseHttpAuthentication(): Boolean {
        return true
    }
}
