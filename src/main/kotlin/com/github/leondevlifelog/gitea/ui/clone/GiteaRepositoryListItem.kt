/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui.clone


import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import org.gitnex.tea4j.v2.models.Repository
import org.gitnex.tea4j.v2.models.User

sealed class GiteaRepositoryListItem(
    val account: GiteaAccount
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiteaRepositoryListItem

        return account == other.account
    }

    override fun hashCode(): Int {
        return account.hashCode()
    }

    class Repo(
        account: GiteaAccount,
        val user: User,
        val repo: Repository
    ) : GiteaRepositoryListItem(account) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as Repo

            if (user != other.user) return false
            if (repo != other.repo) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + user.hashCode()
            result = 31 * result + repo.hashCode()
            return result
        }
    }

    class Error(account: GiteaAccount, val error: Throwable) : GiteaRepositoryListItem(account) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as Error

            return error == other.error
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + error.hashCode()
            return result
        }
    }
}