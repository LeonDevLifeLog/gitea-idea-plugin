/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.authentication.accounts

import com.intellij.collaboration.auth.AccountsRepository
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "GiteaAccounts",
    storages = [Storage(value = "gitea.xml")],
    reportStatistic = false,
    category = SettingsCategory.TOOLS
)
class GiteaPersistentAccounts : AccountsRepository<GiteaAccount>, PersistentStateComponent<Array<GiteaAccount>> {

    private var state = emptyArray<GiteaAccount>()

    override var accounts: Set<GiteaAccount>
        get() = state.toSet()
        set(value) {
            state = value.toTypedArray()
        }

    override fun getState(): Array<GiteaAccount> = state

    override fun loadState(state: Array<GiteaAccount>) {
        this.state = state
    }
}