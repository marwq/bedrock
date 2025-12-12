package dev.mrdw.bedrock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mrdw.bedrock.data.AppDatabase
import dev.mrdw.bedrock.data.model.Reminder
import dev.mrdw.bedrock.data.model.RepeatInterval
import dev.mrdw.bedrock.data.repository.ReminderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository

    private val _reminderId = MutableStateFlow<Long?>(null)
    val reminderId: StateFlow<Long?> = _reminderId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _reminderTime = MutableStateFlow(System.currentTimeMillis())
    val reminderTime: StateFlow<Long> = _reminderTime.asStateFlow()

    private val _isRepeating = MutableStateFlow(false)
    val isRepeating: StateFlow<Boolean> = _isRepeating.asStateFlow()

    private val _repeatInterval = MutableStateFlow<RepeatInterval?>(null)
    val repeatInterval: StateFlow<RepeatInterval?> = _repeatInterval.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReminderRepository(database.reminderDao())

        // Set default time to current time + 1 hour
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        _reminderTime.value = calendar.timeInMillis
    }

    fun loadReminder(reminderId: Long) {
        _reminderId.value = reminderId
        viewModelScope.launch {
            repository.getReminderById(reminderId)?.let { reminder ->
                _title.value = reminder.title
                _description.value = reminder.description
                _reminderTime.value = reminder.reminderTime
                _isRepeating.value = reminder.isRepeating
                _repeatInterval.value = reminder.repeatInterval
            }
        }
    }

    fun updateTitle(title: String) {
        _title.value = title
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateReminderTime(timeInMillis: Long) {
        _reminderTime.value = timeInMillis
    }

    fun updateIsRepeating(isRepeating: Boolean) {
        _isRepeating.value = isRepeating
        if (!isRepeating) {
            _repeatInterval.value = null
        }
    }

    fun updateRepeatInterval(interval: RepeatInterval?) {
        _repeatInterval.value = interval
    }

    fun saveReminder(onSuccess: () -> Unit) {
        if (_title.value.isBlank()) return

        viewModelScope.launch {
            val reminder = Reminder(
                id = _reminderId.value ?: 0,
                title = _title.value.trim(),
                description = _description.value.trim(),
                reminderTime = _reminderTime.value,
                isRepeating = _isRepeating.value,
                repeatInterval = _repeatInterval.value,
                isCompleted = false
            )

            if (_reminderId.value == null) {
                repository.insertReminder(reminder)
            } else {
                repository.updateReminder(reminder)
            }

            onSuccess()
        }
    }

    fun deleteReminder(onSuccess: () -> Unit) {
        _reminderId.value?.let { id ->
            viewModelScope.launch {
                repository.deleteReminderById(id)
                onSuccess()
            }
        }
    }
}
