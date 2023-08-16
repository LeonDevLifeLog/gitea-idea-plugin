/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui.clone

import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.api.GiteaRepositoryCoordinates
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccountManager
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import com.github.leondevlifelog.gitea.exception.GiteaAuthenticationException
import com.github.leondevlifelog.gitea.exception.GiteaMissingTokenException
import com.github.leondevlifelog.gitea.ui.GiteaAccountsDetailsProvider
import com.github.leondevlifelog.gitea.ui.clone.util.LinkActionMouseAdapter
import com.github.leondevlifelog.gitea.util.GiteaGitHelper
import com.github.leondevlifelog.gitea.util.GiteaNotificationIdsHolder
import com.github.leondevlifelog.gitea.util.GiteaNotifications
import com.github.leondevlifelog.gitea.util.GiteaUrlUtil
import com.intellij.collaboration.auth.ui.CompactAccountsPanelFactory
import com.intellij.collaboration.ui.CollaborationToolsUIUtil
import com.intellij.collaboration.util.CollectionDelta
import com.intellij.dvcs.repo.ClonePathProvider
import com.intellij.dvcs.ui.CloneDvcsValidationUtils
import com.intellij.dvcs.ui.DvcsBundle.message
import com.intellij.dvcs.ui.FilePathDocumentChildPathHandle
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.CheckoutProvider
import com.intellij.openapi.vcs.ui.cloneDialog.VcsCloneDialogExtensionComponent
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.CollectionListModel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBList
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.cloneDialog.AccountMenuItem
import com.intellij.util.ui.cloneDialog.VcsCloneDialogUiSpec
import git4idea.GitUtil
import git4idea.checkout.GitCheckoutProvider
import git4idea.commands.Git
import git4idea.remote.GitRememberedInputs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import org.jetbrains.annotations.Nls
import java.awt.event.ActionEvent
import java.nio.file.Paths
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JSeparator
import javax.swing.ListModel
import javax.swing.event.DocumentEvent
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener
import kotlin.properties.Delegates

public abstract class GiteaCloneDialogExtensionComponentBase(
    private val project: Project,
    private val modalityState: ModalityState,
    private val accountManager: GiteaAccountManager
) : VcsCloneDialogExtensionComponent() {

    private val LOG = thisLogger()

    private val giteaGitHelper: GiteaGitHelper = GiteaGitHelper.getInstance()

    private val cs = MainScope().also {
        Disposer.register(this) {
            it.cancel()
        }
    } + modalityState.asContextElement()

    // UI
    private val wrapper: Wrapper = Wrapper()
    private val repositoriesPanel: DialogPanel
    private val repositoryList: JBList<GiteaRepositoryListItem>

    private val searchField: SearchTextField
    private val directoryField = TextFieldWithBrowseButton().apply {
        val fcd = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        fcd.isShowFileSystemRoots = true
        fcd.isHideIgnored = false
        addBrowseFolderListener(message("clone.destination.directory.browser.title"),
            message("clone.destination.directory.browser.description"),
            project,
            fcd)
    }
    private val cloneDirectoryChildHandle = FilePathDocumentChildPathHandle
        .install(directoryField.textField.document, ClonePathProvider.defaultParentDirectoryPath(project, GitRememberedInputs.getInstance()))

    // state
    private val loader = GiteaCloneDialogRepositoryListLoaderImpl()
    private var inLoginState = false
    private var selectedUrl by Delegates.observable<String?>(null) { _, _, _ -> onSelectedUrlChanged() }

    protected val content: JComponent get() = wrapper.targetComponent

    private val accountListModel: ListModel<GiteaAccount> = createAccountsModel()

    init {
        repositoryList = JBList(loader.listModel).apply {
            cellRenderer = GiteaRepositoryListCellRenderer(ErrorHandler()) { accountListModel.itemsSet }
            isFocusable = false
            selectionModel = loader.listSelectionModel
        }.also {
            val mouseAdapter = LinkActionMouseAdapter(it)
            it.addMouseListener(mouseAdapter)
            it.addMouseMotionListener(mouseAdapter)
            it.addListSelectionListener { evt ->
                if (evt.valueIsAdjusting) return@addListSelectionListener
                updateSelectedUrl()
            }
        }
        //TODO: fix jumping selection in the presence of filter
        loader.addLoadingStateListener {
            repositoryList.setPaintBusy(loader.loading)
        }

        searchField = SearchTextField(false).also {
            it.addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) = updateSelectedUrl()
            })
            createFocusFilterFieldAction(it)
        }

        CollaborationToolsUIUtil.attachSearch(repositoryList, searchField) {
            when (it) {
                is GiteaRepositoryListItem.Repo -> it.repo.fullName
                is GiteaRepositoryListItem.Error -> ""
            }
        }

        @Suppress("LeakingThis")
        val parentDisposable: Disposable = this
        Disposer.register(parentDisposable, loader)

        val accountDetailsProvider = GiteaAccountsDetailsProvider(cs, accountManager)

        val accountsPanel = CompactAccountsPanelFactory(accountListModel)
            .create(accountDetailsProvider, VcsCloneDialogUiSpec.Components.avatarSize, AccountsPopupConfig())

        repositoriesPanel = panel {
            row {
                cell(searchField.textEditor)
                    .resizableColumn()
                    .align(Align.FILL)
                cell(JSeparator(JSeparator.VERTICAL))
                    .align(AlignY.FILL)
                cell(accountsPanel)
                    .align(AlignY.FILL)
            }
            row {
                scrollCell(repositoryList)
                    .resizableColumn()
                    .align(Align.FILL)
            }.resizableRow()
            row(GiteaBundle.message("clone.dialog.directory.to.clone.label.text")) {
                cell(directoryField)
                    .align(AlignX.FILL)
                    .validationOnApply {
                        CloneDvcsValidationUtils.checkDirectory(it.text, it)
                    }
            }
        }
        repositoriesPanel.border = JBEmptyBorder(UIUtil.getRegularPanelInsets())
        setupAccountsListeners()
    }

    private inner class ErrorHandler : GiteaRepositoryListCellRenderer.ErrorHandler {

        override fun getPresentableText(error: Throwable): @Nls String = when (error) {
            is GiteaMissingTokenException -> GiteaBundle.message("account.token.missing")
            is GiteaAuthenticationException -> GiteaBundle.message("credentials.invalid.auth.data", error.message)
            else -> GiteaBundle.message("clone.error.load.repositories")
        }

        override fun getAction(account: GiteaAccount, error: Throwable) = when (error) {
            is GiteaAuthenticationException -> object : AbstractAction(GiteaBundle.message("accounts.relogin")) {
                override fun actionPerformed(e: ActionEvent?) {
                    switchToLogin(account)
                }
            }
            else -> object : AbstractAction(GiteaBundle.message("retry.link")) {
                override fun actionPerformed(e: ActionEvent?) {
                    loader.clear(account)
                    loader.loadRepositories(account)
                }
            }
        }
    }

    protected abstract fun isAccountHandled(account: GiteaAccount): Boolean

    protected fun getAccounts(): Set<GiteaAccount> = accountListModel.itemsSet

    protected abstract fun createLoginPanel(account: GiteaAccount?, cancelHandler: () -> Unit): JComponent

    private fun setupAccountsListeners() {
        accountListModel.addListDataListener(object : ListDataListener {

            private var currentList by Delegates.observable(emptySet<GiteaAccount>()) { _, oldValue, newValue ->
                val delta = CollectionDelta(oldValue, newValue)
                for (account in delta.removedItems) {
                    loader.clear(account)
                }
                for (account in delta.newItems) {
                    loader.loadRepositories(account)
                }

                if (newValue.isEmpty()) {
                    switchToLogin(null)
                }
                else {
                    switchToRepositories()
                }
                dialogStateListener.onListItemChanged()
            }

            init {
                currentList = accountListModel.itemsSet
            }

            override fun intervalAdded(e: ListDataEvent) {
                currentList = accountListModel.itemsSet
            }

            override fun intervalRemoved(e: ListDataEvent) {
                currentList = accountListModel.itemsSet
            }

            override fun contentsChanged(e: ListDataEvent) {
                for (i in e.index0..e.index1) {
                    val account = accountListModel.getElementAt(i)
                    loader.clear(account)
                    loader.loadRepositories(account)
                }
                switchToRepositories()
                dialogStateListener.onListItemChanged()
            }
        })
    }

    protected fun switchToLogin(account: GiteaAccount?) {
        wrapper.setContent(createLoginPanel(account) { switchToRepositories() })
        wrapper.repaint()
        inLoginState = true
        updateSelectedUrl()
    }

    private fun switchToRepositories() {
        wrapper.setContent(repositoriesPanel)
        wrapper.repaint()
        inLoginState = false
        updateSelectedUrl()
    }

    override fun getView() = wrapper

    override fun doValidateAll(): List<ValidationInfo> =
        (wrapper.targetComponent as? DialogPanel)?.validationsOnApply?.values?.flatten()?.mapNotNull {
            it.validate()
        } ?: emptyList()

    override fun doClone(checkoutListener: CheckoutProvider.Listener) {
        val parent = Paths.get(directoryField.text).toAbsolutePath().parent
        val destinationValidation = CloneDvcsValidationUtils.createDestination(parent.toString())
        if (destinationValidation != null) {
            LOG.error(GiteaBundle.message("clone.dialog.error.unable.to.create.destination.directory"),
                destinationValidation.message)
            GiteaNotifications.showError(project,
                GiteaNotificationIdsHolder.CLONE_UNABLE_TO_CREATE_DESTINATION_DIR,
                GiteaBundle.message("clone.dialog.clone.failed"),
                GiteaBundle.message("clone.dialog.error.unable.to.find.destination.directory"))
            return
        }

        val lfs = LocalFileSystem.getInstance()
        var destinationParent = lfs.findFileByIoFile(parent.toFile())
        if (destinationParent == null) {
            destinationParent = lfs.refreshAndFindFileByIoFile(parent.toFile())
        }
        if (destinationParent == null) {
            LOG.error(GiteaBundle.message("clone.dialog.error.destination.not.exist"))
            GiteaNotifications.showError(project,
                GiteaNotificationIdsHolder.CLONE_UNABLE_TO_FIND_DESTINATION,
                GiteaBundle.message("clone.dialog.clone.failed"),
                GiteaBundle.message("clone.dialog.error.unable.to.find.destination.directory"))
            return
        }
        val directoryName = Paths.get(directoryField.text).fileName.toString()
        val parentDirectory = parent.toAbsolutePath().toString()

        GitCheckoutProvider.clone(project, Git.getInstance(), checkoutListener, destinationParent, selectedUrl, directoryName, parentDirectory)
    }

    override fun onComponentSelected() {
        dialogStateListener.onOkActionNameChanged(message("clone.button"))
        updateSelectedUrl()

        val focusManager = IdeFocusManager.getInstance(project)
        getPreferredFocusedComponent()?.let { focusManager.requestFocus(it, true) }
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return searchField
    }

    private fun updateSelectedUrl() {
        repositoryList.emptyText.clear()
        if (inLoginState) {
            selectedUrl = null
            return
        }
        val giteaRepoPath = getGiteaRepoPath(searchField.text)
        if (giteaRepoPath != null) {
            selectedUrl = giteaGitHelper.getRemoteUrl(giteaRepoPath.serverPath,
                giteaRepoPath.repositoryPath.owner,
                giteaRepoPath.repositoryPath.repository)
            repositoryList.emptyText.appendText(GiteaBundle.message("clone.dialog.repository.url.text", selectedUrl!!))
            return
        }
        val selectedValue = repositoryList.selectedValue
        if (selectedValue is GiteaRepositoryListItem.Repo) {
            selectedUrl = giteaGitHelper.getRemoteUrl(selectedValue.account.server,
                selectedValue.repo.owner.login,
                selectedValue.repo.name)
            return
        }
        selectedUrl = null
    }


    private fun getGiteaRepoPath(searchText: String): GiteaRepositoryCoordinates? {
        val url = searchText
            .trim()
            .removePrefix("git clone")
            .removeSuffix(".git")
            .trim()

        try {
            val serverPath = GiteaServerPath.from(url)

            val githubFullPath = GiteaUrlUtil.getUserAndRepositoryFromRemoteUrl(url) ?: return null
            return GiteaRepositoryCoordinates(serverPath, githubFullPath)
        }
        catch (e: Throwable) {
            return null
        }
    }

    private fun onSelectedUrlChanged() {
        val urlSelected = selectedUrl != null
        dialogStateListener.onOkActionEnabled(urlSelected)
        if (urlSelected) {
            val path = StringUtil.trimEnd(ClonePathProvider.relativeDirectoryPathForVcsUrl(project, selectedUrl!!), GitUtil.DOT_GIT)
            cloneDirectoryChildHandle.trySetChildPath(path)
        }
    }

    private fun createAccountsModel(): ListModel<GiteaAccount> {
        val model = CollectionListModel<GiteaAccount>()
        cs.launch(Dispatchers.Main.immediate) {
            accountManager.accountsState
                .map { it.filter(::isAccountHandled).toSet() }
                .collectLatest { accounts ->
                    val currentAccounts = model.items
                    accounts.forEach {
                        if (!currentAccounts.contains(it)) {
                            model.add(it)
                            async {
                                accountManager.getCredentialsFlow(it).collect { _ ->
                                    model.contentsChanged(it)
                                }
                            }
                        }
                    }

                    currentAccounts.forEach {
                        if (!accounts.contains(it)) {
                            model.remove(it)
                        }
                    }
                }
        }
        return model
    }

    private inner class AccountsPopupConfig : CompactAccountsPanelFactory.PopupConfig<GiteaAccount> {
        override val avatarSize: Int = VcsCloneDialogUiSpec.Components.popupMenuAvatarSize

        override fun createActions(): Collection<AccountMenuItem.Action> = createAccountMenuLoginActions(null)
    }

    protected abstract fun createAccountMenuLoginActions(account: GiteaAccount?): Collection<AccountMenuItem.Action>

    private fun createFocusFilterFieldAction(searchField: SearchTextField) {
        val action = DumbAwareAction.create {
            val focusManager = IdeFocusManager.getInstance(project)
            if (focusManager.getFocusedDescendantFor(repositoriesPanel) != null) {
                focusManager.requestFocus(searchField, true)
            }
        }
        val shortcuts = KeymapUtil.getActiveKeymapShortcuts(IdeActions.ACTION_FIND)
        action.registerCustomShortcutSet(shortcuts, repositoriesPanel, this)
    }

    companion object {
        internal val <E> ListModel<E>.items
            get() = Iterable {
                object : Iterator<E> {
                    private var idx = -1

                    override fun hasNext(): Boolean = idx < size - 1

                    override fun next(): E {
                        idx++
                        return getElementAt(idx)
                    }
                }
            }

        internal val <E> ListModel<E>.itemsSet
            get() = items.toSet()
    }
}