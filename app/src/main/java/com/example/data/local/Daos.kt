package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY importance = 'HIGH' DESC, createdAtMillis DESC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories WHERE isPermanent = 1 ORDER BY createdAtMillis DESC")
    fun getPermanentMemories(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories WHERE isPermanent = 0 ORDER BY createdAtMillis DESC")
    fun getShortTermMemories(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY createdAtMillis DESC")
    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR connectedTopic LIKE '%' || :query || '%'")
    fun searchMemories(query: String): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories")
    suspend fun getAllMemoriesDirect(): List<MemoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryItem): Long

    @Update
    suspend fun updateMemory(memory: MemoryItem)

    @Delete
    suspend fun deleteMemory(memory: MemoryItem)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM memories WHERE isPermanent = 0 AND createdAtMillis < :cutoffMillis")
    suspend fun purgeOldShortTermMemories(cutoffMillis: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueDateMillis ASC")
    fun getActiveReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY completedAtMillis DESC")
    fun getCompletedReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders ORDER BY dueDateMillis ASC")
    fun getAllReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersDirect(): List<ReminderItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderItem): Long

    @Update
    suspend fun updateReminder(reminder: ReminderItem)

    @Delete
    suspend fun deleteReminder(reminder: ReminderItem)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAtMillis DESC")
    fun getActiveNotes(): Flow<List<NoteItem>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAtMillis DESC")
    fun getArchivedNotes(): Flow<List<NoteItem>>

    @Query("SELECT * FROM notes ORDER BY updatedAtMillis DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesDirect(): List<NoteItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem): Long

    @Update
    suspend fun updateNote(note: NoteItem)

    @Delete
    suspend fun deleteNote(note: NoteItem)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY status = 'ACTIVE' DESC, createdAtMillis DESC")
    fun getAllProjects(): Flow<List<ProjectItem>>

    @Query("SELECT * FROM project_tasks WHERE projectId = :projectId ORDER BY orderIndex ASC, id ASC")
    fun getTasksForProject(projectId: Long): Flow<List<ProjectTaskItem>>

    @Query("SELECT * FROM project_tasks ORDER BY isCompleted ASC, id ASC")
    fun getAllProjectTasks(): Flow<List<ProjectTaskItem>>

    @Query("SELECT * FROM projects")
    suspend fun getAllProjectsDirect(): List<ProjectItem>

    @Query("SELECT * FROM project_tasks")
    suspend fun getAllTasksDirect(): List<ProjectTaskItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectItem): Long

    @Update
    suspend fun updateProject(project: ProjectItem)

    @Delete
    suspend fun deleteProject(project: ProjectItem)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ProjectTaskItem): Long

    @Update
    suspend fun updateTask(task: ProjectTaskItem)

    @Delete
    suspend fun deleteTask(task: ProjectTaskItem)

    @Query("DELETE FROM project_tasks WHERE projectId = :projectId")
    suspend fun deleteTasksByProject(projectId: Long)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events WHERE startMillis >= :startOfDay AND startMillis <= :endOfDay ORDER BY startMillis ASC")
    fun getEventsForRange(startOfDay: Long, endOfDay: Long): Flow<List<CalendarEventItem>>

    @Query("SELECT * FROM calendar_events ORDER BY startMillis ASC")
    fun getAllEvents(): Flow<List<CalendarEventItem>>

    @Query("SELECT * FROM calendar_events")
    suspend fun getAllEventsDirect(): List<CalendarEventItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventItem): Long

    @Update
    suspend fun updateEvent(event: CalendarEventItem)

    @Delete
    suspend fun deleteEvent(event: CalendarEventItem)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestampMillis DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<ActivityLogItem>>

    @Query("SELECT * FROM activity_logs")
    suspend fun getAllLogsDirect(): List<ActivityLogItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogItem): Long

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)
}
