/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui

import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.GiteaConfig
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaProjectDefaultAccountHolder
import com.github.leondevlifelog.gitea.services.GiteaSettings
import com.intellij.collaboration.auth.ui.AccountsPanelFactory
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.dsl.builder.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus

class GiteaSettingsConfigurable internal constructor(private val project: Project) :
    BoundConfigurable(GiteaConfig.SERVICE_DISPLAY_NAME, "settings.gitea") {
    override fun createPanel(): DialogPanel {
        val defaultAccountHolder = project.service<GiteaProjectDefaultAccountHolder>()
        val accountManager = service<GiteaAccountManager>()
        val settings = GiteaSettings.getInstance()

        val scope = MainScope().also {
            Disposer.register(disposable!!) {
                it.cancel()
            }
        } + ModalityState.any().asContextElement()
        val accountsModel = GiteaAccountsListModel()
        val detailsProvider = GiteaAccountsDetailsProvider(scope, accountManager, accountsModel)

        val panelFactory = AccountsPanelFactory(scope, accountManager, defaultAccountHolder, accountsModel)
        val actionsController = GiteaAccountsPanelActionsController(project, accountsModel)

        return panel {
            row {
                panelFactory.accountsPanelCell(this, detailsProvider, actionsController).align(Align.FILL)
            }.resizableRow()
            row(GiteaBundle.message("settings.ssh.port")) {
                intTextField(range = 22..65535).columns(2).bindIntText({ settings.getSshPort() }, {
                    settings.setSshPort(it)
                }).gap(RightGap.SMALL)
            }
            row {
                checkBox(GiteaBundle.message("settings.clone.ssh")).bindSelected(
                    settings::isCloneGitUsingSsh, settings::setCloneGitUsingSsh
                )
            }
            row(GiteaBundle.message("settings.timeout")) {
                intTextField(range = 0..60).columns(2).bindIntText({ settings.getConnectionTimeout() / 1000 }, {
                    settings.setConnectionTimeout(it * 1000)
                }).gap(RightGap.SMALL)
                @Suppress("DialogTitleCapitalization") label(GiteaBundle.message("settings.timeout.seconds"))
            }
        }
    }
}
