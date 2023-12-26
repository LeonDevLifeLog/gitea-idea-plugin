/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.authentication.accounts

import com.github.leondevlifelog.gitea.exception.GiteaParseException
import com.intellij.collaboration.api.ServerPath
import com.intellij.util.io.URLUtil
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import org.apache.http.client.utils.URIBuilder
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.net.MalformedURLException
import java.net.URI
import java.net.URL


/**
 * Gitea server reference allowing to specify custom port and path to instance
 */
@Tag("server")
class GiteaServerPath(usHttp: Boolean, host: String, port: Int, path: String?) : ServerPath {
    constructor() : this(DEFAULT_SERVER.myUseHttp, DEFAULT_SERVER.myHost, DEFAULT_SERVER.myPort, DEFAULT_SERVER.myPath)

    companion object {
        @JvmStatic
        val DEFAULT_SERVER = GiteaServerPath(false, "localhost", -1, null)

        @Throws(GiteaParseException::class)
        @JvmStatic
        fun from(url: String): GiteaServerPath {
            try {
                val instanceUrl = URL(url)
                return GiteaServerPath(
                    instanceUrl.protocol == URLUtil.HTTP_PROTOCOL, instanceUrl.host, instanceUrl.port, instanceUrl.path
                )
            } catch (e: MalformedURLException) {
                throw GiteaParseException()
            }
        }
    }

    @Attribute("useHttp")
    private var myUseHttp: Boolean = usHttp

    @Attribute("host")
    private var myHost: String = host

    @Attribute("port")
    private var myPort: Int = port

    @Attribute("path")
    private var myPath: String? = path

    override fun toString(): String {
        return toURI().toString().trim('/')
    }

    override fun toURI(): URI {
        return URIBuilder().apply {
            scheme = getSchema()
            host = myHost
            port = myPort
            path = myPath
        }.build()
    }

    @NotNull
    fun getSchema(): String {
        return if (!myUseHttp) URLUtil.HTTPS_PROTOCOL else URLUtil.HTTP_PROTOCOL
    }

    @NotNull
    fun getHost(): String {
        return myHost
    }

    @NotNull
    fun getPort(): Int {
        return myPort
    }

    @Nullable
    fun getPath(): String? {
        return myPath
    }

    fun toSshCloneUrl(user: String, repo: String): String {
        return "ssh://git@${myHost}:$myPort/$user/$repo.git"
    }

    fun toHttpCloneUrl(user: String, repo: String): String {
        val instanceUrl = toString().trim('/')
        return "${instanceUrl}/$user/$repo.git"
    }

    fun toAccessTokenUrl(): String {
        val instanceUrl = toString().trim('/')
        return "$instanceUrl/user/settings/applications"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiteaServerPath

        if (myUseHttp != other.myUseHttp) return false
        if (myHost != other.myHost) return false
        if (myPort != other.myPort) return false
        if (myPath != other.myPath) return false

        return true
    }

    fun equals(other: Any?, ignoreProtocol: Boolean): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiteaServerPath

        if (!ignoreProtocol) {
            if (myUseHttp != other.myUseHttp) return false
        }
        if (myHost != other.myHost) return false
        if (myPort != other.myPort) return false
        if (myPath != other.myPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = myUseHttp.hashCode()
        result = 31 * result + myHost.hashCode()
        result = 31 * result + myPort
        result = 31 * result + myPath.hashCode()
        return result
    }

}
