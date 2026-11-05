/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui.clone

import com.github.leondevlifelog.gitea.api.executeBody
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.exception.GiteaMissingTokenException
import com.github.leondevlifelog.gitea.services.GiteaSettings
import com.intellij.collaboration.ui.SimpleEventListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.service
import com.intellij.ui.SingleSelectionModel
import com.intellij.util.EventDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.gitnex.tea4j.v2.models.Repository
import javax.swing.ListSelectionModel

internal class GiteaCloneDialogRepositoryListLoaderImpl : GiteaCloneDialogRepositoryListLoader, Disposable {

    override val loading: Boolean
        get() = jobsByAccount.isNotEmpty()
    private val loadingEventDispatcher = EventDispatcher.create(SimpleEventListener::class.java)

    override val listModel = GiteaCloneDialogRepositoryListModel()
    override val listSelectionModel = SingleSelectionModel()

    private val jobsByAccount = mutableMapOf<GiteaAccount, Job>()
    private val accountManager = service<GiteaAccountManager>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val uiContext = Dispatchers.EDT + ModalityState.any().asContextElement()

    override fun loadRepositories(account: GiteaAccount) {
        if (jobsByAccount.containsKey(account)) return

        lateinit var job: Job
        job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                val token = accountManager.findCredentials(account) ?: throw GiteaMissingTokenException(account)
                val userApi = service<GiteaSettings>().getGiteaApi(account.server.toString(), token).getUserApi()
                val user = userApi.userGetCurrent().executeBody()

                val allRepos = mutableListOf<Repository>()
                var page = 1
                val perPage = service<GiteaSettings>().getReposPerPage()
                while (true) {
                    ensureActive()
                    val pageResult = userApi.userCurrentListRepos(page, perPage).executeBody()
                    if (pageResult.isEmpty()) break
                    allRepos.addAll(pageResult)
                    if (pageResult.size < perPage) break
                    page += 1
                }

                val repositories = allRepos.sortedWith(compareBy<Repository>({ repo ->
                    val owner = repo.owner?.login?.lowercase() ?: ""
                    val userLogin = user.login?.lowercase() ?: ""
                    if (owner == userLogin) 0 else 1
                }, { repo -> repo.owner?.login?.lowercase() ?: "" }, { repo -> repo.name?.lowercase() ?: "" }))

                withContext(uiContext) {
                    ensureActive()
                    preservingSelection(listModel, listSelectionModel) {
                        listModel.addRepositories(account, user, repositories)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                withContext(uiContext) {
                    preservingSelection(listModel, listSelectionModel) {
                        listModel.setError(account, e)
                    }
                }
            } finally {
                withContext(NonCancellable + uiContext) {
                    if (jobsByAccount[account] === job) {
                        jobsByAccount.remove(account)
                        loadingEventDispatcher.multicaster.eventOccurred()
                    }
                }
            }
        }
        jobsByAccount[account] = job
        loadingEventDispatcher.multicaster.eventOccurred()
        job.start()
    }

    override fun clear(account: GiteaAccount) {
        jobsByAccount.remove(account)?.cancel()
        listModel.clear(account)
        loadingEventDispatcher.multicaster.eventOccurred()
    }

    override fun addLoadingStateListener(listener: () -> Unit) =
        SimpleEventListener.addListener(loadingEventDispatcher, listener)

    override fun dispose() {
        jobsByAccount.values.forEach { it.cancel() }
        jobsByAccount.clear()
        scope.cancel()
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
