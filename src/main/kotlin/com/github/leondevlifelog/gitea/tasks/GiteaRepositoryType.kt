package com.github.leondevlifelog.gitea.tasks

import com.github.leondevlifelog.gitea.GiteaConfig
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
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
        return IconLoader.getIcon("resources/icons/gitea.svg", GiteaRepositoryType::class.java)
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