package com.example.workspace.model

import com.example.workspace.R

// --- Data Classes ---
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val projectName: String,
    val assignee: String,
    val date: String,
    val priority: PriorityLevel,
    val status: TaskStatus,
    val comments: List<String> = emptyList()
)

data class Member(
    val id: String,
    val name: String,
    val workload: Int // 0 to 100 representing percentage
)

// --- Enums ---
enum class PriorityLevel(val labelResId: Int) {
    LOW(R.string.priority_low),
    MEDIUM(R.string.priority_medium),
    HIGH(R.string.priority_high)
}

enum class TaskStatus {
    DRAFT,
    TODO,
    IN_PROGRESS,
    DONE,

}