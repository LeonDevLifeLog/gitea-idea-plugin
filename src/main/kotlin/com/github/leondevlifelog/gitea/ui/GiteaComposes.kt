/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.leondevlifelog.gitea.GiteaBundle

/**
 * Welcome panel
 * @author Leon
 */
@Composable
fun Welcome() {
    Surface(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(), color = Color.Transparent, contentColor = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(GiteaBundle.message("coming.soon"))
            }
        }
    }
}