package com.github.leondevlifelog.gitea.ui.clone

import com.github.leondevlifelog.gitea.authentication.accounts.GiteaAccount
import com.github.leondevlifelog.gitea.authentication.accounts.GiteaServerPath
import junit.framework.TestCase
import org.gitnex.tea4j.v2.models.Repository
import org.gitnex.tea4j.v2.models.User
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class GiteaCloneDialogRepositoryListModelTest : TestCase() {

    private val account = GiteaAccount("user", GiteaServerPath.from("https://gitea.example"))
    private val user = User().apply { login = "user" }

    fun testAddAndClearUseInclusiveEventIndexes() {
        val model = GiteaCloneDialogRepositoryListModel()
        val events = model.recordEvents()
        val repository = Repository().apply {
            name = "repository"
            fullName = "user/repository"
            owner = user
        }

        model.addRepositories(account, user, listOf(repository))
        model.clear(account)

        assertEquals(listOf(Event(ListDataEvent.INTERVAL_ADDED, 0, 0), Event(ListDataEvent.INTERVAL_REMOVED, 0, 0)), events)
    }

    fun testErrorInsertionReportsOneAddedElement() {
        val model = GiteaCloneDialogRepositoryListModel()
        val events = model.recordEvents()

        model.setError(account, IllegalStateException("failed"))

        assertEquals(listOf(Event(ListDataEvent.INTERVAL_ADDED, 0, 0)), events)
        assertEquals(1, model.size)
    }

    private fun GiteaCloneDialogRepositoryListModel.recordEvents(): MutableList<Event> {
        val events = mutableListOf<Event>()
        addListDataListener(object : ListDataListener {
            override fun intervalAdded(e: ListDataEvent) {
                events += Event(e.type, e.index0, e.index1)
            }

            override fun intervalRemoved(e: ListDataEvent) {
                events += Event(e.type, e.index0, e.index1)
            }

            override fun contentsChanged(e: ListDataEvent) {
                events += Event(e.type, e.index0, e.index1)
            }
        })
        return events
    }

    private data class Event(val type: Int, val index0: Int, val index1: Int)
}
