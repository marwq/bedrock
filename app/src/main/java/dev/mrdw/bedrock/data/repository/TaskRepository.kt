package dev.mrdw.bedrock.data.repository

import dev.mrdw.bedrock.data.dao.TaskDao
import dev.mrdw.bedrock.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllActiveTasks(): Flow<List<Task>> = taskDao.getAllActiveTasks()

    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()

    fun getTasksForToday(startOfDay: Long, endOfDay: Long): Flow<List<Task>> =
        taskDao.getTasksForToday(startOfDay, endOfDay)

    fun getOverdueTasks(currentTime: Long): Flow<List<Task>> =
        taskDao.getOverdueTasks(currentTime)

    fun getUpcomingTasks(currentTime: Long): Flow<List<Task>> =
        taskDao.getUpcomingTasks(currentTime)

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(taskId: Long) = taskDao.deleteTaskById(taskId)

    suspend fun updateCompletionStatus(taskId: Long, isCompleted: Boolean) =
        taskDao.updateCompletionStatus(
            taskId,
            isCompleted,
            if (isCompleted) System.currentTimeMillis() else null
        )
}
