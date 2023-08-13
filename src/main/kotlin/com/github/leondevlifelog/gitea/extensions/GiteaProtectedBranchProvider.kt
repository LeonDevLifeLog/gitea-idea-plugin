/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */
package com.github.leondevlifelog.gitea.extensions

import com.github.leondevlifelog.gitea.services.GiteaSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import git4idea.config.GitProtectedBranchProvider

class GiteaProtectedBranchProvider : GitProtectedBranchProvider {
    override fun doGetProtectedBranchPatterns(project: Project): List<String> {
        return emptyList()
        // TODO: Implement this method
        val branchProtections =
            service<GiteaSettings>().getGiteaApi("", "").getRepoApi().repoListBranchProtection("", "").execute().body()
                ?: return emptyList()
        return branchProtections.map {
            it.branchName
        }
    }
}
