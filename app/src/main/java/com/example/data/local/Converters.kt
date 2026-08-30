package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.MemoryCategory
import com.example.data.model.MemoryImportance
import com.example.data.model.ProjectStatus
import com.example.data.model.RecurrenceType
import com.example.data.model.ReminderPriority

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString("|||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|||").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromMemoryCategory(value: MemoryCategory): String = value.name

    @TypeConverter
    fun toMemoryCategory(value: String): MemoryCategory =
        try { MemoryCategory.valueOf(value) } catch (e: Exception) { MemoryCategory.GENERAL }

    @TypeConverter
    fun fromMemoryImportance(value: MemoryImportance): String = value.name

    @TypeConverter
    fun toMemoryImportance(value: String): MemoryImportance =
        try { MemoryImportance.valueOf(value) } catch (e: Exception) { MemoryImportance.NORMAL }

    @TypeConverter
    fun fromReminderPriority(value: ReminderPriority): String = value.name

    @TypeConverter
    fun toReminderPriority(value: String): ReminderPriority =
        try { ReminderPriority.valueOf(value) } catch (e: Exception) { ReminderPriority.MEDIUM }

    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType): String = value.name

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType =
        try { RecurrenceType.valueOf(value) } catch (e: Exception) { RecurrenceType.NONE }

    @TypeConverter
    fun fromProjectStatus(value: ProjectStatus): String = value.name

    @TypeConverter
    fun toProjectStatus(value: String): ProjectStatus =
        try { ProjectStatus.valueOf(value) } catch (e: Exception) { ProjectStatus.ACTIVE }
}
