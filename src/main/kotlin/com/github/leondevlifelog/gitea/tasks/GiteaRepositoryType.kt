/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.tasks

import com.github.leondevlifelog.gitea.GiteaConfig
import com.github.leondevlifelog.gitea.icons.GiteaIcons
import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskRepository
import com.intellij.tasks.config.TaskRepositoryEditor
import com.intellij.tasks.impl.BaseRepositoryType
import com.intellij.util.Consumer
import javax.swing.Icon

class GiteaRepositoryType : BaseRepositoryType<GiteaRepository>() {
    override fun getName(): String {
        return GiteaConfig.SERVICE_DISPLAY_NAME
    }

    override fun getIcon(): Icon {
        return GiteaIcons.Gitea
    }

    override fun createEditor(
        repository: GiteaRepository?, project: Project?, changeListener: Consumer<in GiteaRepository>?
    ): TaskRepositoryEditor {
        return GiteaRepositoryEditor(project, repository, changeListener)
    }

    override fun createRepository(): TaskRepository {
        return GiteaRepository(this)
    }

    override fun getRepositoryClass(): Class<GiteaRepository> {
        return GiteaRepository::class.java
    }
}