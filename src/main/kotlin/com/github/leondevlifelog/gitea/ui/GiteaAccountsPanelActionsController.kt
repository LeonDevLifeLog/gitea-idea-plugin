/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui

import com.github.leondevlifelog.gitea.authentication.GiteaAccountAuthData
import com.github.leondevlifelog.gitea.authentication.GiteaAccountsUtil
import com.github.leondevlifelog.gitea.authentication.GiteaLoginRequest
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import com.intellij.collaboration.auth.ui.AccountsPanelActionsController
import com.intellij.ide.DataManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.swing.JComponent

class GiteaAccountsPanelActionsController(private val project: Project, private val model: GiteaAccountsListModel) :
    AccountsPanelActionsController<GiteaAccount> {

    override val isAddActionWithPopup: Boolean = true

    override fun addAccount(parentComponent: JComponent, point: RelativePoint?) {
        val loginModel = AccountsListModelLoginModel(model)
        val group = GiteaAccountsUtil.createAddAccountActionGroup(loginModel, project, parentComponent)


        val actualPoint = point ?: RelativePoint.getCenterOf(parentComponent)
        JBPopupFactory.getInstance().createActionGroupPopup(
            null,
            group,
            DataManager.getInstance().getDataContext(parentComponent),
            JBPopupFactory.ActionSelectionAid.MNEMONICS,
            false
        ).show(actualPoint)
    }

    override fun editAccount(parentComponent: JComponent, account: GiteaAccount) {
        val loginModel = AccountsListModelLoginModel(model, account)
        GiteaAccountsUtil.login(
            loginModel, GiteaLoginRequest(server = account.server, isServerEditable = false), project, parentComponent
        )
    }

    private class AccountsListModelLoginModel(
        private val model: GiteaAccountsListModel, private val account: GiteaAccount? = null
    ) : GiteaLoginModel {

        override fun isAccountUnique(server: GiteaServerPath, login: String): Boolean = model.accounts.filter {
            it != account
        }.none {
            it.name == login && it.server.equals(server, true)
        }

        override suspend fun saveLogin(server: GiteaServerPath, login: String, token: String) {
            withContext(Dispatchers.Main) {
                val accountManager = service<GiteaAccountManager>()
                if (account == null) {
                    val account = GiteaAccountManager.createAccount(login, server)
                    account.name = login
                    model.add(account, token)
                    accountManager.updateAccount(account, token)
                } else {
                    account.name = login
                    model.update(account, token)
                    accountManager.updateAccount(account, token)
                }
            }
        }
    }

}