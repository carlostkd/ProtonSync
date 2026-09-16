package com.protosync.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val ProtonColors = darkColorScheme(
    primary = Color(0xFF6D4AFF),
    onPrimary = Color.White,
    secondary = Color(0xFF9B8CFF),
    background = Color(0xFF16121C),
    surface = Color(0xFF1E1826),
    onBackground = Color(0xFFE6E1F0),
    onSurface = Color(0xFFE6E1F0),
)

@Composable
fun ProtonSyncTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ProtonColors,
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = ProtonColors.background,
                contentColor = ProtonColors.onBackground,
                content = content,
            )
        },
    )
}