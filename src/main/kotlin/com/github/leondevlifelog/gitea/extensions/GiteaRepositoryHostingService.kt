package com.github.leondevlifelog.gitea.extensions

import com.github.leondevlifelog.gitea.GiteaConfig
import com.intellij.openapi.project.Project
import git4idea.remote.GitRepositoryHostingService
import git4idea.remote.InteractiveGitHttpAuthDataProvider

class GiteaRepositoryHostingService : GitRepositoryHostingService() {
    override fun getServiceDisplayName(): String {
        return GiteaConfig.SERVICE_DISPLAY_NAME
    }

    override fun getInteractiveAuthDataProvider(project: Project, url: String): InteractiveGitHttpAuthDataProvider? {
        return super.getInteractiveAuthDataProvider(project, url)
    }

    override fun getInteractiveAuthDataProvider(
        project: Project,
        url: String,
        login: String
    ): InteractiveGitHttpAuthDataProvider? {
        return super.getInteractiveAuthDataProvider(project, url, login)
    }
}