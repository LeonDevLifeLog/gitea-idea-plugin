/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.extensions

import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.GiteaConfig.ACCESS_TOKEN_AUTH
import com.github.leondevlifelog.gitea.authentication.GiteaAccountAuthData
import com.github.leondevlifelog.gitea.authentication.GiteaAccountsUtil
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.ui.GiteaChooseAccountDialog
import com.intellij.openapi.project.Project
import com.intellij.util.AuthData
import com.intellij.util.concurrency.annotations.RequiresEdt
import git4idea.DialogManager
import git4idea.i18n.GitBundle
import git4idea.remote.InteractiveGitHttpAuthDataProvider
import java.awt.Component

internal class GiteaSelectAccountHttpAuthDataProvider(
    private val project: Project, private val potentialAccounts: Map<GiteaAccount, String?>
) : InteractiveGitHttpAuthDataProvider {

    @RequiresEdt
    override fun getAuthData(parentComponent: Component?): AuthData? {
        val (account, setDefault) = chooseAccount(parentComponent) ?: return null
        val token = potentialAccounts[account] ?: GiteaAccountsUtil.requestNewToken(account, project, parentComponent)
        ?: return null
        if (setDefault) {
            GiteaAccountsUtil.setDefaultAccount(project, account)
        }

        return GiteaAccountAuthData(account, ACCESS_TOKEN_AUTH, token)
    }

    private fun chooseAccount(parentComponent: Component?): Pair<GiteaAccount, Boolean>? {
        val dialog = GiteaChooseAccountDialog(
            project,
            parentComponent,
            potentialAccounts.keys,
            null,
            false,
            true,
            GiteaBundle.message("account.choose.title"),
            GitBundle.message("login.dialog.button.login")
        )
        DialogManager.show(dialog)

        return if (dialog.isOK) dialog.account to dialog.setDefault else null
    }
}