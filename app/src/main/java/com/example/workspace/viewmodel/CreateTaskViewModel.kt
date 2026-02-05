package com.example.workspace.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.workspace.model.PriorityLevel
import com.example.workspace.model.Task
import com.example.workspace.model.TaskStatus
import com.example.workspace.repository.TaskRepository
import com.example.workspace.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

// Screen State
data class CreateTaskState(
    val title: String = "",
    val description: String = "",
    val projectName: String = "",
    val assignee: String = "",
    val date: String = "",
    val priority: PriorityLevel = PriorityLevel.HIGH,
    val status: TaskStatus = TaskStatus.IN_PROGRESS,
    val taskId: String? = null,
    val isEditMode: Boolean = false,
    val isTitleError: Boolean = false,
    val isDescError: Boolean = false,
    val isProjectError: Boolean = false,
    val isAssigneeError: Boolean = false,
    val isDateError: Boolean = false
)

// ViewModel Logic
class CreateTaskViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTaskState())
    val state = _state.asStateFlow()

    // Dynamic List
    val availableAssignees = TeamRepository.members.map { list -> list.map { it.name } }

    init {
        val taskId = savedStateHandle.get<String>("taskId")
        if (taskId != null) loadTask(taskId)
    }

    private fun loadTask(id: String) {
        val task = TaskRepository.getTaskById(id)
        task?.let { t ->
            _state.update {
                it.copy(
                    taskId = t.id,
                    isEditMode = true,
                    title = t.title,
                    description = t.description,
                    projectName = t.projectName,
                    assignee = t.assignee,
                    date = t.date,
                    priority = t.priority,
                    status = t.status
                )
            }
        }
    }

    // Input Handlers
    fun onNameChange(v: String) { _state.update { it.copy(title = v, isTitleError = false) } }
    fun onDescriptionChange(v: String) { _state.update { it.copy(description = v, isDescError = false) } }
    fun onProjectChange(v: String) { _state.update { it.copy(projectName = v, isProjectError = false) } }
    fun onAssigneeChange(v: String) { _state.update { it.copy(assignee = v, isAssigneeError = false) } }
    fun onDateChange(v: String) { _state.update { it.copy(date = v, isDateError = false) } }
    fun onPriorityChange(v: PriorityLevel) { _state.update { it.copy(priority = v) } }

    // Save Logic
    fun saveTask(onSuccess: () -> Unit, onError: () -> Unit) {
        val s = _state.value

        // Validation Check
        val hasError = s.title.isBlank() || s.description.isBlank() ||
                s.projectName.isBlank() || s.assignee.isBlank() || s.date.isBlank()

        if (hasError) {
            _state.update {
                it.copy(
                    isTitleError = s.title.isBlank(),
                    isDescError = s.description.isBlank(),
                    isProjectError = s.projectName.isBlank(),
                    isAssigneeError = s.assignee.isBlank(),
                    isDateError = s.date.isBlank()
                )
            }
            onError()
            return
        }

        // Task Model
        val taskToSave = Task(
            id = s.taskId ?: UUID.randomUUID().toString(),
            title = s.title,
            description = s.description,
            projectName = s.projectName.trim().lowercase(),
            assignee = s.assignee,
            date = s.date,
            priority = s.priority,
            status = s.status,
            comments = TaskRepository.getTaskById(s.taskId ?: "")?.comments ?: emptyList()
        )

        // Data Sync
        if (s.isEditMode) {
            TaskRepository.updateTask(taskToSave)
        } else {
            TaskRepository.addTask(taskToSave)

            // Workload Update
            val member = TeamRepository.members.value.find { it.name == s.assignee }
            member?.let {
                TeamRepository.updateWorkload(it.id, it.workload + 15)
            }
        }

        onSuccess()
    }
}