/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui

import androidx.compose.ui.awt.ComposePanel
import com.github.leondevlifelog.gitea.GiteaBundle
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.leondevlifelog.gitea.services.GiteaService


class GiteaToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val giteaToolWindow = GiteaToolWindow(toolWindow)
        val prContent =
            ContentFactory.getInstance().createContent(giteaToolWindow.getContent(), GiteaBundle.message("pr"), false)
        toolWindow.contentManager.addContent(prContent)
        val issueContent =
            ContentFactory.getInstance().createContent(giteaToolWindow.getContent(), GiteaBundle.message("issue"), false)
        toolWindow.contentManager.addContent(issueContent)
    }

    override fun shouldBeAvailable(project: Project) = true

    class GiteaToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<GiteaService>()

        fun getContent(): ComposePanel {
            return ComposePanel().apply {
                setContent {
                    Welcome()
                }
            }
        }
    }
}
