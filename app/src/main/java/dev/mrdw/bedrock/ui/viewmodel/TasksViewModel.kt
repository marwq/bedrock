package dev.mrdw.bedrock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.mrdw.bedrock.data.AppDatabase
import dev.mrdw.bedrock.data.model.Task
import dev.mrdw.bedrock.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository

    val allTasks: StateFlow<List<Task>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())

        allTasks = repository.getAllTasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title.trim(),
                    isCompleted = false
                )
            )
        }
    }

    fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateCompletionStatus(taskId, isCompleted)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}
