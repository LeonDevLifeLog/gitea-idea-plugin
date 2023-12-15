/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */
package com.github.leondevlifelog.gitea.authentication.accounts

import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.util.GiteaNotificationIdsHolder
import com.intellij.collaboration.auth.PersistentDefaultAccountHolder
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsNotifier
import kotlinx.coroutines.CoroutineScope

@State(name = "GiteaDefaultAccount", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)], reportStatistic = false)
@Service(Service.Level.PROJECT)
class GiteaProjectDefaultAccountHolder(project: Project, scope: CoroutineScope) :
    PersistentDefaultAccountHolder<GiteaAccount>(project, scope) {

    override fun accountManager() = service<GiteaAccountManager>()

    override fun notifyDefaultAccountMissing() = runInEdt {
        val title = GiteaBundle.message("accounts.default.missing")
        VcsNotifier.IMPORTANT_ERROR_NOTIFICATION.createNotification(title, NotificationType.WARNING)
            .setDisplayId(GiteaNotificationIdsHolder.MISSING_DEFAULT_ACCOUNT)
//            .addAction(GiteaNotifications.getConfigureAction(project))
            .notify(project)
    }
}
