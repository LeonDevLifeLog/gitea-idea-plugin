/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.authentication

import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaProjectDefaultAccountHolder
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import com.github.leondevlifelog.gitea.ui.GiteaLoginDialog
import com.github.leondevlifelog.gitea.ui.GiteaLoginModel
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.util.AuthData
import com.intellij.util.concurrency.annotations.RequiresEdt
import git4idea.DialogManager
import java.awt.Component
import javax.swing.JComponent

private val accountManager: GiteaAccountManager get() = service()

object GiteaAccountsUtil {
    @JvmStatic
    val accounts: Set<GiteaAccount>
        get() = accountManager.accountsState.value

    @JvmStatic
    fun getDefaultAccount(project: Project): GiteaAccount? = project.service<GiteaProjectDefaultAccountHolder>().account

    @JvmStatic
    fun setDefaultAccount(project: Project, account: GiteaAccount?) {
        project.service<GiteaProjectDefaultAccountHolder>().account = account
    }

    @JvmStatic
    fun getSingleOrDefaultAccount(project: Project): GiteaAccount? =
        getDefaultAccount(project) ?: accounts.singleOrNull()

    internal fun createAddAccountActionGroup(
        model: GiteaLoginModel, project: Project, parentComponent: JComponent
    ): ActionGroup {
        val group = DefaultActionGroup()
        group.add(DumbAwareAction.create(GiteaBundle.message("action.Gitea.Accounts.AddGiteaAccount.text")) {
            GiteaLoginDialog.Token(model, project, parentComponent).apply {
                title = GiteaBundle.message("dialog.title.add.gitea.account")
                setServer("", true)
                setLoginButtonText(GiteaBundle.message("accounts.add.button"))
                showAndGet()
            }
        })
        return group
    }

    @RequiresEdt
    @JvmOverloads
    @JvmStatic
    internal fun requestNewToken(
        account: GiteaAccount, project: Project?, parentComponent: Component? = null
    ): String? {
        val model = AccountManagerLoginModel(account)
        login(
            model,
            GiteaLoginRequest(
                text = GiteaBundle.message("account.token.missing.for", account),
                server = account.server,
                login = account.name
            ),
            project, parentComponent,
        )
        return model.authData?.token
    }

    @RequiresEdt
    @JvmOverloads
    @JvmStatic
    fun requestReLogin(
        account: GiteaAccount,
        project: Project?,
        parentComponent: Component? = null
    ): GiteaAccountAuthData? {
        val model = AccountManagerLoginModel(account)
        login(
            model,
            GiteaLoginRequest(server = account.server, login = account.name),
            project,
            parentComponent
        )
        return model.authData
    }

    @RequiresEdt
    @JvmOverloads
    @JvmStatic
    fun requestNewAccount(
        server: GiteaServerPath? = null,
        login: String? = null,
        project: Project?,
        parentComponent: Component? = null
    ): GiteaAccountAuthData? {
        val model = AccountManagerLoginModel()
        login(
            model,
            GiteaLoginRequest(server = server, login = login, isLoginEditable = login != null),
            project,
            parentComponent
        )
        return model.authData
    }

    @RequiresEdt
    @JvmStatic
    internal fun login(
        model: GiteaLoginModel, request: GiteaLoginRequest, project: Project?, parentComponent: Component?
    ) {
        request.loginWithToken(model, project, parentComponent)
    }
}

class GiteaAccountAuthData(val account: GiteaAccount, login: String, token: String) : AuthData(login, token) {
    val server: GiteaServerPath get() = account.server
    val token: String get() = password!!
}

internal class GiteaLoginRequest(
    val text: @NlsContexts.DialogMessage String? = null, val error: Throwable? = null,

    val server: GiteaServerPath? = null, val isServerEditable: Boolean = server == null,

    val login: String? = null, val isLoginEditable: Boolean = true,
)

private fun GiteaLoginRequest.configure(dialog: GiteaLoginDialog) {
    error?.let { dialog.setError(it) }
    server?.let { dialog.setServer(it.toString(), isServerEditable) }
    login?.let { dialog.setLogin(it, isLoginEditable) }
}

private fun GiteaLoginRequest.loginWithToken(model: GiteaLoginModel, project: Project?, parentComponent: Component?) {
    val dialog = GiteaLoginDialog.Token(model, project, parentComponent)
    configure(dialog)
    DialogManager.show(dialog)
}

private class AccountManagerLoginModel(private val account: GiteaAccount? = null) : GiteaLoginModel {
    private val accountManager: GiteaAccountManager = service()

    var authData: GiteaAccountAuthData? = null

    override fun isAccountUnique(server: GiteaServerPath, login: String): Boolean =
        accountManager.accountsState.value.filter {
            it != account
        }.none {
            it.name == login && it.server.equals(server, true)
        }

    override suspend fun saveLogin(server: GiteaServerPath, login: String, token: String) {
        val acc = account ?: GiteaAccountManager.createAccount(login, server)
        acc.name = login
        accountManager.updateAccount(acc, token)
        authData = GiteaAccountAuthData(acc, login, token)
    }
}