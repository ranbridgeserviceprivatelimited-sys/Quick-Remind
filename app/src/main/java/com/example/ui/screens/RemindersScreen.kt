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
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RemindersScreen(
    allReminders: List<ReminderItem>,
    searchQuery: String,
    onToggleReminder: (ReminderItem) -> Unit,
    onSnoozeReminder: (ReminderItem, Int) -> Unit,
    onDeleteReminder: (ReminderItem) -> Unit,
    onAddReminderClick: () -> Unit,
    onNaturalLanguageExecute: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("ACTIVE") } // "ACTIVE", "OVERDUE", "RECURRING", "COMPLETED"
    var quickNlText by remember { mutableStateOf("") }

    val now = System.currentTimeMillis()

    val filteredList = allReminders.filter { item ->
        val matchesSearch = searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.notes.contains(searchQuery, ignoreCase = true)
        val matchesTab = when(selectedTab) {
            "ACTIVE" -> !item.isCompleted
            "OVERDUE" -> !item.isCompleted && item.dueDateMillis < now
            "RECURRING" -> item.recurrence != RecurrenceType.NONE
            "COMPLETED" -> item.isCompleted
            else -> true
        }
        matchesSearch && matchesTab
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminderClick,
                containerColor = CyberCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("fab_add_reminder")
            ) {
                Icon(Icons.Default.AddAlarm, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Natural Language Quick Input
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = quickNlText,
                        onValueChange = { quickNlText = it },
                        placeholder = { Text("Smart Remind: \"Call sir tomorrow 5 PM\"") },
                        modifier = Modifier.weight(1f).testTag("nl_reminder_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            if (quickNlText.isNotBlank()) {
                                onNaturalLanguageExecute(quickNlText)
                                quickNlText = ""
                            }
                        },
                        enabled = quickNlText.isNotBlank(),
                        modifier = Modifier.testTag("nl_reminder_submit")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = when(selectedTab) {
                    "ACTIVE" -> 0
                    "OVERDUE" -> 1
                    "RECURRING" -> 2
                    "COMPLETED" -> 3
                    else -> 0
                },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                edgePadding = 8.dp
            ) {
                val overdueCount = allReminders.count { !it.isCompleted && it.dueDateMillis < now }
                val activeCount = allReminders.count { !it.isCompleted }

                Tab(
                    selected = selectedTab == "ACTIVE",
                    onClick = { selectedTab = "ACTIVE" },
                    text = { Text("Active ($activeCount)") }
                )
                Tab(
                    selected = selectedTab == "OVERDUE",
                    onClick = { selectedTab = "OVERDUE" },
                    text = { Text("Overdue ($overdueCount)") }
                )
                Tab(
                    selected = selectedTab == "RECURRING",
                    onClick = { selectedTab = "RECURRING" },
                    text = { Text("Recurring") }
                )
                Tab(
                    selected = selectedTab == "COMPLETED",
                    onClick = { selectedTab = "COMPLETED" },
                    text = { Text("Completed") }
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No reminders in this view.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(filteredList) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            isOverdue = !reminder.isCompleted && reminder.dueDateMillis < now,
                            onToggle = { onToggleReminder(reminder) },
                            onSnooze = { onSnoozeReminder(reminder, 15) },
                            onDelete = { onDeleteReminder(reminder) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: ReminderItem,
    isOverdue: Boolean,
    onToggle: () -> Unit,
    onSnooze: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault())
    val dueStr = dateFormat.format(Date(reminder.dueDateMillis))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth().testTag("reminder_card_${reminder.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("reminder_check_${reminder.id}")
            )

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (reminder.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = reminder.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = null,
                            tint = if (isOverdue) CyberRose else CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = dueStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) CyberRose else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    if (reminder.recurrence != RecurrenceType.NONE) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                reminder.recurrence.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyberRose.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "OVERDUE",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberRose,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (!reminder.isCompleted) {
                    IconButton(onClick = onSnooze, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Snooze, contentDescription = "Snooze 15m", tint = CyberAmber, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
