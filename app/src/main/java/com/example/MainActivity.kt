package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavigationTab
import com.example.ui.viewmodel.QuickRemindViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: QuickRemindViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuickRemindTheme {
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

                val memories by viewModel.allMemories.collectAsStateWithLifecycle()
                val activeReminders by viewModel.activeReminders.collectAsStateWithLifecycle()
                val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
                val activeNotes by viewModel.activeNotes.collectAsStateWithLifecycle()
                val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
                val allProjectTasks by viewModel.allProjectTasks.collectAsStateWithLifecycle()
                val allEvents by viewModel.allEvents.collectAsStateWithLifecycle()
                val recentLogs by viewModel.recentActivityLogs.collectAsStateWithLifecycle()

                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
                val isAILoading by viewModel.isAILoading.collectAsStateWithLifecycle()
                val proactiveInsight by viewModel.proactiveInsight.collectAsStateWithLifecycle()
                val dailyBriefingText by viewModel.dailyBriefingText.collectAsStateWithLifecycle()
                val dailyBriefingType by viewModel.dailyBriefingType.collectAsStateWithLifecycle()
                val isBriefingLoading by viewModel.isBriefingLoading.collectAsStateWithLifecycle()

                // Dialog states
                val isVoiceDialogOpen by viewModel.isVoiceDialogOpen.collectAsStateWithLifecycle()
                val isVoiceProcessing by viewModel.isVoiceProcessing.collectAsStateWithLifecycle()
                val voiceFeedback by viewModel.voiceResultFeedback.collectAsStateWithLifecycle()

                val isScannerOpen by viewModel.isScannerDialogOpen.collectAsStateWithLifecycle()
                val isScanningImage by viewModel.isScanningImage.collectAsStateWithLifecycle()

                val showAddMemory by viewModel.showAddMemoryDialog.collectAsStateWithLifecycle()
                val showAddReminder by viewModel.showAddReminderDialog.collectAsStateWithLifecycle()
                val showAddNote by viewModel.showAddNoteDialog.collectAsStateWithLifecycle()
                val showAddProject by viewModel.showAddProjectDialog.collectAsStateWithLifecycle()

                val selectedCategory by viewModel.selectedMemoryCategory.collectAsStateWithLifecycle()
                val selectedImportance by viewModel.selectedMemoryImportance.collectAsStateWithLifecycle()
                val isPinRequired by viewModel.isPinRequired.collectAsStateWithLifecycle()

                var showAddCalendarEventDialog by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppTopBar(
                            title = "Quick Remind",
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.searchQuery.value = it },
                            onVoiceClick = { viewModel.isVoiceDialogOpen.value = true },
                            onScanClick = { viewModel.isScannerDialogOpen.value = true },
                            onMorningPlanClick = { viewModel.generateMorningPlan() },
                            onSettingsClick = { viewModel.currentTab.value = AppNavigationTab.SETTINGS }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .height(68.dp)
                                .testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppNavigationTab.DASHBOARD,
                                onClick = { viewModel.currentTab.value = AppNavigationTab.DASHBOARD },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Today") },
                                label = { Text("Today", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == AppNavigationTab.DASHBOARD) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SleekNavyPrimary,
                                    selectedTextColor = SleekNavyPrimary,
                                    indicatorColor = SleekNavyContainer,
                                    unselectedIconColor = SleekTextSecondary,
                                    unselectedTextColor = SleekTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_item_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavigationTab.MEMORIES,
                                onClick = { viewModel.currentTab.value = AppNavigationTab.MEMORIES },
                                icon = { Icon(Icons.Default.Psychology, contentDescription = "Memory") },
                                label = { Text("Memory", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == AppNavigationTab.MEMORIES) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SleekNavyPrimary,
                                    selectedTextColor = SleekNavyPrimary,
                                    indicatorColor = SleekNavyContainer,
                                    unselectedIconColor = SleekTextSecondary,
                                    unselectedTextColor = SleekTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_item_memories")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavigationTab.REMINDERS,
                                onClick = { viewModel.currentTab.value = AppNavigationTab.REMINDERS },
                                icon = { Icon(Icons.Default.Alarm, contentDescription = "Remind") },
                                label = { Text("Remind", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == AppNavigationTab.REMINDERS) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SleekNavyPrimary,
                                    selectedTextColor = SleekNavyPrimary,
                                    indicatorColor = SleekNavyContainer,
                                    unselectedIconColor = SleekTextSecondary,
                                    unselectedTextColor = SleekTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_item_reminders")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavigationTab.PROJECTS,
                                onClick = { viewModel.currentTab.value = AppNavigationTab.PROJECTS },
                                icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Projects") },
                                label = { Text("Projects", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == AppNavigationTab.PROJECTS) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SleekNavyPrimary,
                                    selectedTextColor = SleekNavyPrimary,
                                    indicatorColor = SleekNavyContainer,
                                    unselectedIconColor = SleekTextSecondary,
                                    unselectedTextColor = SleekTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_item_projects")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavigationTab.AI_CHAT,
                                onClick = { viewModel.currentTab.value = AppNavigationTab.AI_CHAT },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Chat") },
                                label = { Text("AI Chat", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == AppNavigationTab.AI_CHAT) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SleekNavyPrimary,
                                    selectedTextColor = SleekNavyPrimary,
                                    indicatorColor = SleekNavyContainer,
                                    unselectedIconColor = SleekTextSecondary,
                                    unselectedTextColor = SleekTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_item_ai_chat")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Quick navigation subtabs strip for secondary screens
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (currentTab != AppNavigationTab.DASHBOARD && currentTab != AppNavigationTab.AI_CHAT) {
                                ScrollableTabRow(
                                    selectedTabIndex = when (currentTab) {
                                        AppNavigationTab.MEMORIES -> 0
                                        AppNavigationTab.REMINDERS -> 1
                                        AppNavigationTab.PROJECTS -> 2
                                        AppNavigationTab.NOTES -> 3
                                        AppNavigationTab.CALENDAR -> 4
                                        AppNavigationTab.TIMELINE -> 5
                                        AppNavigationTab.ANALYTICS -> 6
                                        AppNavigationTab.SETTINGS -> 7
                                        else -> 0
                                    },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = SleekNavyPrimary,
                                    edgePadding = 16.dp,
                                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp) },
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Tab(
                                        selected = currentTab == AppNavigationTab.MEMORIES,
                                        onClick = { viewModel.currentTab.value = AppNavigationTab.MEMORIES },
                                        text = { Text("Memories", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    Tab(
                                        selected = currentTab == AppNavigationTab.REMINDERS,
                                        onClick = { viewModel.currentTab.value = AppNavigationTab.REMINDERS },
                                        text = { Text("Reminders", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    Tab(
                                        selected = currentTab == AppNavigationTab.PROJECTS,
                                        onClick = { viewModel.currentTab.value = AppNavigationTab.PROJECTS },
                                        text = { Text("Projects", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    Tab(
                                        selected = currentTab == AppNavigationTab.NOTES,
                                        onClick = { viewModel.currentTab.value = AppNavigationTab.NOTES },
                                        text = { Text("Notes", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    Tab(
                                        selected = currentTab == AppNavigationTab.CALENDAR,
                                        onClick = { viewModel.currentTab.value = AppNavigationTab.CALENDAR },
                                        text = { Text("Calendar", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    Tab(
                                        selected = currentTab == AppNavigationTab.TIMELINE,
                                        onClick = { viewModel.currentTab.value = AppNavigationTab.TIMELINE },
                                        text = { Text("Timeline", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    Tab(
                                        selected = currentTab == AppNavigationTab.ANALYTICS,
                                        onClick = { viewModel.currentTab.value = AppNavigationTab.ANALYTICS },
                                        text = { Text("Analytics", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    Tab(
                                        selected = currentTab == AppNavigationTab.SETTINGS,
                                        onClick = { viewModel.currentTab.value = AppNavigationTab.SETTINGS },
                                        text = { Text("Settings", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            // Active Screen View
                            when (currentTab) {
                                AppNavigationTab.DASHBOARD -> {
                                    DashboardScreen(
                                        memories = memories,
                                        reminders = allReminders,
                                        projects = allProjects,
                                        tasks = allProjectTasks,
                                        events = allEvents,
                                        proactiveInsight = proactiveInsight,
                                        onVoiceClick = { viewModel.isVoiceDialogOpen.value = true },
                                        onMorningPlanClick = { viewModel.generateMorningPlan() },
                                        onEveningReviewClick = { viewModel.generateEveningReview() },
                                        onToggleReminder = { viewModel.toggleReminder(it) },
                                        onAddMemoryClick = { viewModel.showAddMemoryDialog.value = true },
                                        onAddReminderClick = { viewModel.showAddReminderDialog.value = true },
                                        onNavigateToTab = { tabName ->
                                            viewModel.currentTab.value = when(tabName) {
                                                "MEMORIES" -> AppNavigationTab.MEMORIES
                                                "PROJECTS" -> AppNavigationTab.PROJECTS
                                                "CALENDAR" -> AppNavigationTab.CALENDAR
                                                else -> AppNavigationTab.DASHBOARD
                                            }
                                        }
                                    )
                                }

                                AppNavigationTab.MEMORIES -> {
                                    MemoriesScreen(
                                        memories = memories,
                                        searchQuery = searchQuery,
                                        selectedCategory = selectedCategory,
                                        selectedImportance = selectedImportance,
                                        onCategorySelect = { viewModel.selectedMemoryCategory.value = it },
                                        onImportanceSelect = { viewModel.selectedMemoryImportance.value = it },
                                        onAddMemoryClick = { viewModel.showAddMemoryDialog.value = true },
                                        onDeleteMemory = { viewModel.deleteMemory(it) },
                                        onTogglePermanent = { viewModel.updateMemory(it.copy(isPermanent = !it.isPermanent)) },
                                        onSpeakMemory = { viewModel.speakText(it) }
                                    )
                                }

                                AppNavigationTab.REMINDERS -> {
                                    RemindersScreen(
                                        allReminders = allReminders,
                                        searchQuery = searchQuery,
                                        onToggleReminder = { viewModel.toggleReminder(it) },
                                        onSnoozeReminder = { rem, mins -> viewModel.snoozeReminder(rem, mins) },
                                        onDeleteReminder = { viewModel.deleteReminder(it) },
                                        onAddReminderClick = { viewModel.showAddReminderDialog.value = true },
                                        onNaturalLanguageExecute = { viewModel.executeNaturalLanguageCommand(it) }
                                    )
                                }

                                AppNavigationTab.PROJECTS -> {
                                    ProjectsScreen(
                                        projects = allProjects,
                                        allTasks = allProjectTasks,
                                        searchQuery = searchQuery,
                                        onAddProjectClick = { viewModel.showAddProjectDialog.value = true },
                                        onToggleTask = { viewModel.toggleProjectTask(it) },
                                        onAddTask = { projId, title -> viewModel.addProjectTask(projId, title) },
                                        onDeleteTask = { viewModel.deleteProjectTask(it) },
                                        onDeleteProject = { viewModel.deleteProject(it) }
                                    )
                                }

                                AppNavigationTab.NOTES -> {
                                    NotesScreen(
                                        notes = activeNotes,
                                        searchQuery = searchQuery,
                                        onAddNoteClick = { viewModel.showAddNoteDialog.value = true },
                                        onTogglePin = { viewModel.togglePinNote(it) },
                                        onDeleteNote = { viewModel.deleteNote(it) }
                                    )
                                }

                                AppNavigationTab.CALENDAR -> {
                                    CalendarScreen(
                                        events = allEvents,
                                        reminders = activeReminders,
                                        onAddEventClick = { showAddCalendarEventDialog = true }
                                    )
                                }

                                AppNavigationTab.TIMELINE -> {
                                    TimelineScreen(
                                        logs = recentLogs,
                                        onAskTimeline = { viewModel.sendChatMessage(it) }
                                    )
                                }

                                AppNavigationTab.AI_CHAT -> {
                                    AIAssistantScreen(
                                        chatMessages = chatMessages,
                                        isLoading = isAILoading,
                                        onSendMessage = { viewModel.sendChatMessage(it) },
                                        onSpeakMessage = { viewModel.speakText(it) },
                                        onVoiceClick = { viewModel.isVoiceDialogOpen.value = true }
                                    )
                                }

                                AppNavigationTab.ANALYTICS -> {
                                    AnalyticsScreen(
                                        memories = memories,
                                        reminders = allReminders,
                                        projects = allProjects,
                                        tasks = allProjectTasks
                                    )
                                }

                                AppNavigationTab.SETTINGS -> {
                                    SettingsScreen(
                                        isPinRequired = isPinRequired,
                                        onTogglePin = { viewModel.isPinRequired.value = it },
                                        onExportBackup = { viewModel.exportBackup {} },
                                        onPurgeShortTerm = {}
                                    )
                                }
                            }
                        }
                    }
                }

                // Global Modals & Dialogs
                VoiceInputDialog(
                    isOpen = isVoiceDialogOpen,
                    isProcessing = isVoiceProcessing,
                    feedbackText = voiceFeedback,
                    onDismiss = {
                        viewModel.isVoiceDialogOpen.value = false
                        viewModel.voiceResultFeedback.value = null
                    },
                    onProcessCommand = { viewModel.executeNaturalLanguageCommand(it) }
                )

                ImageMemoryScannerDialog(
                    isOpen = isScannerOpen,
                    isScanning = isScanningImage,
                    onDismiss = { viewModel.isScannerDialogOpen.value = false },
                    onScanBitmap = { viewModel.scanImage(it) }
                )

                DailyBriefingBottomSheet(
                    briefingText = dailyBriefingText,
                    briefingType = dailyBriefingType,
                    isLoading = isBriefingLoading,
                    onSpeak = { viewModel.speakText(it) },
                    onClose = { viewModel.closeDailyBriefing() }
                )

                if (showAddMemory) {
                    AddMemoryDialog(
                        onDismiss = { viewModel.showAddMemoryDialog.value = false },
                        onConfirm = { title, content, cat, imp, isPerm, tags, topic ->
                            viewModel.addMemory(title, content, cat, imp, isPerm, tags, topic)
                        }
                    )
                }

                if (showAddReminder) {
                    AddReminderDialog(
                        onDismiss = { viewModel.showAddReminderDialog.value = false },
                        onConfirm = { title, notes, due, prio, rec, cat ->
                            viewModel.addReminder(title, notes, due, prio, rec, cat)
                        }
                    )
                }

                if (showAddNote) {
                    AddNoteDialog(
                        onDismiss = { viewModel.showAddNoteDialog.value = false },
                        onConfirm = { title, content, cat, tags, colorHex ->
                            viewModel.addNote(title, content, cat, tags, colorHex)
                        }
                    )
                }

                if (showAddProject) {
                    AddProjectDialog(
                        onDismiss = { viewModel.showAddProjectDialog.value = false },
                        onConfirm = { name, desc, cat, deadline, tasks, color ->
                            viewModel.addProject(name, desc, cat, deadline, tasks, color)
                        }
                    )
                }

                if (showAddCalendarEventDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddCalendarEventDialog = false },
                        title = { Text("Add Calendar Event") },
                        text = { Text("Event quickly scheduled with mentor or team.") },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.addCalendarEvent(
                                    title = "Project Sync",
                                    description = "Flight Controller Hardware check",
                                    startMillis = System.currentTimeMillis() + 3600000L * 4,
                                    endMillis = System.currentTimeMillis() + 3600000L * 5,
                                    location = "Lab 101",
                                    category = "Meeting"
                                )
                                showAddCalendarEventDialog = false
                            }) {
                                Text("Schedule")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddCalendarEventDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}
