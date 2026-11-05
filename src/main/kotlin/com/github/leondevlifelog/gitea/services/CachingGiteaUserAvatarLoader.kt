/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.services

import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.LowMemoryWatcher
import com.intellij.util.ImageLoader
import com.intellij.util.ui.ImageUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.future.future
import java.awt.Image
import java.awt.image.BufferedImage
import java.net.URI
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture

class CachingGiteaUserAvatarLoader : Disposable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val avatarCache = Caffeine.newBuilder().expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES))
        .build<String, CompletableFuture<Image?>>()

    init {
        LowMemoryWatcher.register({ avatarCache.invalidateAll() }, this)
    }

    fun requestAvatar(url: String): CompletableFuture<Image?> = avatarCache.get(url) {
        scope.future {
            ensureActive()
            loadAndDownscale(url, STORED_IMAGE_SIZE)
        }
    }

    private fun loadAndDownscale(url: String, maximumSize: Int): Image? {
        try {
            val loadedImage = ImageLoader.loadFromUrl(URI(url).toURL()) ?: return null
            return if (ImageUtil.getUserWidth(loadedImage) <= maximumSize && ImageUtil.getUserHeight(loadedImage) <= maximumSize) loadedImage
            else ImageLoader.scaleImage(loadedImage, maximumSize) as BufferedImage
        } catch (e: CancellationException) {
            throw e
        } catch (e: ProcessCanceledException) {
            return null
        } catch (e: Exception) {
            LOG.debug("Error loading Gitea avatar", e)
            return null
        }
    }

    override fun dispose() {
        scope.cancel()
    }

    companion object {
        private val LOG = logger<CachingGiteaUserAvatarLoader>()

        @JvmStatic
        fun getInstance(): CachingGiteaUserAvatarLoader = service()

        private const val MAXIMUM_ICON_SIZE = 40

        // store images at maximum used size with maximum reasonable scale to avoid upscaling (3 for system scale, 2 for user scale)
        private const val STORED_IMAGE_SIZE = MAXIMUM_ICON_SIZE * 6
    }
}
