package dev.mrdw.bedrock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long? = null,
    val taskId: Long? = null,
    val title: String,
    val description: String = "",
    val reminderTime: Long,
    val isRepeating: Boolean = false,
    val repeatInterval: RepeatInterval? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RepeatInterval {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
