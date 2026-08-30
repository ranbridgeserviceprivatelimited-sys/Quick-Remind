package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.ActivityLogItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimelineScreen(
    logs: List<ActivityLogItem>,
    onAskTimeline: (String) -> Unit
) {
    var queryDateText by remember { mutableStateOf("") }

    // Group logs by Date string (e.g. "AUG 26, 2026")
    val groupedLogs = remember(logs) {
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        logs.groupBy { format.format(Date(it.timestampMillis)).uppercase() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        item {
            Column {
                Text("🧭 Life Timeline & History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Your personal memory chronicle of past activities and decisions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Timeline Natural Language Query Search Box
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = queryDateText,
                        onValueChange = { queryDateText = it },
                        placeholder = { Text("Ask: \"What was I doing yesterday?\"") },
                        modifier = Modifier.weight(1f).testTag("timeline_search_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            if (queryDateText.isNotBlank()) {
                                onAskTimeline(queryDateText)
                                queryDateText = ""
                            }
                        },
                        enabled = queryDateText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Ask", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (groupedLogs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No timeline history recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            groupedLogs.forEach { (dateHeader, dayLogs) ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberViolet.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberVioletLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }

                items(dayLogs) { logItem ->
                    TimelineItemRow(logItem)
                }
            }
        }
    }
}

@Composable
fun TimelineItemRow(logItem: ActivityLogItem) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(Date(logItem.timestampMillis))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical line & node circle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (logItem.actionType) {
                            "MEMORY_ADDED" -> CyberCyan
                            "TASK_DONE" -> CyberEmerald
                            "REMINDER_SET" -> CyberAmber
                            "PROJECT_CREATED" -> CyberViolet
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        Spacer(Modifier.width(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.weight(1f).testTag("timeline_log_${logItem.id}")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = logItem.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (logItem.details.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = logItem.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
