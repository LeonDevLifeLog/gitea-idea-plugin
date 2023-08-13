/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.authentication.accounts

import com.intellij.collaboration.auth.AccountDetails

class GiteaAccountDetail(override val avatarUrl: String?, override val name: String) :AccountDetails {
}