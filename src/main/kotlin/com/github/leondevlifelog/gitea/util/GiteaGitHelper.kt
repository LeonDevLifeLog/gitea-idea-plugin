/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.util

import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import com.github.leondevlifelog.gitea.services.GiteaSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.io.URLUtil

/**
 * Utilities for Gitea-Git interactions
 */
@Service
class GiteaGitHelper {

    fun getRemoteUrl(server: GiteaServerPath, user: String, repo: String): String {
        val port = if (server.getPort() != -1) ":${server.getPort()}" else ""
        return if (GiteaSettings.getInstance().isCloneGitUsingSsh()) {
            "git@${server.getHost()}$port:$user/$repo.git"
        } else {
            "${server.getSchema()}${URLUtil.SCHEME_SEPARATOR}${server.getHost()}$port/$user/$repo.git"
        }
    }

    companion object {

        @JvmStatic
        fun getInstance(): GiteaGitHelper {
            return service()
        }
    }
}
