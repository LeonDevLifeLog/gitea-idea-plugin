package com.github.leondevlifelog.gitea.tasks

import com.intellij.tasks.Task
import com.intellij.tasks.TaskRepositoryType
import com.intellij.tasks.impl.BaseRepository
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl
import com.intellij.util.xmlb.annotations.Tag
import org.gitnex.tea4j.v2.ApiClient
import org.gitnex.tea4j.v2.apis.IssueApi

@Tag("Gitea")
class GiteaRepository : NewBaseRepositoryImpl {
    var userId: String? = null

    var userLogin: String? = null

    var repoName: String? = null

    var projName: String? = null

    var token: String? = null

    var assigned = false


    constructor(type: TaskRepositoryType<GiteaRepository>?) : super(type)
    constructor(other: GiteaRepository) : super(other) {
        userId = other.userId
        userLogin = other.userLogin
        repoName = other.repoName
        projName = other.projName
        token = other.token
        assigned = other.assigned
    }

    override fun findTask(id: String): Task? {
        val createService = ApiClient().createService(IssueApi::class.java)
        val issueGetIssue = createService.issueGetIssue(userId, repoName, id.toLong()).execute().body() ?: return null
        return GiteaTask(this, issueGetIssue)
    }

    override fun clone(): BaseRepository {
        return GiteaRepository(this)
    }

}