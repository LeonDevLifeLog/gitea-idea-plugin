/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.extensions

import com.github.leondevlifelog.gitea.GiteaConfig
import com.intellij.openapi.project.Project
import git4idea.remote.GitRepositoryHostingService
import git4idea.remote.InteractiveGitHttpAuthDataProvider
import kotlinx.coroutines.runBlocking

class GiteaRepositoryHostingService : GitRepositoryHostingService() {
    override fun getServiceDisplayName(): String {
        return GiteaConfig.SERVICE_DISPLAY_NAME
    }

    override fun getInteractiveAuthDataProvider(project: Project, url: String): InteractiveGitHttpAuthDataProvider? {
        return runBlocking {
            GiteaHttpAuthDataProvider.getAccountsWithTokens(project, url).takeIf { it.isNotEmpty() }?.let {
                GiteaSelectAccountHttpAuthDataProvider(project, it)
            }
        }
    }

    override fun getInteractiveAuthDataProvider(
        project: Project, url: String, login: String
    ): InteractiveGitHttpAuthDataProvider? {
        return runBlocking {
            GiteaHttpAuthDataProvider.getAccountsWithTokens(project, url).mapNotNull { (acc, token) ->
                if (token == null) return@mapNotNull null
                val details = GiteaHttpAuthDataProvider.getAccountDetails(acc, token) ?: return@mapNotNull null
                if (details.name != login) return@mapNotNull null
                acc to token
            }.takeIf { it.isNotEmpty() }?.let {
                GiteaSelectAccountHttpAuthDataProvider(project, it.toMap())
            }
        }
    }

}