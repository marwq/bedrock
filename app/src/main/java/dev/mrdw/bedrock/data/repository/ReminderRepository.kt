package dev.mrdw.bedrock.data.repository

import dev.mrdw.bedrock.data.dao.ReminderDao
import dev.mrdw.bedrock.data.model.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getAllActiveReminders(): Flow<List<Reminder>> = reminderDao.getAllActiveReminders()

    fun getRemindersForNote(noteId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersForNote(noteId)

    fun getRemindersForTask(taskId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersForTask(taskId)

    fun getRemindersInRange(startTime: Long, endTime: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersInRange(startTime, endTime)

    suspend fun getReminderById(reminderId: Long): Reminder? =
        reminderDao.getReminderById(reminderId)

    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: Reminder) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) = reminderDao.deleteReminder(reminder)

    suspend fun deleteReminderById(reminderId: Long) = reminderDao.deleteReminderById(reminderId)

    suspend fun updateCompletionStatus(reminderId: Long, isCompleted: Boolean) =
        reminderDao.updateCompletionStatus(reminderId, isCompleted)
}
