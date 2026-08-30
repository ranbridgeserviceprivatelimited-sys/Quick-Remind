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
import com.example.data.model.ProjectItem
import com.example.data.model.ProjectTaskItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProjectsScreen(
    projects: List<ProjectItem>,
    allTasks: List<ProjectTaskItem>,
    searchQuery: String,
    onAddProjectClick: () -> Unit,
    onToggleTask: (ProjectTaskItem) -> Unit,
    onAddTask: (projectId: Long, title: String) -> Unit,
    onDeleteTask: (ProjectTaskItem) -> Unit,
    onDeleteProject: (ProjectItem) -> Unit
) {
    val filteredProjects = projects.filter { proj ->
        searchQuery.isBlank() || proj.name.contains(searchQuery, ignoreCase = true) || proj.description.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProjectClick,
                containerColor = CyberViolet,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_project")
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "New Project")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Project Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Break down ambitious projects into milestones", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (filteredProjects.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No projects yet. Tap + to start tracking your goals.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredProjects) { project ->
                    val projectTasks = allTasks.filter { it.projectId == project.id }
                    ProjectDetailCard(
                        project = project,
                        tasks = projectTasks,
                        onToggleTask = onToggleTask,
                        onAddTask = { title -> onAddTask(project.id, title) },
                        onDeleteTask = onDeleteTask,
                        onDeleteProject = { onDeleteProject(project) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectDetailCard(
    project: ProjectItem,
    tasks: List<ProjectTaskItem>,
    onToggleTask: (ProjectTaskItem) -> Unit,
    onAddTask: (String) -> Unit,
    onDeleteTask: (ProjectTaskItem) -> Unit,
    onDeleteProject: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    var newTaskTitle by remember { mutableStateOf("") }

    val completedTasks = tasks.filter { it.isCompleted }
    val pendingTasks = tasks.filter { !it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedTasks.size.toFloat() / totalCount.toFloat() else 0f

    val deadlineStr = project.deadlineMillis?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().testTag("project_card_${project.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(CyberViolet)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    IconButton(onClick = onDeleteProject, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = CyberViolet,
                trackColor = MaterialTheme.colorScheme.surface
            )

            // Next Step Suggestion
            if (project.nextStepSuggestion.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberCyan.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingFlat, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Next Step", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                            Text(project.nextStepSuggestion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            if (deadlineStr != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Deadline: $deadlineStr", style = MaterialTheme.typography.labelSmall, color = CyberAmber, fontWeight = FontWeight.Medium)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surface)

            // Subtasks checklist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Subtasks (${completedTasks.size}/$totalCount completed)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand"
                    )
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(6.dp))

                // Pending Tasks
                if (pendingTasks.isNotEmpty()) {
                    Text("Pending", style = MaterialTheme.typography.labelSmall, color = CyberAmber, fontWeight = FontWeight.Bold)
                    pendingTasks.forEach { task ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { onToggleTask(task) },
                                modifier = Modifier.size(32.dp).testTag("task_check_${task.id}")
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(task.title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDeleteTask(task) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Completed Tasks
                if (completedTasks.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Completed", style = MaterialTheme.typography.labelSmall, color = CyberEmerald, fontWeight = FontWeight.Bold)
                    completedTasks.forEach { task ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { onToggleTask(task) },
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                task.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDeleteTask(task) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Quick add subtask row
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("+ Add subtask...") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                onAddTask(newTaskTitle)
                                newTaskTitle = ""
                            }
                        },
                        enabled = newTaskTitle.isNotBlank()
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add subtask", tint = CyberViolet)
                    }
                }
            }
        }
    }
}
