/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui

import com.github.leondevlifelog.gitea.GiteaBundle.message
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import com.github.leondevlifelog.gitea.exception.GiteaAuthenticationException
import com.github.leondevlifelog.gitea.exception.GiteaParseException
import com.github.leondevlifelog.gitea.exception.LoginNotUniqueException
import com.github.leondevlifelog.gitea.services.GiteaSettings
import com.github.leondevlifelog.gitea.util.Utils
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.layout.ComponentPredicate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.UnknownHostException
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.event.DocumentEvent

internal class GiteaTokenCredentialsUi(
    private val serverTextField: ExtendableTextField, private val isAccountUnique: UniqueLoginPredicate
) : GiteaCredentialsUi() {

    private val tokenTextField = JBPasswordField()
    private var fixedLogin: String? = null

    override fun Panel.centerPanel() {
        row(message("credentials.server.field")) { cell(serverTextField).align(AlignX.FILL) }
        row(message("credentials.token.field")) {
            cell(tokenTextField).comment(
                message("clone.dialog.insufficient.scopes")
            ).align(AlignX.FILL).resizableColumn()
            button(message("credentials.button.generate")) { browseNewTokenUrl() }.enabledIf(serverTextField.serverValid)
        }
    }

    private fun browseNewTokenUrl() = BrowserUtil.browse(serverTextField.tryParseServer()!!.toAccessTokenUrl())

    override fun getPreferredFocusableComponent(): JComponent = tokenTextField

    override fun getValidator(): Validator = { Utils.notBlank(tokenTextField, message("login.token.cannot.be.empty")) }

    override suspend fun login(server: GiteaServerPath): Pair<String, String> =
        withContext(Dispatchers.Main.immediate) {
            val token = String(tokenTextField.password)
            val login = acquireLogin(server, token, isAccountUnique)
            login to token
        }

    override fun handleAcquireError(error: Throwable): ValidationInfo = when (error) {
        is GiteaParseException -> ValidationInfo(
            error.message ?: message("credentials.invalid.server.path"), serverTextField
        )

        else -> handleError(error)
    }

    override fun setBusy(busy: Boolean) {
        tokenTextField.isEnabled = !busy
    }

    fun setFixedLogin(fixedLogin: String?) {
        this.fixedLogin = fixedLogin
    }

    companion object {
        suspend fun acquireLogin(
            server: GiteaServerPath, token: String, isAccountUnique: UniqueLoginPredicate
        ): String {
            val details = withContext(Dispatchers.IO) {
                val service = service<GiteaSettings>()
                service.getGiteaApi(server.toString(), token).getUserApi().userGetCurrent().execute().body()
            } ?: throw GiteaAuthenticationException("Token is invalid")
            if (!isAccountUnique(details.login, server)) throw LoginNotUniqueException(details.login)
            return details.login
        }

        fun handleError(error: Throwable): ValidationInfo = when (error) {
            is LoginNotUniqueException -> ValidationInfo(
                message(
                    "login.account.already.added", error.login
                )
            ).withOKEnabled()

            is UnknownHostException -> ValidationInfo(message("server.unreachable")).withOKEnabled()
            is GiteaAuthenticationException -> ValidationInfo(
                message("credentials.incorrect", error.message.orEmpty())
            ).withOKEnabled()

            else -> ValidationInfo(
                message("credentials.invalid.auth.data", error.message.orEmpty())
            ).withOKEnabled()
        }
    }
}

private val JTextField.serverValid: ComponentPredicate
    get() = object : ComponentPredicate() {
        override fun invoke(): Boolean = tryParseServer() != null

        override fun addListener(listener: (Boolean) -> Unit) =
            document.addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) = listener(tryParseServer() != null)
            })
    }

private fun JTextField.tryParseServer(): GiteaServerPath? = try {
    GiteaServerPath.from(text.trim())
} catch (e: GiteaParseException) {
    null
}
