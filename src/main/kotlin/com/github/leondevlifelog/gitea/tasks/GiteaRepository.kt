/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.tasks

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.util.text.StringUtil
import com.intellij.tasks.Task
import com.intellij.tasks.TaskRepositoryType
import com.intellij.tasks.impl.BaseRepository
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl
import com.intellij.util.xmlb.annotations.Tag
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.gitnex.tea4j.v2.models.Issue

/**
 * Gitea Task Repo
 * @author Leon
 */
@Tag("Gitea")
class GiteaRepository : NewBaseRepositoryImpl {

    var repoAuthor: String? = null

    var repoName: String? = null

    var assigned = false

    companion object {
        private val GSON: Gson = TaskGsonUtil.createDefaultBuilder().create()
        private val LIST_OF_ISSUES_TYPE: TypeToken<List<Issue>> = object : TypeToken<List<Issue>>() {}
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
            HttpGet(),
            TaskResponseUtil.GsonSingleObjectDeserializer<Issue>(GSON, Issue::class.java)
        )
        return GiteaTask(this, issue)
    }

    override fun createCancellableConnection(): CancellableConnection? {
        return HttpTestConnection(HttpGet(getRestApiUrl("repos", "$repoAuthor", "$repoName", "issues")))
    }

    override fun getIssues(query: String?, offset: Int, limit: Int, withClosed: Boolean): Array<Task> {
        val uri = URIBuilder(getRestApiUrl("repos", "$repoAuthor", "$repoName", "issues"))
            .addParameter("q", query)
            .addParameter("page", ((offset / limit) + 1).toString())
            .addParameter("limit", limit.toString())
            .build()
        val httpGet = HttpGet(uri)
        val issues = httpClient.execute(
            httpGet,
            TaskResponseUtil.GsonMultipleObjectsDeserializer<Issue>(GSON, LIST_OF_ISSUES_TYPE)
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
        return super.isConfigured() && StringUtil.isNotEmpty(password)
                && StringUtil.isNotEmpty(repoAuthor)
                && StringUtil.isNotEmpty(repoName)
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

}