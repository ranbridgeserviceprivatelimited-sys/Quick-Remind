package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class MemoryImportance {
    HIGH, NORMAL, LOW
}

enum class MemoryCategory {
    PROJECT, PERSONAL, IDEA, DECISION, WORK, STUDY, FINANCE, HEALTH, GENERAL
}

@Entity(tableName = "memories")
@JsonClass(generateAdapter = true)
data class MemoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: MemoryCategory = MemoryCategory.GENERAL,
    val importance: MemoryImportance = MemoryImportance.NORMAL,
    val isPermanent: Boolean = true,
    val tags: List<String> = emptyList(),
    val connectedTopic: String? = null,
    val relatedEntityId: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

enum class ReminderPriority {
    HIGH, MEDIUM, LOW
}

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, CUSTOM
}

@Entity(tableName = "reminders")
@JsonClass(generateAdapter = true)
data class ReminderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueDateMillis: Long,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val snoozeCount: Int = 0,
    val locationTag: String? = null,
    val category: String = "General",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
@JsonClass(generateAdapter = true)
data class NoteItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val category: String = "Idea",
    val tags: List<String> = emptyList(),
    val photoUri: String? = null,
    val colorHex: String = "#1E293B",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

enum class ProjectStatus {
    ACTIVE, COMPLETED, ON_HOLD
}

@Entity(tableName = "projects")
@JsonClass(generateAdapter = true)
data class ProjectItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val category: String = "Engineering",
    val deadlineMillis: Long? = null,
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val nextStepSuggestion: String = "",
    val colorHex: String = "#6366F1",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_tasks")
@JsonClass(generateAdapter = true)
data class ProjectTaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val orderIndex: Int = 0,
    val completedAtMillis: Long? = null
)

@Entity(tableName = "calendar_events")
@JsonClass(generateAdapter = true)
data class CalendarEventItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean = false,
    val location: String = "",
    val category: String = "Meeting",
    val colorHex: String = "#8B5CF6"
)

@Entity(tableName = "activity_logs")
@JsonClass(generateAdapter = true)
data class ActivityLogItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionType: String, // MEMORY_ADDED, REMINDER_SET, TASK_DONE, NOTE_CREATED, PROJECT_UPDATED
    val title: String,
    val details: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val detectedAction: String? = null,
    val relatedItems: List<String> = emptyList()
)

enum class MessageSender {
    USER, AI
}
