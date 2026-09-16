package com.protosync.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                "Proton Sync",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Open-source, end-to-end encrypted synchronization between your Proton account and your Android device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle("Functions")
            BulletItem("Two-way sync of Proton Contacts to the Android Contacts provider.")
            BulletItem("One-way sync of Proton Calendar events to the Android Calendar provider.")
            BulletItem("Full end-to-end decryption on device: user, address and calendar keys are unlocked locally, so plaintext data never leaves your device unencrypted.")
            BulletItem("Local-only storage: decrypted data is written only to the system Contacts/Calendar providers on this device.")

            Spacer(Modifier.height(24.dp))
            SectionTitle("Open Source")
            Text(
                "This app is fully open source (GPL-3.0). You are welcome to inspect, audit and contribute to the code.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Source code:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinkText(
                text = "https://github.com/carlostkd/ProtonSync",
                onClick = { uriHandler.openUri("https://github.com/carlostkd/ProtonSync") }
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle("Disclaimer")
            Text(
                "This is NOT an official Proton app. It is an independent community project and is not affiliated with, endorsed by, or supported by Proton AG. Use it at your own risk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun BulletItem(text: String) {
    Text(
        "•  $text",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun LinkText(text: String, onClick: () -> Unit) {
    val annotated = rememberAnnotatedStringLabel(text)
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
        color = Color(0xFF6B9BD1),
        modifier = Modifier
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun rememberAnnotatedStringLabel(text: String) = text
