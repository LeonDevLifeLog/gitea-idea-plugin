package com.github.leondevlifelog.gitea.toolWindow

import androidx.compose.ui.awt.ComposePanel
import com.github.leondevlifelog.gitea.GiteaBundle
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.leondevlifelog.gitea.services.MyProjectService
import com.github.leondevlifelog.gitea.ui.Welcome


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val prContent =
            ContentFactory.getInstance().createContent(myToolWindow.getContent(), GiteaBundle.message("pr"), false)
        toolWindow.contentManager.addContent(prContent)
        val issueContent =
            ContentFactory.getInstance().createContent(myToolWindow.getContent(), GiteaBundle.message("issue"), false)
        toolWindow.contentManager.addContent(issueContent)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()

        fun getContent(): ComposePanel {
            return ComposePanel().apply {
                setContent {
                    Welcome()
                }
            }
        }
    }
}