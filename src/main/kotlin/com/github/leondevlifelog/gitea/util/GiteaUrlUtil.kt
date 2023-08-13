/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.util


import com.github.leondevlifelog.gitea.api.GiteaRepositoryPath
import git4idea.remote.hosting.GitHostingUrlUtil.removeProtocolPrefix
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import kotlin.math.max

object GiteaUrlUtil {
    private const val DOT_GIT = ".git"

    @NotNull
    private fun removeTrailingSlash(@NotNull s: String): String {
        return if (s.endsWith("/")) {
            s.substring(0, s.length - 1)
        } else s
    }

    /**
     * git@xxx.com:user/repo.git -> user/repo
     */
    @Nullable
    fun getUserAndRepositoryFromRemoteUrl(@NotNull repoUrl: String): GiteaRepositoryPath? {
        var remoteUrl = repoUrl
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
    private fun removeEndingDotGit(@NotNull urlWithDot: String): String {
        var url: String = urlWithDot
        url = removeTrailingSlash(url)
        return if (url.endsWith(DOT_GIT)) {
            url.substring(0, url.length - DOT_GIT.length)
        } else url
    }
}