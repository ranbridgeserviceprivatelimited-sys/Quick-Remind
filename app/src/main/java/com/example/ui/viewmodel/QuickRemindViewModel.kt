package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.QuickRemindRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppNavigationTab {
    DASHBOARD, MEMORIES, REMINDERS, PROJECTS, NOTES, CALENDAR, TIMELINE, AI_CHAT, ANALYTICS, SETTINGS
}

class QuickRemindViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuickRemindRepository(AppDatabase.getDatabase(application))

    // UI state flows
    val currentTab = MutableStateFlow(AppNavigationTab.DASHBOARD)
    val searchQuery = MutableStateFlow("")

    val allMemories: StateFlow<List<MemoryItem>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders: StateFlow<List<ReminderItem>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedReminders: StateFlow<List<ReminderItem>> = repository.completedReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<ReminderItem>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeNotes: StateFlow<List<NoteItem>> = repository.activeNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<ProjectItem>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjectTasks: StateFlow<List<ProjectTaskItem>> = repository.allProjectTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<CalendarEventItem>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivityLogs: StateFlow<List<ActivityLogItem>> = repository.recentActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI & Interactive States
    val chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            sender = MessageSender.AI,
            message = "Namaste! I'm your Quick Remind AI Memory Assistant. Tell me anything once, and I'll remember it forever. Ask me about your projects, microcontroller specs, meetings, or tell me in English or Telugu:\n\n• \"What microcontroller am I using in my flight controller?\"\n• \"Repu morning 8 ki college ki vellali ani remind cheyyi\"\n• \"Show me everything related to C-DAC\""
        )
    ))
    val isAILoading = MutableStateFlow(false)
    val proactiveInsight = MutableStateFlow("Analyzing your memories and schedule...")
    val dailyBriefingText = MutableStateFlow<String?>(null)
    val dailyBriefingType = MutableStateFlow("MORNING") // "MORNING" or "EVENING"
    val isBriefingLoading = MutableStateFlow(false)

    // Voice & NLP Dialog States
    val isVoiceDialogOpen = MutableStateFlow(false)
    val voiceTranscriptionText = MutableStateFlow("")
    val isVoiceProcessing = MutableStateFlow(false)
    val voiceResultFeedback = MutableStateFlow<String?>(null)

    // Scanner
    val isScannerDialogOpen = MutableStateFlow(false)
    val isScanningImage = MutableStateFlow(false)

    // Creation Dialogs
    val showAddMemoryDialog = MutableStateFlow(false)
    val showAddReminderDialog = MutableStateFlow(false)
    val showAddNoteDialog = MutableStateFlow(false)
    val showAddProjectDialog = MutableStateFlow(false)

    // Filters
    val selectedMemoryCategory = MutableStateFlow<MemoryCategory?>(null)
    val selectedMemoryImportance = MutableStateFlow<MemoryImportance?>(null)
    val selectedProjectCategory = MutableStateFlow<String?>(null)

    // Privacy & PIN Lock
    val isAppLocked = MutableStateFlow(false)
    val appPinCode = MutableStateFlow("1234")
    val isPinRequired = MutableStateFlow(false)

    // TTS
    private var tts: TextToSpeech? = null
    val isTtsSpeaking = MutableStateFlow(false)

    init {
        initTts(application)
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            refreshProactiveInsight()
        }
    }

    private fun initTts(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
            }
        }
    }

    fun speakText(text: String) {
        tts?.let {
            if (it.isSpeaking) {
                it.stop()
                isTtsSpeaking.value = false
            } else {
                it.speak(text, TextToSpeech.QUEUE_FLUSH, null, "QuickRemindUtterance")
                isTtsSpeaking.value = true
            }
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        isTtsSpeaking.value = false
    }

    fun refreshProactiveInsight() {
        viewModelScope.launch {
            try {
                val insight = repository.getProactiveInsight()
                if (insight.isNotBlank() && !insight.startsWith("Error")) {
                    proactiveInsight.value = insight
                } else {
                    proactiveInsight.value = "Your Flight Controller testing task has been pending. Dedicate 30 mins to verify IMU SPI communication!"
                }
            } catch (e: Exception) {
                proactiveInsight.value = "Your Flight Controller testing task has been pending. Dedicate 30 mins to verify IMU SPI communication!"
            }
        }
    }

    fun generateMorningPlan() {
        viewModelScope.launch {
            isBriefingLoading.value = true
            dailyBriefingType.value = "MORNING"
            dailyBriefingText.value = repository.generateDailyPlan()
            isBriefingLoading.value = false
        }
    }

    fun generateEveningReview() {
        viewModelScope.launch {
            isBriefingLoading.value = true
            dailyBriefingType.value = "EVENING"
            dailyBriefingText.value = repository.generateEveningReview()
            isBriefingLoading.value = false
        }
    }

    fun closeDailyBriefing() {
        dailyBriefingText.value = null
    }

    // Natural Language Voice or Text execution
    fun executeNaturalLanguageCommand(rawInput: String) {
        if (rawInput.isBlank()) return
        viewModelScope.launch {
            isVoiceProcessing.value = true
            try {
                val result = repository.processNaturalLanguageCommand(rawInput)
                voiceResultFeedback.value = result.conversationalReply
                speakText(result.conversationalReply)
                // Also add to chat
                chatMessages.value = chatMessages.value + ChatMessage(
                    sender = MessageSender.USER,
                    message = rawInput
                ) + ChatMessage(
                    sender = MessageSender.AI,
                    message = result.conversationalReply,
                    detectedAction = result.intent
                )
            } catch (e: Exception) {
                voiceResultFeedback.value = "Processed: $rawInput"
            } finally {
                isVoiceProcessing.value = false
            }
        }
    }

    // AI Chat Assistant
    fun sendChatMessage(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            val userMsg = ChatMessage(sender = MessageSender.USER, message = query)
            chatMessages.value = chatMessages.value + userMsg
            isAILoading.value = true
            try {
                val answer = repository.queryAIAssistant(query)
                chatMessages.value = chatMessages.value + ChatMessage(
                    sender = MessageSender.AI,
                    message = answer
                )
                speakText(answer.take(300))
            } catch (e: Exception) {
                chatMessages.value = chatMessages.value + ChatMessage(
                    sender = MessageSender.AI,
                    message = "Error searching memory: ${e.localizedMessage}"
                )
            } finally {
                isAILoading.value = false
            }
        }
    }

    // Image memory scanning
    fun scanImage(bitmap: Bitmap) {
        viewModelScope.launch {
            isScanningImage.value = true
            try {
                val res = repository.scanImageMemory(bitmap)
                voiceResultFeedback.value = res.conversationalReply
                isScannerDialogOpen.value = false
            } catch (e: Exception) {
                voiceResultFeedback.value = "Failed to scan photo: ${e.message}"
            } finally {
                isScanningImage.value = false
            }
        }
    }

    // Memory CRUD
    fun addMemory(title: String, content: String, category: MemoryCategory, importance: MemoryImportance, isPermanent: Boolean, tags: List<String>, topic: String? = null) {
        viewModelScope.launch {
            repository.saveMemory(
                MemoryItem(
                    title = title,
                    content = content,
                    category = category,
                    importance = importance,
                    isPermanent = isPermanent,
                    tags = tags,
                    connectedTopic = topic
                )
            )
            showAddMemoryDialog.value = false
        }
    }

    fun updateMemory(memory: MemoryItem) {
        viewModelScope.launch { repository.updateMemory(memory) }
    }

    fun deleteMemory(memory: MemoryItem) {
        viewModelScope.launch { repository.deleteMemory(memory) }
    }

    // Reminder CRUD
    fun addReminder(title: String, notes: String, dueMillis: Long, priority: ReminderPriority, recurrence: RecurrenceType, category: String) {
        viewModelScope.launch {
            repository.saveReminder(
                ReminderItem(
                    title = title,
                    notes = notes,
                    dueDateMillis = dueMillis,
                    priority = priority,
                    recurrence = recurrence,
                    category = category
                )
            )
            showAddReminderDialog.value = false
        }
    }

    fun toggleReminder(reminder: ReminderItem) {
        viewModelScope.launch { repository.toggleReminderComplete(reminder) }
    }

    fun snoozeReminder(reminder: ReminderItem, minutes: Int = 15) {
        viewModelScope.launch { repository.snoozeReminder(reminder, minutes) }
    }

    fun deleteReminder(reminder: ReminderItem) {
        viewModelScope.launch { repository.deleteReminder(reminder) }
    }

    // Note CRUD
    fun addNote(title: String, content: String, category: String, tags: List<String>, colorHex: String) {
        viewModelScope.launch {
            repository.saveNote(
                NoteItem(
                    title = title,
                    content = content,
                    category = category,
                    tags = tags,
                    colorHex = colorHex
                )
            )
            showAddNoteDialog.value = false
        }
    }

    fun togglePinNote(note: NoteItem) {
        viewModelScope.launch { repository.togglePinNote(note) }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    // Project CRUD
    fun addProject(name: String, description: String, category: String, deadlineMillis: Long?, tasks: List<String>, colorHex: String) {
        viewModelScope.launch {
            repository.saveProject(
                ProjectItem(
                    name = name,
                    description = description,
                    category = category,
                    deadlineMillis = deadlineMillis,
                    colorHex = colorHex
                ),
                initialTasks = tasks
            )
            showAddProjectDialog.value = false
        }
    }

    fun toggleProjectTask(task: ProjectTaskItem) {
        viewModelScope.launch { repository.toggleProjectTask(task) }
    }

    fun addProjectTask(projectId: Long, title: String) {
        viewModelScope.launch { repository.addProjectTask(projectId, title) }
    }

    fun deleteProjectTask(task: ProjectTaskItem) {
        viewModelScope.launch { repository.deleteProjectTask(task) }
    }

    fun deleteProject(project: ProjectItem) {
        viewModelScope.launch { repository.deleteProject(project) }
    }

    // Calendar
    fun addCalendarEvent(title: String, description: String, startMillis: Long, endMillis: Long, location: String, category: String) {
        viewModelScope.launch {
            repository.saveCalendarEvent(
                CalendarEventItem(
                    title = title,
                    description = description,
                    startMillis = startMillis,
                    endMillis = endMillis,
                    location = location,
                    category = category
                )
            )
        }
    }

    // Export Data
    fun exportBackup(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataToJson()
            onExportReady(json)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
