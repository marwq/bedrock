package dev.mrdw.bedrock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dev.mrdw.bedrock.data.Converters

@Entity(tableName = "habits")
@TypeConverters(Converters::class)
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val emojiIcon: String? = null,
    val color: String = "#6C5CE7",
    val timeHour: Int = 9, // 0-23
    val timeMinute: Int = 0, // 0-59
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // Format: yyyy-MM-dd
    val completed: Boolean = false,
    val completedAt: Long? = null
)
