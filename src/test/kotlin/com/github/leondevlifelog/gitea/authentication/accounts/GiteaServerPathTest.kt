package com.github.leondevlifelog.gitea.authentication.accounts

import junit.framework.TestCase

class GiteaServerPathTest : TestCase() {

    private val server = GiteaServerPath(true, "localhost", -1, "gitea")


    fun testGetSchema() {
        assertEquals("http", server.getSchema())
    }

    fun testGHost() {
        assertEquals("localhost", server.getHost())
    }


    fun testGPort() {
        assertEquals(-1, server.getPort())
    }


    fun testGPath() {
        assertEquals("gitea", server.getPath())
    }


    fun testToSshCloneUrl() {
        assertEquals("git@localhost:user/repo.git", server.toSshCloneUrl("user", "repo"))
    }


    fun testToHttpCloneUrl() {
        assertEquals("http://localhost/gitea/user/repo.git", server.toHttpCloneUrl("user", "repo"))
    }


    fun testToAccessTokenUrl() {
        assertEquals("http://localhost/gitea/user/settings/applications", server.toAccessTokenUrl())
    }
}
