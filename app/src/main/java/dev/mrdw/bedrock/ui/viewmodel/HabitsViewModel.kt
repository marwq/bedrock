package dev.mrdw.bedrock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mrdw.bedrock.data.AppDatabase
import dev.mrdw.bedrock.data.model.Habit
import dev.mrdw.bedrock.data.model.HabitCompletion
import dev.mrdw.bedrock.data.repository.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitWithCompletion(
    val habit: Habit,
    val isCompleted: Boolean,
    val streak: Int = 0
)

class HabitsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val habits: StateFlow<List<Habit>>
    val habitsWithCompletions: StateFlow<List<HabitWithCompletion>>

    private val completionsForDate: StateFlow<List<HabitCompletion>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao())

        habits = repository.getAllActiveHabits()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        completionsForDate = _selectedDate.flatMapLatest { date ->
            repository.getCompletionsForDate(date)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        habitsWithCompletions = combine(
            habits,
            completionsForDate,
            _selectedDate
        ) { habitsList, completions, selectedDate ->
            habitsList.filter { habit ->
                // Only show habits created on or before selected date
                val habitCreatedDate = LocalDate.ofEpochDay(habit.createdAt / (24 * 60 * 60 * 1000))
                !habitCreatedDate.isAfter(selectedDate)
            }.map { habit ->
                val completion = completions.find { it.habitId == habit.id }
                HabitWithCompletion(
                    habit = habit,
                    isCompleted = completion?.completed == true
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleHabitCompletion(habitId: Long) {
        val selectedDate = _selectedDate.value
        val today = LocalDate.now()

        // Only allow toggling for today or future dates
        if (!selectedDate.isBefore(today)) {
            viewModelScope.launch {
                repository.toggleCompletion(habitId, selectedDate)
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun getDaysOfWeek(): List<LocalDate> {
        val today = LocalDate.now()
        return (0..6).map { today.plusDays(it.toLong()) }
    }

    suspend fun getStreak(habitId: Long): Int {
        return repository.getCurrentStreak(habitId)
    }
}
