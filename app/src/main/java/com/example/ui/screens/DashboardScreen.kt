package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    memories: List<MemoryItem>,
    reminders: List<ReminderItem>,
    projects: List<ProjectItem>,
    tasks: List<ProjectTaskItem>,
    events: List<CalendarEventItem>,
    proactiveInsight: String,
    onVoiceClick: () -> Unit,
    onMorningPlanClick: () -> Unit,
    onEveningReviewClick: () -> Unit,
    onToggleReminder: (ReminderItem) -> Unit,
    onAddMemoryClick: () -> Unit,
    onAddReminderClick: () -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    val pendingReminders = reminders.filter { !it.isCompleted }
    val urgentCount = pendingReminders.count { it.priority == ReminderPriority.HIGH } + memories.count { it.importance == MemoryImportance.HIGH }
    val tasksCount = pendingReminders.size
    val meetingsCount = events.size

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // Sleek Stat Badges Grid (Urgent, Tasks, Meetings)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_stat_grid"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Urgent Stat
                SleekStatCard(
                    modifier = Modifier.weight(1f),
                    count = urgentCount.toString(),
                    label = "URGENT",
                    containerColor = SleekUrgentBg,
                    textColor = SleekUrgentText,
                    labelColor = SleekUrgentLabel
                )

                // Tasks Stat
                SleekStatCard(
                    modifier = Modifier.weight(1f),
                    count = tasksCount.toString(),
                    label = "TASKS",
                    containerColor = SleekTaskBg,
                    textColor = SleekTaskText,
                    labelColor = SleekTaskLabel
                )

                // Meetings Stat
                SleekStatCard(
                    modifier = Modifier.weight(1f),
                    count = meetingsCount.toString(),
                    label = "MEETING",
                    containerColor = SleekMeetingBg,
                    textColor = SleekMeetingText,
                    labelColor = SleekMeetingLabel
                )
            }
        }

        // AI Memory Insight Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("proactive_suggestion_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SleekNavyPrimary.copy(alpha = pulseAlpha))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "AI Memory Insight",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "\"$proactiveInsight\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clickable { onNavigateToTab("AI_CHAT") }
                                .testTag("insight_action_chat")
                        ) {
                            Text(
                                text = "Ask AI Brain",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SleekNavyPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clickable { onMorningPlanClick() }
                                .testTag("insight_action_plan")
                        ) {
                            Text(
                                text = "Daily Focus Plan",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SleekNavySecondary
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Projects Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ACTIVE PROJECTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextMuted
                        )
                    )
                    Text(
                        text = "View Timeline",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = SleekNavyPrimary
                        ),
                        modifier = Modifier.clickable { onNavigateToTab("TIMELINE") }
                    )
                }

                if (projects.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active projects yet. Add one to track milestones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    projects.take(3).forEach { project ->
                        val pTasks = tasks.filter { it.projectId == project.id }
                        val completedCount = pTasks.count { it.isCompleted }
                        val totalCount = pTasks.size
                        val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0.65f
                        val progressPercent = (progress * 100).toInt()

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToTab("PROJECTS") }
                                .testTag("dashboard_project_card_${project.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = project.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$progressPercent%",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SleekNavyPrimary
                                        )
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.outline)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = progress.coerceIn(0.05f, 1f))
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(SleekNavyPrimary)
                                    )
                                }
                                if (project.nextStepSuggestion.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Next: ${project.nextStepSuggestion}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Reminders & Tasks
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TODAY'S REMINDERS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted
                    )
                )
                TextButton(
                    onClick = onAddReminderClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = SleekNavyPrimary)
                    Spacer(Modifier.width(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SleekNavyPrimary))
                }
            }
        }

        if (pendingReminders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨ All tasks caught up! Speak or tap to add.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(pendingReminders.take(4)) { reminder ->
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timeStr = timeFormat.format(Date(reminder.dueDateMillis))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_reminder_card_${reminder.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = reminder.isCompleted,
                            onCheckedChange = { onToggleReminder(reminder) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SleekNavyPrimary,
                                uncheckedColor = SleekTextMuted
                            ),
                            modifier = Modifier.testTag("dashboard_checkbox_${reminder.id}")
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reminder.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$timeStr • ${reminder.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (reminder.priority == ReminderPriority.HIGH) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SleekUrgentBg
                            ) {
                                Text(
                                    text = "URGENT",
                                    color = SleekUrgentLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sleek Voice Input Section (Microphone and Natural Language prompt pill)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Telugu / English Natural language example pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.clickable { onVoiceClick() }
                ) {
                    Text(
                        text = "\"Repu 8 ki college ki vellali ani remind cheyyi\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Center Sleek 64.dp Mic Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = SleekNavyPrimary)
                        .clip(CircleShape)
                        .background(SleekNavyPrimary)
                        .clickable { onVoiceClick() }
                        .testTag("dashboard_voice_banner"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Tell Quick Remind",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "TELL QUICK REMIND",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekNavyPrimary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun SleekStatCard(
    count: String,
    label: String,
    containerColor: Color,
    textColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = textColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = labelColor
            )
        }
    }
}
