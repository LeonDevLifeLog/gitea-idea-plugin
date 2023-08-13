/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Access Token Auth
 * @param token Access Token
 * @author Leon
 */
class AccessTokenAuth(private val token: String) : Interceptor {
    companion object {
        const val AUTHORIZATION = "Authorization"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder().addHeader(AUTHORIZATION, "token $token").build()
        return chain.proceed(newRequest)
    }
}