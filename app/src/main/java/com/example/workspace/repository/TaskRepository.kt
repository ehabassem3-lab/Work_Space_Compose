package com.example.workspace.repository

import com.example.workspace.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object TaskRepository {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    // Synchronous get for ViewModel logic
    fun getTaskById(id: String): Task? {
        return _tasks.value.find { it.id == id }
    }

    fun addTask(task: Task) {
        _tasks.update { it + task }
    }

    fun updateTask(updatedTask: Task) {
        _tasks.update { list ->
            list.map { if (it.id == updatedTask.id) updatedTask else it }
        }
    }
    // In TaskRepository.kt
    fun deleteTask(taskId: String) {
        _tasks.update { list -> list.filterNot { it.id == taskId } }
    }
}