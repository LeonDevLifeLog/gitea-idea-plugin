/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */
package com.github.leondevlifelog.gitea.extensions

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import git4idea.fetch.GitFetchHandler
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository

class GiteaProtectedBranchRulesLoader : GitFetchHandler {
    override fun doAfterSuccessfulFetch(
        project: Project, fetches: Map<GitRepository, List<GitRemote>>, indicator: ProgressIndicator
    ) {
    }
}
