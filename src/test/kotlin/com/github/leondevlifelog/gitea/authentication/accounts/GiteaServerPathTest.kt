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
        assertEquals("ssh://git@localhost:22/user/repo.git", server.toSshCloneUrl("user", "repo", 22))
    }


    fun testToHttpCloneUrl() {
        assertEquals("http://localhost/gitea/user/repo.git", server.toHttpCloneUrl("user", "repo"))
    }


    fun testToAccessTokenUrl() {
        assertEquals("http://localhost/gitea/user/settings/applications", server.toAccessTokenUrl())
    }

    fun testFromRemoteUrlWithoutServerPath() {
        val parsed = GiteaServerPath.fromRemoteUrl("https://gitea.example/owner/repository.git")

        assertEquals("https", parsed.getSchema())
        assertEquals("gitea.example", parsed.getHost())
        assertNull(parsed.getPath())
    }

    fun testFromRemoteUrlKeepsServerPathAndPort() {
        val parsed = GiteaServerPath.fromRemoteUrl("http://gitea.example:3000/code/owner/repository.git")

        assertEquals("http", parsed.getSchema())
        assertEquals(3000, parsed.getPort())
        assertEquals("/code", parsed.getPath())
        assertEquals("http://gitea.example:3000/code", parsed.toString())
    }

    fun testFromRejectsNonHttpProtocol() {
        try {
            GiteaServerPath.from("ftp://gitea.example")
            fail("Expected GiteaParseException")
        } catch (_: com.github.leondevlifelog.gitea.exception.GiteaParseException) {
        }
    }

    fun testFromRejectsEmptyUrl() {
        try {
            GiteaServerPath.from("")
            fail("Expected GiteaParseException")
        } catch (_: com.github.leondevlifelog.gitea.exception.GiteaParseException) {
        }
    }

    fun testFromRejectsRelativeUrl() {
        try {
            GiteaServerPath.from("gitea.example")
            fail("Expected GiteaParseException")
        } catch (_: com.github.leondevlifelog.gitea.exception.GiteaParseException) {
        }
    }

    fun testHostComparisonIsCaseInsensitive() {
        val lower = GiteaServerPath.from("https://gitea.example/code")
        val upper = GiteaServerPath.from("https://GITEA.EXAMPLE/code/")

        assertEquals(lower, upper)
        assertEquals(lower.hashCode(), upper.hashCode())
    }
}
