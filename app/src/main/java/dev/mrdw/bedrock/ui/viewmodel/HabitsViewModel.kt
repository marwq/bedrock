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
    val allCompletions: Flow<List<HabitCompletion>>

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
            // Show ALL habits for any date (they are global, not date-specific)
            habitsList.map { habit ->
                // Check if this habit is completed on the selected date
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

        // Get all completions for calendar view
        allCompletions = repository.getAllCompletions()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleHabitCompletion(habitId: Long) {
        val selectedDate = _selectedDate.value
        val today = LocalDate.now()

        // Only allow toggling for today (not past, not future)
        if (selectedDate == today) {
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
        // Show today + next 6 days (7 days total)
        return (0..6).map { today.plusDays(it.toLong()) }
    }

    suspend fun getStreak(habitId: Long): Int {
        return repository.getCurrentStreak(habitId)
    }

    // Debug function to print database contents
    fun printDatabaseContents() {
        viewModelScope.launch {
            val allHabits = repository.getAllActiveHabits().first()
            val allCompletions = repository.getAllCompletions().first()

            android.util.Log.d("DATABASE", "========== HABITS ==========")
            allHabits.forEach { habit ->
                val createdDate = LocalDate.ofEpochDay(habit.createdAt / (24 * 60 * 60 * 1000))
                android.util.Log.d("DATABASE", "Habit: id=${habit.id}, name=${habit.name}, createdAt=$createdDate")
            }

            android.util.Log.d("DATABASE", "========== COMPLETIONS ==========")
            allCompletions.forEach { completion ->
                android.util.Log.d("DATABASE", "Completion: id=${completion.id}, habitId=${completion.habitId}, date=${completion.date}, completed=${completion.completed}")
            }

            android.util.Log.d("DATABASE", "Total habits: ${allHabits.size}, Total completions: ${allCompletions.size}")
        }
    }

    // Test function to add sample data
    fun addSampleData() {
        viewModelScope.launch {
            val today = LocalDate.now()

            // Create sample habits (created today - timestamp doesn't matter much now)
            repository.insertHabit(
                Habit(
                    name = "Morning Workout",
                    description = "30 minutes exercise",
                    emojiIcon = "🏃",
                    timeHour = 7,
                    timeMinute = 0,
                    createdAt = System.currentTimeMillis()
                )
            )

            repository.insertHabit(
                Habit(
                    name = "Read",
                    description = "30 minutes of reading",
                    emojiIcon = "📚",
                    timeHour = 21,
                    timeMinute = 0,
                    createdAt = System.currentTimeMillis()
                )
            )

            repository.insertHabit(
                Habit(
                    name = "Meditation",
                    description = "10 minutes mindfulness",
                    emojiIcon = "🧘",
                    timeHour = 8,
                    timeMinute = 30,
                    createdAt = System.currentTimeMillis()
                )
            )

            // No completions created - user will mark them manually for today
        }
    }
}
