/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.tasks

import com.intellij.tasks.generic.GenericTask
import org.gitnex.tea4j.v2.models.Issue
import java.util.*

class GiteaTask(private val giteaRepository: GiteaRepository, private val issue: Issue) :
    GenericTask(issue.number.toString(), issue.title, giteaRepository) {
    override fun getCreated(): Date {
        return issue.createdAt
    }

    override fun getDescription(): String? {
        return issue.body
    }

    override fun getIssueUrl(): String? {
        return issue.htmlUrl
    }
}
