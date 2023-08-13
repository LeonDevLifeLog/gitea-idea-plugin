/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.services
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service

@Service(Service.Level.PROJECT)
internal class GiteaGitAuthenticationFailureManager
    : HostedGitAuthenticationFailureManager<GiteaAccount>(accountManager = { service<GiteaAccountManager>() }) {
    override fun dispose() = Unit
}