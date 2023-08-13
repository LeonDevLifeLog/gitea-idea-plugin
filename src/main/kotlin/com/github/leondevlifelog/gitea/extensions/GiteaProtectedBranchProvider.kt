/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */
package com.github.leondevlifelog.gitea.extensions

import com.intellij.openapi.project.Project
import git4idea.config.GitProtectedBranchProvider

class GiteaProtectedBranchProvider : GitProtectedBranchProvider {
    override fun doGetProtectedBranchPatterns(project: Project): List<String> {
        // TODO: Implement this method
        return emptyList()
    }
}
