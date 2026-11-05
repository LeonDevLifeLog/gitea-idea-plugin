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
import java.net.URISyntaxException


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
                val instanceUrl = URI(url).toURL()
                if (instanceUrl.protocol != URLUtil.HTTP_PROTOCOL && instanceUrl.protocol != URLUtil.HTTPS_PROTOCOL) {
                    throw GiteaParseException()
                }
                return GiteaServerPath(
                    instanceUrl.protocol == URLUtil.HTTP_PROTOCOL,
                    instanceUrl.host,
                    instanceUrl.port,
                    instanceUrl.path.trim('/').takeIf { it.isNotEmpty() }?.let { "/$it" },
                )
            } catch (e: MalformedURLException) {
                throw GiteaParseException()
            } catch (e: URISyntaxException) {
                throw GiteaParseException()
            } catch (e: IllegalArgumentException) {
                throw GiteaParseException()
            }
        }

        /**
         * Creates a server path from an HTTP(S) Git remote URL while preserving an optional
         * Gitea installation path prefix and removing the owner/repository suffix.
         */
        @Throws(GiteaParseException::class)
        @JvmStatic
        fun fromRemoteUrl(url: String): GiteaServerPath {
            try {
                val remoteUrl = URI(url).toURL()
                if (remoteUrl.protocol != URLUtil.HTTP_PROTOCOL && remoteUrl.protocol != URLUtil.HTTPS_PROTOCOL) {
                    throw GiteaParseException()
                }
                val pathSegments = remoteUrl.path.trim('/').split('/').filter { it.isNotEmpty() }
                if (pathSegments.size < 2) throw GiteaParseException()

                val serverPath = pathSegments.dropLast(2).takeIf { it.isNotEmpty() }?.joinToString("/", prefix = "/")
                return GiteaServerPath(
                    remoteUrl.protocol == URLUtil.HTTP_PROTOCOL,
                    remoteUrl.host,
                    remoteUrl.port,
                    serverPath,
                )
            } catch (e: MalformedURLException) {
                throw GiteaParseException()
            } catch (e: URISyntaxException) {
                throw GiteaParseException()
            } catch (e: IllegalArgumentException) {
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

    fun toSshCloneUrl(user: String, repo: String, sshPort: Int): String {
        return "ssh://git@${myHost}:$sshPort/$user/$repo.git"
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
        if (!myHost.equals(other.myHost, ignoreCase = true)) return false
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
        if (!myHost.equals(other.myHost, ignoreCase = true)) return false
        if (myPort != other.myPort) return false
        if (myPath != other.myPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = myUseHttp.hashCode()
        result = 31 * result + myHost.lowercase().hashCode()
        result = 31 * result + myPort
        result = 31 * result + myPath.hashCode()
        return result
    }

}
