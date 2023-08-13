/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui

import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.intellij.collaboration.auth.ui.AccountsListModel
import com.intellij.collaboration.auth.ui.MutableAccountsListModel

class GiteaAccountsListModel : MutableAccountsListModel<GiteaAccount, String>(),
    AccountsListModel.WithDefault<GiteaAccount, String> {
    override var defaultAccount: GiteaAccount? = null
}
