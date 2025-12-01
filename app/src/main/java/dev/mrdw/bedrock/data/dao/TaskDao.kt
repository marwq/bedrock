package dev.mrdw.bedrock.data.dao

import androidx.room.*
import dev.mrdw.bedrock.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getAllActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("""
        SELECT * FROM tasks
        WHERE isCompleted = 0
        AND dueDate IS NOT NULL
        AND dueDate >= :startOfDay
        AND dueDate < :endOfDay
        ORDER BY dueDate ASC
    """)
    fun getTasksForToday(startOfDay: Long, endOfDay: Long): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks
        WHERE isCompleted = 0
        AND dueDate IS NOT NULL
        AND dueDate < :currentTime
        ORDER BY dueDate ASC
    """)
    fun getOverdueTasks(currentTime: Long): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks
        WHERE isCompleted = 0
        AND dueDate IS NOT NULL
        AND dueDate >= :currentTime
        ORDER BY dueDate ASC
    """)
    fun getUpcomingTasks(currentTime: Long): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateCompletionStatus(taskId: Long, isCompleted: Boolean, completedAt: Long?)
}
