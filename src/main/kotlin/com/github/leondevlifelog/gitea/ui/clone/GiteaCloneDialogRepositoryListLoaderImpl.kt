/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui.clone

import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.exception.GiteaMissingTokenException
import com.github.leondevlifelog.gitea.services.GiteaSettings
import com.intellij.collaboration.async.CompletableFutureUtil.errorOnEdt
import com.intellij.collaboration.async.CompletableFutureUtil.submitIOTask
import com.intellij.collaboration.ui.SimpleEventListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.ui.SingleSelectionModel
import com.intellij.util.EventDispatcher
import kotlinx.coroutines.runBlocking
import org.gitnex.tea4j.v2.models.Repository
import javax.swing.ListSelectionModel

internal class GiteaCloneDialogRepositoryListLoaderImpl : GiteaCloneDialogRepositoryListLoader, Disposable {

    override val loading: Boolean
        get() = indicatorsMap.isNotEmpty()
    private val loadingEventDispatcher = EventDispatcher.create(SimpleEventListener::class.java)

    override val listModel = GiteaCloneDialogRepositoryListModel()
    override val listSelectionModel = SingleSelectionModel()

    private val indicatorsMap = mutableMapOf<GiteaAccount, ProgressIndicator>()
    private val accountManager = service<GiteaAccountManager>()

    override fun loadRepositories(account: GiteaAccount) {
        if (indicatorsMap.containsKey(account)) return
        val indicator = EmptyProgressIndicator()
        indicatorsMap[account] = indicator
        loadingEventDispatcher.multicaster.eventOccurred()

        ProgressManager.getInstance().submitIOTask(indicator) {
            val token = runBlocking { service<GiteaAccountManager>().findCredentials(account) }
                ?: throw GiteaMissingTokenException(account)

            val userApi = service<GiteaSettings>().getGiteaApi(account.server.toString(), token).getUserApi()
            val user = userApi.userGetCurrent().execute().body() ?: return@submitIOTask
            
            val allRepos = mutableListOf<Repository>()
            var page = 1
            val perPage = service<GiteaSettings>().getReposPerPage()
            while (true) {
                indicator.checkCanceled()
                val pageResult = userApi.userCurrentListRepos(page, perPage).execute().body() ?: break
                if (pageResult.isEmpty()) break
                allRepos.addAll(pageResult)
                if (pageResult.size < perPage) break
                page += 1
            }

            if (allRepos.isEmpty()) return@submitIOTask
            val mutableList = allRepos.sortedWith(compareBy<Repository>({ repo ->
                val owner = repo.owner?.login?.lowercase() ?: ""
                val userLogin = user.login?.lowercase() ?: ""
                if (owner == userLogin) 0 else 1
            }, { repo -> repo.owner?.login?.lowercase() ?: "" }, { repo -> repo.name?.lowercase() ?: "" })).toMutableList()
            runInEdt {
                indicator.checkCanceled()
                preservingSelection(listModel, listSelectionModel) {
                    listModel.addRepositories(account, user, mutableList.toList())
                }
            }
        }.whenComplete { _, _ ->
            indicatorsMap.remove(account)
            loadingEventDispatcher.multicaster.eventOccurred()
        }.errorOnEdt(ModalityState.any()) {
            preservingSelection(listModel, listSelectionModel) {
                listModel.setError(account, it)
            }
        }
    }

    override fun clear(account: GiteaAccount) {
        indicatorsMap[account]?.cancel()
        listModel.clear(account)
        loadingEventDispatcher.multicaster.eventOccurred()
    }

    override fun addLoadingStateListener(listener: () -> Unit) =
        SimpleEventListener.addListener(loadingEventDispatcher, listener)

    override fun dispose() {
        indicatorsMap.forEach { (_, indicator) -> indicator.cancel() }
        loadingEventDispatcher.multicaster.eventOccurred()
    }

    companion object {
        private fun preservingSelection(
            listModel: GiteaCloneDialogRepositoryListModel, selectionModel: ListSelectionModel, action: () -> Unit
        ) {
            val selection = if (selectionModel.isSelectionEmpty) {
                null
            } else {
                selectionModel.leadSelectionIndex.let {
                    if (it < 0 || listModel.size == 0) null
                    else listModel.getItemAt(it)
                }
            }
            action()
            if (selection != null) {
                val (account, item) = selection
                val newIdx = listModel.indexOf(account, item)
                if (newIdx >= 0) {
                    selectionModel.setSelectionInterval(newIdx, newIdx)
                }
            }
        }
    }
}
