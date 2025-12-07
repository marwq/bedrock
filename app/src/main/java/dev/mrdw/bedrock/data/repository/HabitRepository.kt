package dev.mrdw.bedrock.data.repository

import dev.mrdw.bedrock.data.dao.HabitDao
import dev.mrdw.bedrock.data.model.Habit
import dev.mrdw.bedrock.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(private val habitDao: HabitDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getAllActiveHabits(): Flow<List<Habit>> = habitDao.getAllActiveHabits()

    suspend fun getHabitById(habitId: Long): Habit? = habitDao.getHabitById(habitId)

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun archiveHabit(habitId: Long) = habitDao.archiveHabit(habitId)

    // Completions
    suspend fun getCompletion(habitId: Long, date: LocalDate): HabitCompletion? {
        return habitDao.getCompletion(habitId, date.format(dateFormatter))
    }

    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>> =
        habitDao.getCompletionsForHabit(habitId)

    fun getCompletionsForDate(date: LocalDate): Flow<List<HabitCompletion>> =
        habitDao.getCompletionsForDate(date.format(dateFormatter))

    fun getAllCompletions(): Flow<List<HabitCompletion>> =
        habitDao.getAllCompletions()

    suspend fun toggleCompletion(habitId: Long, date: LocalDate) {
        val dateString = date.format(dateFormatter)
        val existing = habitDao.getCompletion(habitId, dateString)

        if (existing == null) {
            habitDao.insertCompletion(
                HabitCompletion(
                    habitId = habitId,
                    date = dateString,
                    completed = true,
                    completedAt = System.currentTimeMillis()
                )
            )
        } else {
            if (existing.completed) {
                habitDao.deleteCompletion(habitId, dateString)
            } else {
                habitDao.insertCompletion(
                    existing.copy(
                        completed = true,
                        completedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun getCurrentStreak(habitId: Long): Int {
        val today = LocalDate.now()
        var streak = 0
        var currentDate = today

        while (true) {
            val completion = getCompletion(habitId, currentDate)
            if (completion?.completed == true) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    suspend fun getCompletionRate(habitId: Long, days: Int): Float {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong() - 1)

        val habit = getHabitById(habitId) ?: return 0f
        val habitCreatedDate = LocalDate.ofEpochDay(habit.createdAt / (24 * 60 * 60 * 1000))

        val actualStartDate = if (habitCreatedDate.isAfter(startDate)) habitCreatedDate else startDate

        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(actualStartDate, endDate) + 1
        val completedDays = habitDao.getCompletionCount(
            habitId,
            actualStartDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )

        return if (totalDays > 0) completedDays.toFloat() / totalDays.toFloat() else 0f
    }
}
