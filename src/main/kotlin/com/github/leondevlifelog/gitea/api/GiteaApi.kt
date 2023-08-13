/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.api

import okhttp3.OkHttpClient
import org.gitnex.tea4j.v2.apis.IssueApi
import org.gitnex.tea4j.v2.apis.RepositoryApi
import org.gitnex.tea4j.v2.apis.UserApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    constructor(baseUrl: String, token: String) {
        retrofit = Retrofit.Builder().baseUrl("$baseUrl/api/v1/")
            .client(OkHttpClient.Builder().addInterceptor(AccessTokenAuth(token)).build())
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    /**
     * @param baseUrl Base Url
     * @param username Username
     * @param password Password
     * @see BasicAuth
     */
    constructor(baseUrl: String, username: String, password: String) {
        retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .client(OkHttpClient.Builder().addInterceptor(BasicAuth(username, password)).build())
            .addConverterFactory(GsonConverterFactory.create()).build()
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
}