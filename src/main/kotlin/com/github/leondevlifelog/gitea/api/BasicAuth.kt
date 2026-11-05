/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.api

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Basic Auth
 * @param username username
 * @param password password
 * @author Leon
 */
class BasicAuth(private val username: String, private val password: String) : Interceptor {
    private val credential = Credentials.basic(username, password)
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder().header(AccessTokenAuth.AUTHORIZATION, credential).build()
        return chain.proceed(newRequest)
    }
}
