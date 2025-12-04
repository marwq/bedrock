package dev.mrdw.bedrock.data.dao

import androidx.room.*
import dev.mrdw.bedrock.data.model.Habit
import dev.mrdw.bedrock.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY timeHour ASC, timeMinute ASC")
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("UPDATE habits SET archived = 1 WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long)

    // Completions
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun getCompletion(habitId: Long, date: String): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions")
    fun getAllCompletions(): Flow<List<HabitCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)

    @Query("""
        SELECT COUNT(*) FROM habit_completions
        WHERE habitId = :habitId
        AND completed = 1
        AND date >= :startDate
        AND date <= :endDate
    """)
    suspend fun getCompletionCount(habitId: Long, startDate: String, endDate: String): Int
}
