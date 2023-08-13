/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.services

import com.intellij.collaboration.async.disposingScope
import com.intellij.collaboration.auth.Account
import com.intellij.collaboration.auth.AccountManager
import com.intellij.collaboration.auth.AccountUrlAuthenticationFailuresHolder
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Experimental
abstract class HostedGitAuthenticationFailureManager<A : Account>(accountManager: () -> AccountManager<A, *>) :
    Disposable {
    private val holder = AccountUrlAuthenticationFailuresHolder(disposingScope(), accountManager).also {
        Disposer.register(this, it)
    }

    fun ignoreAccount(url: String, account: A) {
        holder.markFailed(account, url)
    }

    fun isAccountIgnored(url: String, account: A): Boolean = holder.isFailed(account, url)
}