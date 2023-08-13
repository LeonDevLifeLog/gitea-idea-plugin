/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */
package com.github.leondevlifelog.gitea.ui.clone

import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.GiteaConfig
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.icons.GiteaIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.ui.cloneDialog.VcsCloneDialogExtension
import com.intellij.openapi.vcs.ui.cloneDialog.VcsCloneDialogExtensionComponent
import com.intellij.openapi.vcs.ui.cloneDialog.VcsCloneDialogExtensionStatusLine
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI.Panels.simplePanel
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.cloneDialog.AccountMenuItem
import com.intellij.util.ui.components.BorderLayoutPanel
import org.jetbrains.annotations.Nls
import javax.swing.Icon
import javax.swing.JComponent

class GiteaCloneDialogExtension : VcsCloneDialogExtension {
    private val accountManager: GiteaAccountManager = service<GiteaAccountManager>()
    override fun getIcon(): Icon {
        return GiteaIcons.Gitea
    }

    override fun getName(): @Nls String {
        return GiteaConfig.SERVICE_DISPLAY_NAME
    }

    override fun getAdditionalStatusLines(): List<VcsCloneDialogExtensionStatusLine> {
        val accounts = accountManager.accountsState.value
        return if (accounts.isEmpty()) listOf(
            VcsCloneDialogExtensionStatusLine.greyText(
                GiteaBundle.message(
                    "clone.dialog.label.no.accounts"
                )
            )
        )
        else accounts.map { account ->
            VcsCloneDialogExtensionStatusLine.greyText(account.name)
        }
    }

    override fun createMainComponent(project: Project, modalityState: ModalityState): VcsCloneDialogExtensionComponent {
        return GiteaCloneDialogExtensionComponent(project, modalityState)
    }

    class GiteaCloneDialogExtensionComponent(project: Project, modalityState: ModalityState) :
        GiteaCloneDialogExtensionComponentBase(project, modalityState, service()) {
        override fun isAccountHandled(account: GiteaAccount): Boolean {
            return true
        }

        override fun createLoginPanel(account: GiteaAccount?, cancelHandler: () -> Unit): JComponent {
            return GiteaCloneDialogLoginPanel(account).apply {
                Disposer.register(this@GiteaCloneDialogExtensionComponent, this)

                loginPanel.isCancelVisible = getAccounts().isNotEmpty()
                loginPanel.setCancelHandler(cancelHandler)
            }
        }

        override fun createAccountMenuLoginActions(account: GiteaAccount?): Collection<AccountMenuItem.Action> {
            return listOf(createLoginAction(account))
        }

        private fun createLoginAction(account: GiteaAccount?): AccountMenuItem.Action {
            val isExistingAccount = account != null
            return AccountMenuItem.Action(
                GiteaBundle.message("login.to.gitea"),
                { switchToLogin(account) },
                showSeparatorAbove = !isExistingAccount
            )
        }

    }

    private class GiteaCloneDialogLoginPanel(account: GiteaAccount?) : BorderLayoutPanel(), Disposable {
        private val titlePanel = simplePanel().apply {
            val title = JBLabel(GiteaBundle.message("login.to.gitea"), UIUtil.ComponentStyle.LARGE).apply {
                font = JBFont.label().biggerOn(5.0f)
            }
            addToLeft(title)
        }
        val loginPanel = CloneDialogLoginPanel(account).apply {
            Disposer.register(this@GiteaCloneDialogLoginPanel, this)

            if (account == null) setServer("", true)
            setTokenUi()
        }

        init {
//            addToTop(titlePanel.apply { border = JBEmptyBorder(getRegularPanelInsets().apply { bottom = 0 }) })
            addToCenter(loginPanel)
        }

        override fun dispose() = loginPanel.cancelLogin()
    }
}
