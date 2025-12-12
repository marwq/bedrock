package dev.mrdw.bedrock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mrdw.bedrock.data.AppDatabase
import dev.mrdw.bedrock.data.model.Reminder
import dev.mrdw.bedrock.data.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository

    val activeReminders: StateFlow<List<Reminder>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReminderRepository(database.reminderDao())

        activeReminders = repository.getAllActiveReminders()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addReminder(title: String, reminderTime: Long, description: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertReminder(
                Reminder(
                    title = title.trim(),
                    description = description.trim(),
                    reminderTime = reminderTime,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleReminderCompletion(reminderId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateCompletionStatus(reminderId, isCompleted)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }
}
