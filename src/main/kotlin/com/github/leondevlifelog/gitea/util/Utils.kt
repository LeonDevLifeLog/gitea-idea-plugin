/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.util

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.NlsContexts
import javax.swing.JTextField
object Utils {
fun notBlank(textField: JTextField, @NlsContexts.DialogMessage message: String): ValidationInfo? {
    return if (textField.text.isNullOrBlank()) ValidationInfo(message, textField) else null
}}