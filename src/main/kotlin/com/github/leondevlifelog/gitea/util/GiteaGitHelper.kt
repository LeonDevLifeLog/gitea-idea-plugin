/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.util

import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import com.github.leondevlifelog.gitea.services.GiteaSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service

/**
 * Utilities for Gitea-Git interactions
 */
@Service
class GiteaGitHelper {

    fun getRemoteUrl(server: GiteaServerPath, user: String, repo: String): String {
        return if (GiteaSettings.getInstance().isCloneGitUsingSsh()) {
            server.toSshCloneUrl(user, repo, GiteaSettings.getInstance().getSshPort())
        } else {
            server.toHttpCloneUrl(user, repo)
        }
    }

    companion object {

        @JvmStatic
        fun getInstance(): GiteaGitHelper {
            return service()
        }
    }
}
