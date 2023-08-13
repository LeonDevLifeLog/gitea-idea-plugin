/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.authentication.accounts

import com.intellij.collaboration.auth.ServerAccount
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.Transient
import org.jetbrains.annotations.VisibleForTesting

@Tag("account")
class GiteaAccount(
    @set:Transient
    @NlsSafe
    @Attribute("name")
    override var name: String = "",
    @Property(style = Property.Style.ATTRIBUTE, surroundWithTag = false)
    override val server: GiteaServerPath = GiteaServerPath(),

    @Attribute("id") @VisibleForTesting override val id: String = generateId()
) : ServerAccount() {
    override fun toString(): String = "$server/$name"
}