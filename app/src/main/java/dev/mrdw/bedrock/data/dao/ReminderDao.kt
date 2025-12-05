package dev.mrdw.bedrock.data.dao

import androidx.room.*
import dev.mrdw.bedrock.data.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY reminderTime ASC")
    fun getAllActiveReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE noteId = :noteId")
    fun getRemindersForNote(noteId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE taskId = :taskId")
    fun getRemindersForTask(taskId: Long): Flow<List<Reminder>>

    @Query("""
        SELECT * FROM reminders
        WHERE isCompleted = 0
        AND reminderTime >= :startTime
        AND reminderTime < :endTime
        ORDER BY reminderTime ASC
    """)
    fun getRemindersInRange(startTime: Long, endTime: Long): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: Long)

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE id = :reminderId")
    suspend fun updateCompletionStatus(reminderId: Long, isCompleted: Boolean)
}
