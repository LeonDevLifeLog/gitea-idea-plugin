/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.services

import com.github.leondevlifelog.gitea.api.GiteaApi
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "GiteaSettings",
    storages = [Storage(value = "gitea.xml")],
    category = SettingsCategory.TOOLS
)
class GiteaSettings : PersistentStateComponent<GiteaSettings.State> {
    private var myState = State()
    fun getGiteaApi(baseUrl:String,token:String): GiteaApi {
        return GiteaApi(baseUrl,token)
    }
    class State {
        var OPEN_IN_BROWSER_GIST = true
        var COPY_URL_GIST = false

        // "Secret" in UI, "Public" in API. "Private" here to preserve user settings after refactoring
        var PRIVATE_GIST = true
        var CONNECTION_TIMEOUT = 5000
        var CLONE_GIT_USING_SSH = false
    }

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        @JvmStatic
        fun getInstance(): GiteaSettings {
            return ApplicationManager.getApplication().getService(GiteaSettings::class.java)
        }
    }

    fun getConnectionTimeout(): Int {
        return myState.CONNECTION_TIMEOUT
    }

    fun setConnectionTimeout(timeout: Int) {
        myState.CONNECTION_TIMEOUT = timeout
    }

    fun isOpenInBrowserGist(): Boolean {
        return myState.OPEN_IN_BROWSER_GIST
    }

    fun isCopyURLGist(): Boolean {
        return myState.COPY_URL_GIST
    }

    fun setCopyURLGist(copyLink: Boolean) {
        myState.COPY_URL_GIST = copyLink
    }

    fun isPrivateGist(): Boolean {
        return myState.PRIVATE_GIST
    }

    fun isCloneGitUsingSsh(): Boolean {
        return myState.CLONE_GIT_USING_SSH
    }

    fun setPrivateGist(secretGist: Boolean) {
        myState.PRIVATE_GIST = secretGist
    }

    fun setOpenInBrowserGist(openInBrowserGist: Boolean) {
        myState.OPEN_IN_BROWSER_GIST = openInBrowserGist
    }

    fun setCloneGitUsingSsh(value: Boolean) {
        myState.CLONE_GIT_USING_SSH = value
    }

}