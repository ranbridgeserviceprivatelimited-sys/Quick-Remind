package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.ai.GeminiBrainService
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class QuickRemindRepository(private val database: AppDatabase) {

    private val memoryDao = database.memoryDao()
    private val reminderDao = database.reminderDao()
    private val noteDao = database.noteDao()
    private val projectDao = database.projectDao()
    private val calendarDao = database.calendarDao()
    private val activityLogDao = database.activityLogDao()
    val aiBrain = GeminiBrainService()

    // Observables
    val allMemories: Flow<List<MemoryItem>> = memoryDao.getAllMemories()
    val permanentMemories: Flow<List<MemoryItem>> = memoryDao.getPermanentMemories()
    val activeReminders: Flow<List<ReminderItem>> = reminderDao.getActiveReminders()
    val completedReminders: Flow<List<ReminderItem>> = reminderDao.getCompletedReminders()
    val allReminders: Flow<List<ReminderItem>> = reminderDao.getAllReminders()
    val activeNotes: Flow<List<NoteItem>> = noteDao.getActiveNotes()
    val allProjects: Flow<List<ProjectItem>> = projectDao.getAllProjects()
    val allProjectTasks: Flow<List<ProjectTaskItem>> = projectDao.getAllProjectTasks()
    val allEvents: Flow<List<CalendarEventItem>> = calendarDao.getAllEvents()
    val recentActivityLogs: Flow<List<ActivityLogItem>> = activityLogDao.getRecentLogs()

    fun getTasksForProject(projectId: Long): Flow<List<ProjectTaskItem>> = projectDao.getTasksForProject(projectId)

    // Memory operations
    suspend fun saveMemory(memory: MemoryItem): Long = withContext(Dispatchers.IO) {
        val id = memoryDao.insertMemory(memory)
        logActivity("MEMORY_ADDED", "Stored: ${memory.title}", memory.content.take(80))
        id
    }

    suspend fun updateMemory(memory: MemoryItem) = withContext(Dispatchers.IO) {
        memoryDao.updateMemory(memory.copy(updatedAtMillis = System.currentTimeMillis()))
        logActivity("MEMORY_UPDATED", "Updated: ${memory.title}", memory.content.take(80))
    }

    suspend fun deleteMemory(memory: MemoryItem) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemory(memory)
        logActivity("MEMORY_FORGOTTEN", "Forgot: ${memory.title}", "Removed from permanent memory")
    }

    // Reminder operations
    suspend fun saveReminder(reminder: ReminderItem): Long = withContext(Dispatchers.IO) {
        val id = reminderDao.insertReminder(reminder)
        logActivity("REMINDER_SET", "Alarm Set: ${reminder.title}", "Due at ${java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(reminder.dueDateMillis))}")
        id
    }

    suspend fun toggleReminderComplete(reminder: ReminderItem) = withContext(Dispatchers.IO) {
        val newState = !reminder.isCompleted
        val updated = reminder.copy(
            isCompleted = newState,
            completedAtMillis = if (newState) System.currentTimeMillis() else null
        )
        reminderDao.updateReminder(updated)
        if (newState) {
            logActivity("TASK_DONE", "Completed: ${reminder.title}", "Marked finished")
        }
    }

    suspend fun snoozeReminder(reminder: ReminderItem, minutes: Int = 15) = withContext(Dispatchers.IO) {
        val newDue = reminder.dueDateMillis + (minutes * 60 * 1000L)
        val updated = reminder.copy(
            dueDateMillis = newDue,
            snoozeCount = reminder.snoozeCount + 1
        )
        reminderDao.updateReminder(updated)
        logActivity("REMINDER_SNOOZED", "Snoozed: ${reminder.title}", "Pushed by $minutes mins")
    }

    suspend fun deleteReminder(reminder: ReminderItem) = withContext(Dispatchers.IO) {
        reminderDao.deleteReminder(reminder)
    }

    // Note operations
    suspend fun saveNote(note: NoteItem): Long = withContext(Dispatchers.IO) {
        val id = noteDao.insertNote(note)
        logActivity("NOTE_CREATED", "Saved Note: ${note.title}", note.content.take(80))
        id
    }

    suspend fun updateNote(note: NoteItem) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun togglePinNote(note: NoteItem) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note.copy(isPinned = !note.isPinned, updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun deleteNote(note: NoteItem) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    // Project operations
    suspend fun saveProject(project: ProjectItem, initialTasks: List<String> = emptyList()): Long = withContext(Dispatchers.IO) {
        val projId = projectDao.insertProject(project)
        initialTasks.forEachIndexed { index, taskTitle ->
            projectDao.insertTask(
                ProjectTaskItem(
                    projectId = projId,
                    title = taskTitle,
                    orderIndex = index
                )
            )
        }
        logActivity("PROJECT_CREATED", "Project Started: ${project.name}", project.description.take(80))
        projId
    }

    suspend fun updateProject(project: ProjectItem) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: ProjectItem) = withContext(Dispatchers.IO) {
        projectDao.deleteTasksByProject(project.id)
        projectDao.deleteProject(project)
        logActivity("PROJECT_ARCHIVED", "Removed Project: ${project.name}", "")
    }

    suspend fun toggleProjectTask(task: ProjectTaskItem) = withContext(Dispatchers.IO) {
        val newState = !task.isCompleted
        projectDao.updateTask(task.copy(isCompleted = newState, completedAtMillis = if (newState) System.currentTimeMillis() else null))
        if (newState) {
            logActivity("TASK_DONE", "Subtask Done: ${task.title}", "")
        }
    }

    suspend fun addProjectTask(projectId: Long, title: String) = withContext(Dispatchers.IO) {
        projectDao.insertTask(ProjectTaskItem(projectId = projectId, title = title))
    }

    suspend fun deleteProjectTask(task: ProjectTaskItem) = withContext(Dispatchers.IO) {
        projectDao.deleteTask(task)
    }

    // Calendar Event operations
    suspend fun saveCalendarEvent(event: CalendarEventItem): Long = withContext(Dispatchers.IO) {
        val id = calendarDao.insertEvent(event)
        logActivity("EVENT_SCHEDULED", "Calendar: ${event.title}", "Scheduled for ${java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(event.startMillis))}")
        id
    }

    suspend fun deleteCalendarEvent(event: CalendarEventItem) = withContext(Dispatchers.IO) {
        calendarDao.deleteEvent(event)
    }

    // Activity Log
    suspend fun logActivity(actionType: String, title: String, details: String) = withContext(Dispatchers.IO) {
        activityLogDao.insertLog(
            ActivityLogItem(
                actionType = actionType,
                title = title,
                details = details,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    // AI & NL processing
    suspend fun processNaturalLanguageCommand(rawInput: String): GeminiBrainService.ParsedNLResult {
        val result = aiBrain.parseNaturalLanguageVoiceOrText(rawInput)
        when (result.intent) {
            "REMINDER" -> {
                val due = result.dueDateTimeMillis ?: (System.currentTimeMillis() + 3600000L * 3)
                val prio = when (result.priority.uppercase()) {
                    "HIGH" -> ReminderPriority.HIGH
                    "LOW" -> ReminderPriority.LOW
                    else -> ReminderPriority.MEDIUM
                }
                val rec = when (result.recurrence.uppercase()) {
                    "DAILY" -> RecurrenceType.DAILY
                    "WEEKLY" -> RecurrenceType.WEEKLY
                    "MONTHLY" -> RecurrenceType.MONTHLY
                    else -> RecurrenceType.NONE
                }
                saveReminder(
                    ReminderItem(
                        title = result.title,
                        notes = result.details,
                        dueDateMillis = due,
                        priority = prio,
                        recurrence = rec,
                        category = result.category
                    )
                )
            }
            "MEMORY" -> {
                val cat = try { MemoryCategory.valueOf(result.category.uppercase()) } catch (e: Exception) { MemoryCategory.GENERAL }
                val imp = when (result.priority.uppercase()) {
                    "HIGH" -> MemoryImportance.HIGH
                    "LOW" -> MemoryImportance.LOW
                    else -> MemoryImportance.NORMAL
                }
                saveMemory(
                    MemoryItem(
                        title = result.title,
                        content = result.details,
                        category = cat,
                        importance = imp,
                        isPermanent = result.isPermanent,
                        tags = result.tags
                    )
                )
            }
            "NOTE" -> {
                saveNote(
                    NoteItem(
                        title = result.title,
                        content = result.details,
                        category = result.category,
                        tags = result.tags
                    )
                )
            }
            "PROJECT" -> {
                saveProject(
                    ProjectItem(
                        name = result.title,
                        description = result.details,
                        category = result.category,
                        deadlineMillis = System.currentTimeMillis() + (86400000L * 14)
                    ),
                    initialTasks = listOf("Define requirements", "Initial prototyping", "System verification")
                )
            }
        }
        return result
    }

    suspend fun queryAIAssistant(userQuery: String): String = withContext(Dispatchers.IO) {
        val memories = memoryDao.getAllMemoriesDirect()
        val reminders = reminderDao.getAllRemindersDirect()
        val projects = projectDao.getAllProjectsDirect()
        val notes = noteDao.getAllNotesDirect()
        val events = calendarDao.getAllEventsDirect()
        aiBrain.answerAssistantQuery(userQuery, memories, reminders, projects, notes, events)
    }

    suspend fun generateDailyPlan(): String = withContext(Dispatchers.IO) {
        val reminders = reminderDao.getAllRemindersDirect()
        val projects = projectDao.getAllProjectsDirect()
        val tasks = projectDao.getAllTasksDirect()
        val memories = memoryDao.getAllMemoriesDirect()
        aiBrain.generateDailyBriefing(reminders, projects, tasks, memories)
    }

    suspend fun generateEveningReview(): String = withContext(Dispatchers.IO) {
        val reminders = reminderDao.getAllRemindersDirect()
        val completed = reminders.filter { it.isCompleted }
        val pending = reminders.filter { !it.isCompleted }
        val projects = projectDao.getAllProjectsDirect()
        aiBrain.generateEveningReview(completed, pending, projects)
    }

    suspend fun scanImageMemory(bitmap: Bitmap): GeminiBrainService.ParsedNLResult {
        val result = aiBrain.extractFromImageMemory(bitmap)
        val cat = try { MemoryCategory.valueOf(result.category.uppercase()) } catch (e: Exception) { MemoryCategory.GENERAL }
        saveMemory(
            MemoryItem(
                title = result.title,
                content = result.details,
                category = cat,
                importance = MemoryImportance.HIGH,
                tags = result.tags.ifEmpty { listOf("photo-ocr", "scanned") }
            )
        )
        return result
    }

    suspend fun getProactiveInsight(): String = withContext(Dispatchers.IO) {
        val memories = memoryDao.getAllMemoriesDirect()
        val reminders = reminderDao.getAllRemindersDirect()
        val projects = projectDao.getAllProjectsDirect()
        val tasks = projectDao.getAllTasksDirect()
        aiBrain.generateProactiveInsight(memories, reminders, projects, tasks)
    }

    // Seed realistic sample data so first open is immediately rich and matches user brief!
    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = memoryDao.getAllMemoriesDirect()
        if (existing.isNotEmpty()) return@withContext

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // 1. Memories
        saveMemory(
            MemoryItem(
                title = "Flight Controller Hardware Architecture",
                content = "My flight controller project uses an STM32F405 microcontroller running at 168MHz with an MPU6000 IMU over SPI and DPS310 barometer over I2C.",
                category = MemoryCategory.PROJECT,
                importance = MemoryImportance.HIGH,
                isPermanent = true,
                tags = listOf("flight-controller", "stm32", "hardware", "drone"),
                connectedTopic = "Flight Controller"
            )
        )

        saveMemory(
            MemoryItem(
                title = "GPS Module Configuration with Ravi",
                content = "Discussed with Ravi that the U-Blox NEO-M8N GPS module Baud rate should be locked to 115200bps and GNSS constellation set to GPS+Galileo.",
                category = MemoryCategory.PROJECT,
                importance = MemoryImportance.NORMAL,
                isPermanent = true,
                tags = listOf("ravi", "gps", "flight-controller"),
                connectedTopic = "Flight Controller"
            )
        )

        saveMemory(
            MemoryItem(
                title = "C-DAC Project Evaluation Requirements",
                content = "C-DAC project review requires submission of 3 components: PCB Gerber files, firmware unit test coverage report, and power budget analysis.",
                category = MemoryCategory.WORK,
                importance = MemoryImportance.HIGH,
                isPermanent = true,
                tags = listOf("cdac", "evaluation", "college"),
                connectedTopic = "C-DAC"
            )
        )

        saveMemory(
            MemoryItem(
                title = "Personal Server IP & SSH Key",
                content = "Home lab server is at 192.168.1.120 port 2222 with ed25519 key authentication only. Docker swarm master node.",
                category = MemoryCategory.PERSONAL,
                importance = MemoryImportance.HIGH,
                isPermanent = true,
                tags = listOf("server", "homelab", "credentials")
            )
        )

        // 2. Projects & Subtasks
        val projId1 = projectDao.insertProject(
            ProjectItem(
                name = "Flight Controller Drone",
                description = "Custom 32-bit autonomous flight controller PCB and sensor fusion firmware.",
                category = "Robotics & Embedded",
                deadlineMillis = now + (86400000L * 18),
                status = ProjectStatus.ACTIVE,
                nextStepSuggestion = "Test IMU SPI communication at 10MHz and verify low-pass filtering.",
                colorHex = "#6366F1"
            )
        )

        projectDao.insertTask(ProjectTaskItem(projectId = projId1, title = "PCB schematic & 4-layer routing", isCompleted = true, orderIndex = 0))
        projectDao.insertTask(ProjectTaskItem(projectId = projId1, title = "Power regulation 5V/3.3V step-down", isCompleted = true, orderIndex = 1))
        projectDao.insertTask(ProjectTaskItem(projectId = projId1, title = "Sensor integration & MPU6000 driver", isCompleted = true, orderIndex = 2))
        projectDao.insertTask(ProjectTaskItem(projectId = projId1, title = "Test IMU communication & Kalman filter", isCompleted = false, orderIndex = 3, priority = ReminderPriority.HIGH))
        projectDao.insertTask(ProjectTaskItem(projectId = projId1, title = "GPS UART driver integration", isCompleted = false, orderIndex = 4))
        projectDao.insertTask(ProjectTaskItem(projectId = projId1, title = "Bench flight tests & PID tuning", isCompleted = false, orderIndex = 5))

        val projId2 = projectDao.insertProject(
            ProjectItem(
                name = "AI Smart Classroom",
                description = "Automated attendance and lecture audio summarization pipeline for college labs.",
                category = "AI / ML",
                deadlineMillis = now + (86400000L * 30),
                status = ProjectStatus.ACTIVE,
                nextStepSuggestion = "Deploy quantized Whisper model to edge gateway.",
                colorHex = "#10B981"
            )
        )
        projectDao.insertTask(ProjectTaskItem(projectId = projId2, title = "Audio ingestion pipeline", isCompleted = true, orderIndex = 0))
        projectDao.insertTask(ProjectTaskItem(projectId = projId2, title = "Edge deployment & testing", isCompleted = false, orderIndex = 1))

        // 3. Smart Reminders
        saveReminder(
            ReminderItem(
                title = "College lab session & project demo",
                notes = "Bring STM32 programmer, breadboard, and logic analyzer.",
                dueDateMillis = now + 3600000L * 2,
                priority = ReminderPriority.HIGH,
                category = "College"
            )
        )

        saveReminder(
            ReminderItem(
                title = "C-DAC Discussion Meeting with Mentor",
                notes = "Review milestone 2 documentation and presentation deck.",
                dueDateMillis = now + 3600000L * 6,
                priority = ReminderPriority.HIGH,
                category = "C-DAC"
            )
        )

        saveReminder(
            ReminderItem(
                title = "Daily Evening Code Review & Commit",
                notes = "Push git commits and update project task log.",
                dueDateMillis = now + 3600000L * 10,
                recurrence = RecurrenceType.DAILY,
                priority = ReminderPriority.MEDIUM,
                category = "Development"
            )
        )

        // 4. Quick Notes
        saveNote(
            NoteItem(
                title = "Future idea: AI based agriculture drone",
                content = "Multispectral camera drone detecting early nitrogen deficiency and aphid infestation in paddy fields with autonomous spot-spraying payload.",
                category = "Idea",
                isPinned = true,
                tags = listOf("agriculture", "drone", "ai", "startup"),
                colorHex = "#065F46"
            )
        )

        saveNote(
            NoteItem(
                title = "Defence drone autonomous waypoint notes",
                content = "Optical flow + GPS denied navigation using stereo vision visual odometry. Need to benchmark on Raspberry Pi 5.",
                category = "Research",
                isPinned = false,
                tags = listOf("defence", "robotics", "cv"),
                colorHex = "#1E293B"
            )
        )

        // 5. Calendar Events
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 0)
        val eventStart1 = cal.timeInMillis

        calendarDao.insertEvent(
            CalendarEventItem(
                title = "C-DAC Meeting",
                description = "Architecture presentation with project guide",
                startMillis = eventStart1,
                endMillis = eventStart1 + 3600000L,
                location = "Lab 402 / Online",
                category = "Meeting",
                colorHex = "#6366F1"
            )
        )

        cal.set(Calendar.HOUR_OF_DAY, 14)
        val eventStart2 = cal.timeInMillis
        calendarDao.insertEvent(
            CalendarEventItem(
                title = "Flight Controller Hardware Lab",
                description = "Soldering breakout boards and testing IMU SPI",
                startMillis = eventStart2,
                endMillis = eventStart2 + 7200000L,
                location = "Electronics Lab",
                category = "Project",
                colorHex = "#EC4899"
            )
        )

        // 6. Timeline logs
        logActivity("MEMORY_ADDED", "Stored Flight Controller Architecture", "STM32 microcontroller specification")
        logActivity("PROJECT_CREATED", "Started Flight Controller Drone", "Initial schematics complete")
        logActivity("REMINDER_SET", "C-DAC Discussion Meeting", "Scheduled review with mentor")
    }

    suspend fun exportDataToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val memories = memoryDao.getAllMemoriesDirect()
        val reminders = reminderDao.getAllRemindersDirect()
        val notes = noteDao.getAllNotesDirect()
        val projects = projectDao.getAllProjectsDirect()
        val tasks = projectDao.getAllTasksDirect()

        val memArr = JSONArray()
        memories.forEach {
            memArr.put(JSONObject().apply {
                put("title", it.title)
                put("content", it.content)
                put("category", it.category.name)
                put("importance", it.importance.name)
                put("isPermanent", it.isPermanent)
            })
        }
        root.put("memories", memArr)

        val remArr = JSONArray()
        reminders.forEach {
            remArr.put(JSONObject().apply {
                put("title", it.title)
                put("notes", it.notes)
                put("dueDateMillis", it.dueDateMillis)
                put("isCompleted", it.isCompleted)
            })
        }
        root.put("reminders", remArr)

        val noteArr = JSONArray()
        notes.forEach {
            noteArr.put(JSONObject().apply {
                put("title", it.title)
                put("content", it.content)
                put("category", it.category)
            })
        }
        root.put("notes", noteArr)

        root.toString(2)
    }
}
