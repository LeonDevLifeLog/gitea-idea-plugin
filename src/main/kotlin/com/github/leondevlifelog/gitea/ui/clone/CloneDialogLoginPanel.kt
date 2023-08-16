/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui.clone

import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.ui.GiteaLoginPanel
import com.intellij.ide.IdeBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts.ENTER
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys.CONTEXT_COMPONENT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes.ERROR_ATTRIBUTES
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.scale.JBUIScale.scale
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI.Borders.empty
import com.intellij.util.ui.JBUI.Panels.simplePanel
import com.intellij.util.ui.UIUtil.getRegularPanelInsets
import kotlinx.coroutines.*
import java.awt.Dimension
import java.awt.LayoutManager
import java.awt.Rectangle
import javax.swing.*

internal class CloneDialogLoginPanel(private val account: GiteaAccount?) :
    JBPanel<CloneDialogLoginPanel>(ListLayout.vertical(0)), Disposable {

    private val cs = MainScope().also {
        Disposer.register(this) {
            it.cancel()
        }
    }

    private val accountManager get() = service<GiteaAccountManager>()

    private val errorPanel = VerticalListPanel(10)
    private val loginPanel = GiteaLoginPanel { name, server ->
        return@GiteaLoginPanel if (account == null) accountManager.isAccountUnique(server, name) else true
    }
    private val inlineCancelPanel = simplePanel()
    private val loginButton = JButton(GiteaBundle.message("clone.dialog.button.login.mnemonic"))
    private val backLink = LinkLabel<Any?>(IdeBundle.message("button.back"), null).apply {
        verticalAlignment = SwingConstants.CENTER
    }

    private var errors = emptyList<ValidationInfo>()
    private var loginJob: Job? = null

    var isCancelVisible: Boolean
        get() = backLink.isVisible
        set(value) {
            backLink.isVisible = value
        }

    init {
        buildLayout()

        if (account != null) {
            loginPanel.setServer(account.server.toURI().toString(), false)
            loginPanel.setLogin(account.name, false)
        }

        loginButton.addActionListener { login() }
        LoginAction().registerCustomShortcutSet(ENTER, loginPanel)
    }

    fun setCancelHandler(listener: () -> Unit) = backLink.setListener(
        { _, _ ->
            cancelLogin()
            listener()
        }, null
    )

    fun setTokenUi() {
        setupNewUi(false)
        loginPanel.setTokenUi()
    }

    fun setServer(path: String, editable: Boolean) = loginPanel.setServer(path, editable)

    override fun dispose() {
        cancelLogin()
    }

    private fun buildLayout() {
        add(HorizontalListPanel().apply {
            add(loginPanel)
            add(inlineCancelPanel.apply { border = JBEmptyBorder(getRegularPanelInsets().apply { left = scale(6) }) })
        })
        add(errorPanel.apply { border = JBEmptyBorder(getRegularPanelInsets().apply { top = 0 }) })
    }

    private fun setupNewUi(isOAuth: Boolean) {
        loginButton.isVisible = !isOAuth
        backLink.text = if (isOAuth) IdeBundle.message("link.cancel") else IdeBundle.message("button.back")

        loginPanel.footer =
            { if (!isOAuth) buttonPanel() } // footer is used to put buttons in 2-nd column - align under text boxes
        if (isOAuth) inlineCancelPanel.addToCenter(backLink)
        inlineCancelPanel.isVisible = isOAuth

        clearErrors()
    }

    private fun Panel.buttonPanel() = row("") {
        cell(loginButton)
        cell(backLink)
    }

    fun cancelLogin() {
        loginJob?.cancel()
    }

    private fun login() {
        cancelLogin()

        loginPanel.setError(null)
        clearErrors()
        if (!doValidate()) return

        loginJob = cs.async(Dispatchers.Main.immediate + ModalityState.stateForComponent(this).asContextElement()) {
            try {
                val (login, token) = loginPanel.acquireLoginAndToken()
                val acc = account ?: GiteaAccountManager.createAccount(login, loginPanel.getServer())
                accountManager.updateAccount(acc, token)
                clearErrors()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                clearErrors()
                doValidate()
            }
        }
    }

    private fun doValidate(): Boolean {
        errors = loginPanel.doValidateAll()
        setErrors(errors)

        val toFocus = errors.firstOrNull()?.component
        if (toFocus?.isVisible == true) IdeFocusManager.getGlobalInstance().requestFocus(toFocus, true)

        return errors.isEmpty()
    }

    private fun clearErrors() {
        for (component in errors.mapNotNull { it.component }) {
            ComponentValidator.getInstance(component).ifPresent { it.updateInfo(null) }
        }
        errorPanel.removeAll()
        errors = emptyList()
    }

    private fun setErrors(errors: Collection<ValidationInfo>) {
        for (error in errors) {
            val component = error.component

            if (component != null) {
                ComponentValidator.getInstance(component).orElseGet { ComponentValidator(this).installOn(component) }
                    .updateInfo(error)
            } else {
                errorPanel.add(toErrorComponent(error))
            }
        }

        errorPanel.revalidate()
        errorPanel.repaint()
    }

    private fun toErrorComponent(info: ValidationInfo): JComponent = SimpleColoredComponent().apply {
        myBorder = empty()
        ipad = JBInsets(0, 0, 0, 0)

        append(info.message, ERROR_ATTRIBUTES)
    }

    private inner class LoginAction : DumbAwareAction() {

        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabledAndVisible = e.getData(CONTEXT_COMPONENT) != backLink
        }

        override fun actionPerformed(e: AnActionEvent) = login()
    }
}

@Suppress("FunctionName")
fun HorizontalListPanel(gap: Int = 0): JPanel =
    ScrollablePanel(ListLayout.horizontal(gap), SwingConstants.HORIZONTAL).apply {
        isOpaque = false
    }

private class ScrollablePanel(layout: LayoutManager?, private val orientation: Int) : JPanel(layout), Scrollable {

    private var verticalUnit = 1
    private var horizontalUnit = 1

    override fun addNotify() {
        super.addNotify()
        val fontMetrics = getFontMetrics(font)
        verticalUnit = fontMetrics.maxAscent + fontMetrics.maxDescent
        horizontalUnit = fontMetrics.charWidth('W')
    }

    override fun getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int =
        if (orientation == SwingConstants.HORIZONTAL) horizontalUnit else verticalUnit

    override fun getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int): Int =
        if (orientation == SwingConstants.HORIZONTAL) visibleRect.width else visibleRect.height

    override fun getPreferredScrollableViewportSize(): Dimension? = preferredSize

    override fun getScrollableTracksViewportWidth(): Boolean = orientation == SwingConstants.VERTICAL

    override fun getScrollableTracksViewportHeight(): Boolean = orientation == SwingConstants.HORIZONTAL
}

@Suppress("FunctionName")
fun VerticalListPanel(gap: Int = 0): JPanel = ScrollablePanel(ListLayout.vertical(gap), SwingConstants.VERTICAL).apply {
    isOpaque = false
}