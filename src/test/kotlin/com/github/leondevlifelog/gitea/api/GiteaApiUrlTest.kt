package com.github.leondevlifelog.gitea.api

import junit.framework.TestCase

class GiteaApiUrlTest : TestCase() {

    fun testNormalizeBaseUrlWithoutTrailingSlash() {
        assertEquals("https://gitea.example/api/v1/", normalizeGiteaApiBaseUrl("https://gitea.example"))
    }

    fun testNormalizeBaseUrlWithServerPathAndTrailingSlash() {
        assertEquals(
            "https://gitea.example/code/api/v1/",
            normalizeGiteaApiBaseUrl("https://gitea.example/code/"),
        )
    }
}
