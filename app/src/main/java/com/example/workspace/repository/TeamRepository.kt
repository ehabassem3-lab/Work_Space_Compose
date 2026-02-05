package com.example.workspace.repository

import com.example.workspace.model.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object TeamRepository {
    // Mock Data
    private val _members = MutableStateFlow(
        listOf(
            Member("1", "Alice Johnson", 30),
            Member("2", "Bob Smith", 60),
            Member("3", "Charlie Davis", 10),
            Member("4", "Diana Prince", 85)
        )
    )
    val members = _members.asStateFlow()

    fun updateWorkload(memberId: String, newWorkload: Int) {
        _members.update { currentList ->
            currentList.map {
                if (it.id == memberId) it.copy(workload = newWorkload) else it
            }
        }
    }
}