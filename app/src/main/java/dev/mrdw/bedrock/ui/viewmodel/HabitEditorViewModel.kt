package dev.mrdw.bedrock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mrdw.bedrock.data.AppDatabase
import dev.mrdw.bedrock.data.model.Habit
import dev.mrdw.bedrock.data.repository.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class HabitEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository

    private val _habitId = MutableStateFlow<Long?>(null)

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _emojiIcon = MutableStateFlow<String?>(null)
    val emojiIcon: StateFlow<String?> = _emojiIcon.asStateFlow()

    private val _timeHour = MutableStateFlow(9)
    val timeHour: StateFlow<Int> = _timeHour.asStateFlow()

    private val _timeMinute = MutableStateFlow(0)
    val timeMinute: StateFlow<Int> = _timeMinute.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao())

        // Auto-save for existing habits with debounce
        viewModelScope.launch {
            combine(
                _habitId,
                _name,
                _description,
                _emojiIcon,
                _timeHour,
                _timeMinute
            ) { values: Array<*> ->
                val habitId = values[0] as Long?
                val name = values[1] as String
                val description = values[2] as String
                val emoji = values[3] as String?
                val hour = values[4] as Int
                val minute = values[5] as Int

                if (habitId != null && name.isNotBlank()) {
                    Habit(
                        id = habitId,
                        name = name,
                        description = description,
                        emojiIcon = emoji,
                        color = "#6C5CE7",
                        timeHour = hour,
                        timeMinute = minute,
                        createdAt = 0
                    )
                } else {
                    null
                }
            }
                .debounce(500) // Wait 500ms after last change
                .collect { habit ->
                    habit?.let { repository.updateHabit(it) }
                }
        }
    }

    fun loadHabit(habitId: Long) {
        _habitId.value = habitId
        viewModelScope.launch {
            repository.getHabitById(habitId)?.let { habit ->
                _name.value = habit.name
                _description.value = habit.description
                _emojiIcon.value = habit.emojiIcon
                _timeHour.value = habit.timeHour
                _timeMinute.value = habit.timeMinute
            }
        }
    }

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    fun updateEmojiIcon(emoji: String?) {
        _emojiIcon.value = emoji
    }

    fun updateTime(hour: Int, minute: Int) {
        _timeHour.value = hour
        _timeMinute.value = minute
    }

    fun applyTemplate(name: String, description: String, emoji: String) {
        _name.value = name
        _description.value = description
        _emojiIcon.value = emoji
    }

    fun loadHabitForDuplication(habitId: Long) {
        viewModelScope.launch {
            repository.getHabitById(habitId)?.let { habit ->
                _name.value = habit.name
                _description.value = habit.description
                _emojiIcon.value = habit.emojiIcon
                _timeHour.value = habit.timeHour
                _timeMinute.value = habit.timeMinute
                // Explicitly NOT setting _habitId so it's treated as new
            }
        }
    }

    suspend fun saveHabit(): Boolean {
        return try {
            if (_name.value.isBlank()) {
                return false
            }

            val habit = Habit(
                id = _habitId.value ?: 0,
                name = _name.value,
                description = _description.value,
                emojiIcon = _emojiIcon.value,
                color = "#6C5CE7",
                timeHour = _timeHour.value,
                timeMinute = _timeMinute.value,
                createdAt = if (_habitId.value == null) System.currentTimeMillis() else 0
            )

            if (_habitId.value == null) {
                repository.insertHabit(habit)
            } else {
                repository.updateHabit(habit)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteHabit(): Boolean {
        return try {
            _habitId.value?.let { id ->
                repository.getHabitById(id)?.let { habit ->
                    repository.deleteHabit(habit)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
