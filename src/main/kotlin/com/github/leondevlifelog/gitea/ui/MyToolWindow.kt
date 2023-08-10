package com.github.leondevlifelog.gitea.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Welcome() {
    Surface(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(), color = Color.Transparent, contentColor = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                var count by remember { mutableStateOf(0) }
                Text("Count: $count")
                Spacer(modifier = Modifier.height(8.dp))
                Text(modifier = Modifier.onClick { count += 1 }, text = "Hello JetBrains!")
            }
        }
    }
}