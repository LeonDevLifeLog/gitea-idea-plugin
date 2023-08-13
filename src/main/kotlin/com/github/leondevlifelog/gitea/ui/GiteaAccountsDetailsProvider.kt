/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui


import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountDetail
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.icons.GiteaIcons
import com.github.leondevlifelog.gitea.services.GiteaSettings
import com.github.leondevlifelog.gitea.services.CachingGiteaUserAvatarLoader
import com.intellij.collaboration.auth.ui.LazyLoadingAccountsDetailsProvider
import com.intellij.collaboration.auth.ui.cancelOnRemoval
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.awt.Image

internal class GiteaAccountsDetailsProvider(
    scope: CoroutineScope
) : LazyLoadingAccountsDetailsProvider<GiteaAccount, GiteaAccountDetail>(
    scope, GiteaIcons.Gitea
) {

    private lateinit var accountManager: GiteaAccountManager

    constructor(
        scope: CoroutineScope, accountManager: GiteaAccountManager, accountsModel: GiteaAccountsListModel
    ) : this(scope) {
        cancelOnRemoval(accountsModel.accountsListModel)
        this.accountManager = accountManager
    }

    constructor(scope: CoroutineScope, accountManager: GiteaAccountManager) : this(scope) {
        cancelOnRemoval(scope, accountManager)
        this.accountManager = accountManager
    }

    override suspend fun loadDetails(account: GiteaAccount): Result<GiteaAccountDetail> {
        return withContext(Dispatchers.IO) {
            doLoadDetails(account)
        }
    }

    private fun doLoadDetails(
        account: GiteaAccount
    ): Result<GiteaAccountDetail> {
        val token = runBlocking { accountManager.findCredentials(account) } ?: return Result.Error(
            GiteaBundle.message("account.token.missing"), true
        )
        val userApi = service<GiteaSettings>().getGiteaApi(account.server.toString(), token).getUserApi()
        val user = userApi.userGetCurrent().execute().body() ?: return Result.Error("user detail load failed", false)
        return Result.Success(GiteaAccountDetail(user.avatarUrl, account.name))
    }

    override suspend fun loadAvatar(account: GiteaAccount, url: String): Image? {
        return CachingGiteaUserAvatarLoader.getInstance().requestAvatar(url).await()
    }

}
