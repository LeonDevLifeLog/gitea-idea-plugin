/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui.clone

import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.intellij.ui.SingleSelectionModel
import javax.swing.ListModel

interface GiteaCloneDialogRepositoryListLoader {
    val loading: Boolean
    val listModel: ListModel<GiteaRepositoryListItem>
    val listSelectionModel: SingleSelectionModel

    fun loadRepositories(account: GiteaAccount)
    fun clear(account: GiteaAccount)
    fun addLoadingStateListener(listener: () -> Unit)
}