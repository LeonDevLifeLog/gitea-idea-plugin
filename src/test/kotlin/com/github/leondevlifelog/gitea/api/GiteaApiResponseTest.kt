package com.github.leondevlifelog.gitea.api

import com.github.leondevlifelog.gitea.exception.GiteaAuthenticationException
import junit.framework.TestCase
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException

class GiteaApiResponseTest : TestCase() {

    private lateinit var server: MockWebServer
    private lateinit var api: TestApi

    override fun setUp() {
        super.setUp()
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TestApi::class.java)
    }

    override fun tearDown() {
        server.shutdown()
        super.tearDown()
    }

    fun testSuccessfulResponseReturnsBody() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{\"value\":\"ok\"}"))

        assertEquals("ok", api.getPayload().executeBody().value)
    }

    fun testUnauthorizedResponseThrowsAuthenticationException() {
        server.enqueue(MockResponse().setResponseCode(401))

        assertFailsWith<GiteaAuthenticationException> { api.getPayload().executeBody() }
    }

    fun testServerErrorThrowsIOException() {
        server.enqueue(MockResponse().setResponseCode(500))

        assertFailsWith<IOException> { api.getPayload().executeBody() }
    }

    fun testEmptySuccessfulResponseThrowsIOException() {
        server.enqueue(MockResponse().setResponseCode(200))

        assertFailsWith<IOException> { api.getPayload().executeBody() }
    }

    private inline fun <reified T : Throwable> assertFailsWith(action: () -> Unit) {
        try {
            action()
            fail("Expected ${T::class.java.simpleName}")
        } catch (error: Throwable) {
            if (error !is T) throw error
        }
    }

    private interface TestApi {
        @GET("payload")
        fun getPayload(): Call<Payload>
    }

    private data class Payload(val value: String)
}
