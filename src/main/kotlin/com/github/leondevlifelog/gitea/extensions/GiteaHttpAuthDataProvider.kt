/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */
package com.github.leondevlifelog.gitea.extensions

import com.github.leondevlifelog.gitea.GiteaConfig.ACCESS_TOKEN_AUTH
import com.github.leondevlifelog.gitea.authentication.GiteaAccountAuthData
import com.github.leondevlifelog.gitea.authentication.GiteaAccountsUtil
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountDetail
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath.Companion.fromRemoteUrl
import com.github.leondevlifelog.gitea.api.executeBody
import com.github.leondevlifelog.gitea.exception.GiteaParseException
import com.github.leondevlifelog.gitea.services.GiteaSettings
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.AuthData
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import git4idea.remote.GitHttpAuthDataProvider
import git4idea.remote.hosting.GitHostingUrlUtil.match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class GiteaHttpAuthDataProvider : GitHttpAuthDataProvider {
    @RequiresBackgroundThread
    override fun getAuthData(project: Project, url: String, login: String): AuthData? {
        return runBlocking {
            getAuthDataOrCancel(project, url, login)
        }
    }

    @RequiresBackgroundThread
    override fun getAuthData(project: Project, url: String): AuthData? {
        return runBlocking {
            getAuthDataOrCancel(project, url, null)
        }
    }

    private suspend fun getAuthDataOrCancel(project: Project, url: String, login: String?): AuthData? {
        val accountManager = service<GiteaAccountManager>()
        val accountsWithTokens = accountManager.accountsState.value.filter { match(it.server.toURI(), url) }
            .associateWith { accountManager.findCredentials(it) }

        return withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) {
            when (accountsWithTokens.size) {
                0 -> {
                    val server = try {
                        fromRemoteUrl(url)
                    } catch (_: GiteaParseException) {
                        null
                    }
                    GiteaAccountsUtil.requestNewAccount(server, login, project)
                }

                1 -> {
                    val (account, storedToken) = accountsWithTokens.entries.single()
                    val token = storedToken ?: GiteaAccountsUtil.requestNewToken(account, project) ?: return@withContext null
                    GiteaAccountAuthData(account, ACCESS_TOKEN_AUTH, token)
                }

                else -> {
                    GiteaSelectAccountHttpAuthDataProvider(project, accountsWithTokens).getAuthData(null)
                }
            }
        }
    }

    companion object {
        suspend fun getAccountsWithTokens(project: Project, url: String): Map<GiteaAccount, String?> {
            val accountManager = service<GiteaAccountManager>()
            return HashMap<GiteaAccount, String?>().also { map ->
                accountManager.accountsState.value.filter { match(it.server.toURI(), url) }.forEach { account ->
                    map[account] = accountManager.findCredentials(account)
                }
            }
        }

        suspend fun getAccountDetails(acc: GiteaAccount, token: String): GiteaAccountDetail? {
            val user = service<GiteaSettings>().getGiteaApi(acc.server.toString(), token).getUserApi().userGetCurrent()
                .executeBody()
            return GiteaAccountDetail(user.avatarUrl, user.loginName)
        }
    }
}
