package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, category: MemoryCategory, importance: MemoryImportance, isPermanent: Boolean, tags: List<String>, topic: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MemoryCategory.GENERAL) }
    var importance by remember { mutableStateOf(MemoryImportance.NORMAL) }
    var isPermanent by remember { mutableStateOf(true) }
    var tagsText by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Store in Memory", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Memory Title / Subject") },
                    placeholder = { Text("e.g. Flight Controller MCU") },
                    modifier = Modifier.fillMaxWidth().testTag("memory_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("What should Quick Remind remember?") },
                    placeholder = { Text("e.g. Uses STM32F405 microcontroller with SPI MPU6000...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("memory_content_input"),
                    maxLines = 5
                )

                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(MemoryCategory.PROJECT, MemoryCategory.IDEA, MemoryCategory.PERSONAL, MemoryCategory.WORK).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Importance Level", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MemoryImportance.values().forEach { imp ->
                        FilterChip(
                            selected = importance == imp,
                            onClick = { importance = imp },
                            label = { Text(imp.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when(imp) {
                                    MemoryImportance.HIGH -> MaterialTheme.colorScheme.error
                                    MemoryImportance.NORMAL -> MaterialTheme.colorScheme.primary
                                    MemoryImportance.LOW -> MaterialTheme.colorScheme.secondary
                                }
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Remember Permanently", style = MaterialTheme.typography.bodyMedium)
                        Text("Keep across all sessions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isPermanent, onCheckedChange = { isPermanent = it })
                }

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Connected Project / Topic (Optional)") },
                    placeholder = { Text("e.g. Flight Controller") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("stm32, hardware, embedded") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onConfirm(title, content, category, importance, isPermanent, tags, topic.ifBlank { null })
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_memory_button")
            ) {
                Text("Remember")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, notes: String, dueMillis: Long, priority: ReminderPriority, recurrence: RecurrenceType, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var hoursAhead by remember { mutableStateOf(2) }
    var priority by remember { mutableStateOf(ReminderPriority.MEDIUM) }
    var recurrence by remember { mutableStateOf(RecurrenceType.NONE) }
    var category by remember { mutableStateOf("General") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Set Smart Reminder", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title") },
                    placeholder = { Text("e.g. Call mentor about C-DAC") },
                    modifier = Modifier.fillMaxWidth().testTag("reminder_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Details / Context") },
                    placeholder = { Text("e.g. Review milestone 2 presentation") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text("When to remind?", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1 to "1 hr", 2 to "2 hrs", 6 to "Evening", 24 to "Tomorrow").forEach { (hrs, label) ->
                        FilterChip(
                            selected = hoursAhead == hrs,
                            onClick = { hoursAhead = hrs },
                            label = { Text(label) }
                        )
                    }
                }

                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReminderPriority.values().forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio.name) }
                        )
                    }
                }

                Text("Recurrence", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(RecurrenceType.NONE, RecurrenceType.DAILY, RecurrenceType.WEEKLY).forEach { rec ->
                        FilterChip(
                            selected = recurrence == rec,
                            onClick = { recurrence = rec },
                            label = { Text(rec.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val due = System.currentTimeMillis() + (hoursAhead * 3600000L)
                        onConfirm(title, notes, due, priority, recurrence, category)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_reminder_button")
            ) {
                Text("Set Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, category: String, deadlineMillis: Long?, tasks: List<String>, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Engineering") }
    var tasksInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#6366F1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Create New Project", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    placeholder = { Text("e.g. Flight Controller") },
                    modifier = Modifier.fillMaxWidth().testTag("project_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Goals") },
                    placeholder = { Text("Custom autonomous drone flight controller...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = tasksInput,
                    onValueChange = { tasksInput = it },
                    label = { Text("Initial Subtasks (one per line)") },
                    placeholder = { Text("PCB design\nSensor integration\nFirmware coding") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 5
                )

                Text("Theme Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("#6366F1", "#10B981", "#F59E0B", "#EC4899", "#06B6D4").forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(hex)),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val taskList = tasksInput.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                        val deadline = System.currentTimeMillis() + (86400000L * 21)
                        onConfirm(name, description, category, deadline, taskList, selectedColor)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_project_button")
            ) {
                Text("Start Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, category: String, tags: List<String>, colorHex: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Idea") }
    var tagsInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.StickyNote2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Quick Note / Idea", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    placeholder = { Text("e.g. AI based agriculture drone") },
                    modifier = Modifier.fillMaxWidth().testTag("note_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note Content") },
                    placeholder = { Text("Write your thoughts or quick snippet...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 6
                )

                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("idea, agriculture, future") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onConfirm(title, content, category, tags, "#1E293B")
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text("Save Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
