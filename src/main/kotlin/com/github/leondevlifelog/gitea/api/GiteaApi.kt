/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.api

import com.intellij.util.net.ssl.CertificateManager
import okhttp3.OkHttpClient
import org.gitnex.tea4j.v2.apis.IssueApi
import org.gitnex.tea4j.v2.apis.RepositoryApi
import org.gitnex.tea4j.v2.apis.UserApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Gitea Api
 * @author Leon
 */
class GiteaApi {

    private var retrofit: Retrofit

    /**
     * @param baseUrl Base Url
     * @param token Access Token
     * @see AccessTokenAuth
     */
    constructor(baseUrl: String, token: String, connectionTimeoutMs: Long = DEFAULT_CONNECTION_TIMEOUT_MS) {
        retrofit = Retrofit.Builder().baseUrl(normalizeGiteaApiBaseUrl(baseUrl)).client(
            httpClientBuilder(connectionTimeoutMs).sslSocketFactory(
                CertificateManager.getInstance().sslContext.socketFactory, CertificateManager.getInstance().trustManager
            ).addInterceptor(AccessTokenAuth(token)).build()
        ).addConverterFactory(GsonConverterFactory.create()).build()
    }

    /**
     * @param baseUrl Base Url
     * @param username Username
     * @param password Password
     * @see BasicAuth
     */
    constructor(
        baseUrl: String,
        username: String,
        password: String,
        connectionTimeoutMs: Long = DEFAULT_CONNECTION_TIMEOUT_MS,
    ) {
        retrofit = Retrofit.Builder().baseUrl(normalizeGiteaApiBaseUrl(baseUrl)).client(
            httpClientBuilder(connectionTimeoutMs).sslSocketFactory(
                CertificateManager.getInstance().sslContext.socketFactory, CertificateManager.getInstance().trustManager
            ).addInterceptor(BasicAuth(username, password)).build()
        ).addConverterFactory(GsonConverterFactory.create()).build()
    }

    fun getIssueApi(): IssueApi {
        return retrofit.create(IssueApi::class.java)
    }

    fun getUserApi(): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    fun getRepoApi(): RepositoryApi {
        return retrofit.create(RepositoryApi::class.java)
    }

    private companion object {
        const val DEFAULT_CONNECTION_TIMEOUT_MS = 5_000L

        fun httpClientBuilder(timeoutMs: Long): OkHttpClient.Builder {
            // OkHttp a 0-t végtelen időtúllépésként kezeli
            val timeout = if (timeoutMs > 0) timeoutMs else DEFAULT_CONNECTION_TIMEOUT_MS
            return OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
        }
    }
}
