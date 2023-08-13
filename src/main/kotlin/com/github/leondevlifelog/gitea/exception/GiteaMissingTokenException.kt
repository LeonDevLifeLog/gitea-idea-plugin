/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.exception

import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount

class GiteaMissingTokenException(account: GiteaAccount) : RuntimeException() {

}
