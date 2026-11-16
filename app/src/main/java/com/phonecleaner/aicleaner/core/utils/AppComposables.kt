package com.phonecleaner.aicleaner.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.phonecleaner.aicleaner.core.common.postAnalytic

@Composable
fun PostAnalytic(text: String) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        context.postAnalytic(text)
    }
}


