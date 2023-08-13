/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui

import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import com.intellij.collaboration.async.DisposingMainScope
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DialogWrapper.IS_VISUAL_PADDING_COMPENSATED_ON_COMPONENT_LEVEL_KEY
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.NlsContexts
import git4idea.i18n.GitBundle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Component
import javax.swing.JComponent

internal fun JComponent.setPaddingCompensated(): JComponent =
    apply { putClientProperty(IS_VISUAL_PADDING_COMPENSATED_ON_COMPONENT_LEVEL_KEY, false) }

internal sealed class GiteaLoginDialog(
    private val model: GiteaLoginModel,
    project: Project?,
    parent: Component?
) : DialogWrapper(project, parent, false, IdeModalityType.PROJECT) {

    private val cs = DisposingMainScope(disposable)

    protected val loginPanel = GiteaLoginPanel() { login, server ->
        model.isAccountUnique(server, login)
    }


    fun setLogin(login: String?, editable: Boolean) = loginPanel.setLogin(login, editable)
    fun setServer(path: String, editable: Boolean) = loginPanel.setServer(path, editable)

    fun setError(exception: Throwable) {
        loginPanel.setError(exception)
        startTrackingValidation()
    }

    override fun getPreferredFocusedComponent(): JComponent? = loginPanel.getPreferredFocusableComponent()

    override fun doValidateAll(): List<ValidationInfo> = loginPanel.doValidateAll()

    override fun doOKAction() {
        cs.launch(Dispatchers.Main.immediate + ModalityState.stateForComponent(rootPane).asContextElement()) {
            try {
                val (login, token) = loginPanel.acquireLoginAndToken()
                val accountManager = service<GiteaAccountManager>()
                val acc = GiteaAccountManager.createAccount(login, loginPanel.getServer())
                accountManager.updateAccount(acc, token)
                model.saveLogin(loginPanel.getServer(), login, token)
                close(OK_EXIT_CODE, true)
            }
            catch (e: Exception) {
                if (e is CancellationException) {
                    close(CANCEL_EXIT_CODE, false)
                    throw e
                }
                setError(e)
            }
        }
    }


    class Token(model: GiteaLoginModel, project: Project?, parent: Component?) :
        GiteaLoginDialog(model, project, parent) {

        init {
            title = GiteaBundle.message("login.to.gitea")
            setLoginButtonText(GitBundle.message("login.dialog.button.login"))
            loginPanel.setTokenUi()

            init()
        }

        internal fun setLoginButtonText(@NlsContexts.Button text: String) = setOKButtonText(text)

        override fun createCenterPanel(): JComponent = loginPanel.setPaddingCompensated()
    }

}

internal interface GiteaLoginModel {
    fun isAccountUnique(server: GiteaServerPath, login: String): Boolean
    suspend fun saveLogin(server: GiteaServerPath, login: String, token: String)
}