/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui


import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import com.github.leondevlifelog.gitea.util.Utils.notBlank
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.Panel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.swing.JComponent
import javax.swing.JTextField

internal typealias UniqueLoginPredicate = (login: String, server: GiteaServerPath) -> Boolean

internal class GiteaLoginPanel(
    isAccountUnique: UniqueLoginPredicate
) : Wrapper() {

    private val serverTextField = ExtendableTextField("", 0)
    private var tokenAcquisitionError: ValidationInfo? = null

    private lateinit var currentUi: GiteaCredentialsUi
    private var tokenUi = GiteaTokenCredentialsUi(serverTextField,isAccountUnique)

    private val progressIcon = AnimatedIcon.Default()
    private val progressExtension = ExtendableTextComponent.Extension { progressIcon }

    var footer: Panel.() -> Unit
        get() = tokenUi.footer
        set(value) {
            tokenUi.footer = value
            applyUi(currentUi)
        }

    init {
        applyUi(tokenUi)
    }

    private fun applyUi(ui: GiteaCredentialsUi) {
        currentUi = ui
        setContent(currentUi.getPanel())
        currentUi.getPreferredFocusableComponent()?.requestFocus()
        tokenAcquisitionError = null
    }

    fun getPreferredFocusableComponent(): JComponent? =
        serverTextField.takeIf { it.isEditable && it.text.isBlank() } ?: currentUi.getPreferredFocusableComponent()

    fun doValidateAll(): List<ValidationInfo> {
        var uiError: ValidationInfo? =
            notBlank(serverTextField, GiteaBundle.message("credentials.server.cannot.be.empty")) ?: validateServerPath(
                serverTextField
            ) ?: currentUi.getValidator().invoke()
        return listOfNotNull(uiError, tokenAcquisitionError)
    }

    private fun validateServerPath(field: JTextField): ValidationInfo? = try {
        GiteaServerPath.from(field.text)
        null
    } catch (e: Exception) {
        ValidationInfo(GiteaBundle.message("credentials.server.path.invalid"), field)
    }

    private fun setBusy(busy: Boolean) {
        serverTextField.apply { if (busy) addExtension(progressExtension) else removeExtension(progressExtension) }
        serverTextField.isEnabled = !busy

        currentUi.setBusy(busy)
    }

    suspend fun acquireLoginAndToken(): Pair<String, String> =
        withContext(Dispatchers.Main.immediate + ModalityState.stateForComponent(this).asContextElement()) {
            try {
                setBusy(true)
                tokenAcquisitionError = null
                currentUi.login(getServer())
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                setError(e)
                throw e
            } finally {
                setBusy(false)
            }
        }

    fun getServer(): GiteaServerPath = GiteaServerPath.from(serverTextField.text.trim())

    fun setServer(path: String, editable: Boolean) {
        serverTextField.text = path
        serverTextField.isEditable = editable
    }

    fun setLogin(login: String?, editable: Boolean) {
        tokenUi.setFixedLogin(if (editable) null else login)
    }

    fun setError(exception: Throwable?) {
        tokenAcquisitionError = exception?.let { currentUi.handleAcquireError(it) }
    }

    fun setTokenUi() = applyUi(tokenUi)
}