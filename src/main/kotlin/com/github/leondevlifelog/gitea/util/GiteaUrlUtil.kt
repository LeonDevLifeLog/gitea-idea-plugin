/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.util

import com.github.leondevlifelog.gitea.api.GiteaRepositoryPath
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import kotlin.math.max


import git4idea.remote.hosting.GitHostingUrlUtil.removeProtocolPrefix

object GiteaUrlUtil {
    @NotNull
    private fun removeTrailingSlash(@NotNull s: String?): @NlsSafe String? {
        return if (s!!.endsWith("/")) {
            s!!.substring(0, s!!.length - 1)
        } else s
    }

    /**
     * git@xxx.com:user/repo.git -> user/repo
     */
    @Nullable
    fun getUserAndRepositoryFromRemoteUrl(@NotNull remoteUrl: String): GiteaRepositoryPath? {
        var remoteUrl = remoteUrl
        remoteUrl = removeProtocolPrefix(removeEndingDotGit(remoteUrl))
        val index1 = remoteUrl.lastIndexOf('/')
        if (index1 == -1) {
            return null
        }
        val url = remoteUrl.substring(0, index1)
        val index2 = max(url.lastIndexOf('/').toDouble(), url.lastIndexOf(':').toDouble()).toInt()
        if (index2 == -1) {
            return null
        }
        val username = remoteUrl.substring(index2 + 1, index1)
        val reponame = remoteUrl.substring(index1 + 1)
        return if (username.isEmpty() || reponame.isEmpty()) {
            null
        } else GiteaRepositoryPath(username, reponame)
    }

    @NotNull
    private fun removeEndingDotGit(@NotNull url: String): @NlsSafe String {
        var url: String? = url
        url = removeTrailingSlash(url)
        val DOT_GIT = ".git"
        return if (url!!.endsWith(DOT_GIT)) {
            url!!.substring(0, url!!.length - DOT_GIT.length)
        } else url
    }
}