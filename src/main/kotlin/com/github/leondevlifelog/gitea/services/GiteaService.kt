/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.github.leondevlifelog.gitea.GiteaBundle
import com.github.leondevlifelog.gitea.api.GiteaApi

/**
 *
 * @author Leon
 */
@Service(Service.Level.PROJECT)
class GiteaService(private val project: Project) {
    fun getGiteaApi(baseUrl:String,token:String): GiteaApi {
        return GiteaApi(baseUrl,token)
    }
}
