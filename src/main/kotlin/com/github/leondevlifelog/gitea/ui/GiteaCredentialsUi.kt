/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui


import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.UIUtil.getRegularPanelInsets
import javax.swing.JComponent
import javax.swing.JPanel
public typealias Validator = () -> ValidationInfo?

internal abstract class GiteaCredentialsUi {
    abstract fun getPreferredFocusableComponent(): JComponent?
    abstract fun getValidator(): Validator
    abstract suspend fun login(server: GiteaServerPath): Pair<String, String>
    abstract fun handleAcquireError(error: Throwable): ValidationInfo
    abstract fun setBusy(busy: Boolean)

    var footer: Panel.() -> Unit = { }

    fun getPanel(): JPanel =
        panel {
            centerPanel()
            footer()
        }.apply {
            // Border is required to have more space - otherwise there could be issues with focus ring.
            // `getRegularPanelInsets()` is used to simplify border calculation for dialogs where this panel is used.
            border = JBEmptyBorder(getRegularPanelInsets())
        }

    protected abstract fun Panel.centerPanel()
}