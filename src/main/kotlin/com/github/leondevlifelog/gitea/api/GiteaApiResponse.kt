package com.github.leondevlifelog.gitea.api

import com.github.leondevlifelog.gitea.exception.GiteaAuthenticationException
import retrofit2.Call
import java.io.IOException

internal fun <T : Any> Call<T>.executeBody(): T {
    val response = execute()
    if (!response.isSuccessful) {
        if (response.code() == 401 || response.code() == 403) {
            throw GiteaAuthenticationException("Authentication failed with HTTP ${response.code()}")
        }
        throw IOException("Gitea API request failed with HTTP ${response.code()}")
    }
    return response.body() ?: throw IOException("Gitea API returned an empty response body")
}

internal fun normalizeGiteaApiBaseUrl(baseUrl: String): String = "${baseUrl.trimEnd('/')}/api/v1/"
