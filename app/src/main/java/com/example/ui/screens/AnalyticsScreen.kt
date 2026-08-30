package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    memories: List<MemoryItem>,
    reminders: List<ReminderItem>,
    projects: List<ProjectItem>,
    tasks: List<ProjectTaskItem>
) {
    val totalReminders = reminders.size
    val completedReminders = reminders.count { it.isCompleted }
    val completionRate = if (totalReminders > 0) (completedReminders.toFloat() / totalReminders.toFloat() * 100).toInt() else 100

    val totalSubtasks = tasks.size
    val completedSubtasks = tasks.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Productivity & Memory Analytics",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "AI insights into your execution patterns and memory retention",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Completion Rate Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Overall Task Completion Rate",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "$completionRate%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SleekNavyPrimary
                        )
                        Text(
                            "$completedReminders of $totalReminders reminders cleared",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { completionRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = SleekNavyPrimary,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // AI Pattern Detection Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = SleekNavyPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "AI Pattern Observations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekNavyPrimary
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("• You are fastest at completing hardware engineering & PCB tasks.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("• Highest cognitive activity occurs between 9:00 AM - 1:00 PM.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("• Connected memories indexed under 'Flight Controller' and 'STM32'.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Metrics breakdown
        item {
            Text(
                "KNOWLEDGE BASE METRICS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextMuted
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsStatPill(
                    modifier = Modifier.weight(1f),
                    count = memories.size.toString(),
                    label = "MEMORIES",
                    color = SleekNavyPrimary,
                    containerColor = SleekMeetingBg
                )
                AnalyticsStatPill(
                    modifier = Modifier.weight(1f),
                    count = projects.size.toString(),
                    label = "PROJECTS",
                    color = SleekTaskLabel,
                    containerColor = SleekTaskBg
                )
                AnalyticsStatPill(
                    modifier = Modifier.weight(1f),
                    count = "$completedSubtasks/$totalSubtasks",
                    label = "SUBTASKS",
                    color = SleekUrgentLabel,
                    containerColor = SleekUrgentBg
                )
            }
        }
    }
}

@Composable
private fun AnalyticsStatPill(
    count: String,
    label: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = color)
        }
    }
}
