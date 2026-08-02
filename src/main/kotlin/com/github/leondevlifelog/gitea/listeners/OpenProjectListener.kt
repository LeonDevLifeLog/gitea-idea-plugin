/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.listeners

import com.github.leondevlifelog.gitea.GiteaConfig
import com.github.leondevlifelog.gitea.util.GiteaNotifications
import com.intellij.ide.plugins.cl.PluginAwareClassLoader
import com.intellij.ide.util.RunOnceUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class OpenProjectListener : ProjectActivity {
    override suspend fun execute(project: Project) {
        val version = (OpenProjectListener::class.java.classLoader
            as? PluginAwareClassLoader)?.pluginDescriptor?.version
        RunOnceUtil.runOnceForApp(GiteaConfig.GITEA_PLUGIN_ID + version) {
            GiteaNotifications.showStarMe(project)
        }
    }

}
