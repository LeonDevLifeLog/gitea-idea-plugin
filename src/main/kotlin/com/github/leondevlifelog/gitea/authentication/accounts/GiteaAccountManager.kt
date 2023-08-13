/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.authentication.accounts

import com.github.leondevlifelog.gitea.GiteaConfig
import com.intellij.collaboration.auth.AccountManagerBase
import com.intellij.collaboration.auth.AccountsRepository
import com.intellij.collaboration.auth.CredentialsRepository
import com.intellij.collaboration.auth.PasswordSafeCredentialsRepository
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger

@Service
class GiteaAccountManager : AccountManagerBase<GiteaAccount, String>(logger<GiteaAccountManager>()), Disposable {
    override fun accountsRepository(): AccountsRepository<GiteaAccount> {
        return service<GiteaPersistentAccounts>()
    }

    override fun credentialsRepository(): CredentialsRepository<GiteaAccount, String> {
        return PasswordSafeCredentialsRepository(
            GiteaConfig.SERVICE_DISPLAY_NAME, PasswordSafeCredentialsRepository.CredentialsMapper.Simple
        )
    }

    suspend fun forgetPassword(giteaAccount: GiteaAccount): Unit {
        credentialsRepository().persistCredentials(giteaAccount, null)
    }
    fun isAccountUnique(server: GiteaServerPath, accountName: String): Boolean {
        return accountsState.value.none { account: GiteaAccount ->
            account.server.equals(server, true) && account.name == accountName
        }
    }
    companion object {
        fun createAccount(name: String, server: GiteaServerPath) = GiteaAccount(name, server)
    }

    override fun dispose() = Unit

}