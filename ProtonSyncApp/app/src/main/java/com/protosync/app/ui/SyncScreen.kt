package com.protosync.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncScreen(
    lastContactSyncTime: Long,
    lastCalendarSyncTime: Long,
    contactsSyncEnabled: Boolean,
    calendarSyncEnabled: Boolean,
    isSyncing: Boolean,
    message: String?,
    isError: Boolean,
    onSyncNow: () -> Unit,
    onToggleContacts: () -> Unit,
    onToggleCalendar: () -> Unit,
    onSignOut: () -> Unit,
    onOpenAbout: () -> Unit = {},
    onOpenSyncLogs: () -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Proton Sync",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SyncToggleRow("Contacts", contactsSyncEnabled, onToggleContacts, lastContactSyncTime)
        Spacer(Modifier.height(16.dp))
        SyncToggleRow("Calendar", calendarSyncEnabled, onToggleCalendar, lastCalendarSyncTime)

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onSyncNow,
            enabled = !isSyncing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.size(8.dp))
                Text("Syncing…")
            } else {
                Text("Sync Now")
            }
        }

        if (message != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Sign Out")
        }

        Spacer(Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onOpenAbout, modifier = Modifier.weight(1f)) {
                Text("About")
            }
            TextButton(onClick = onOpenSyncLogs, modifier = Modifier.weight(1f)) {
                Text("Sync Logs")
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Enjoying the app? Your donation helps keep development going!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://donate.stripe.com/8wM6pe9DD99xgAofYZ"))
                    context.startActivity(intent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Donate")
        }
    }
}

@Composable
private fun SyncToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
    lastSyncTime: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val timeText = if (lastSyncTime > 0) {
                "Last sync: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastSyncTime))}"
            } else "Not yet synced"
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = enabled, onCheckedChange = { onToggle() })
    }
}