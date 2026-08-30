package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    isPinRequired: Boolean,
    onTogglePin: (Boolean) -> Unit,
    onExportBackup: () -> Unit,
    onPurgeShortTerm: () -> Unit
) {
    var showExportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        item {
            Column {
                Text("🔐 Privacy & Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("You own your memories. End-to-end local persistence.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Security & Privacy Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Security & Access Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric / PIN App Lock", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Require PIN (1234) before unlocking memory vault", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isPinRequired,
                            onCheckedChange = { onTogglePin(it) },
                            modifier = Modifier.testTag("pin_lock_switch")
                        )
                    }
                }
            }
        }

        // Data Ownership & Backup
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Data Ownership & Export", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    ListItem(
                        headlineContent = { Text("Export QuickRemind_Backup.json") },
                        supportingContent = { Text("Complete export of all memories, tasks, notes, and projects") },
                        leadingContent = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CyberCyan) },
                        modifier = Modifier.clickable {
                            onExportBackup()
                            showExportDialog = true
                        }.testTag("export_backup_item")
                    )

                    Divider(color = MaterialTheme.colorScheme.surface)

                    ListItem(
                        headlineContent = { Text("Purge Expired Short-term Memories") },
                        supportingContent = { Text("Clear non-permanent memories older than 7 days") },
                        leadingContent = { Icon(Icons.Default.CleaningServices, contentDescription = null, tint = CyberAmber) },
                        modifier = Modifier.clickable { onPurgeShortTerm() }
                    )
                }
            }
        }

        // Philosophy & Vision
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberViolet.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Quick Remind Philosophy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyberVioletLight)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "“If you tell Quick Remind something once, it should help you remember it later.”\n\nBuilt for makers, engineers, students, and ambitious thinkers who need zero-latency memory retrieval and context-aware natural language recall.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup Ready") },
            text = { Text("Your complete Quick Remind database has been packaged into JSON format and synced locally.") },
            confirmButton = {
                Button(onClick = { showExportDialog = false }) { Text("Done") }
            }
        )
    }
}
