package com.family2.todo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.family2.todo.data.Task
import com.family2.todo.data.TaskNode
import com.family2.todo.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TaskRepository) : ViewModel() {

    val tasks: StateFlow<List<TaskNode>> = repository.observeTree()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTask(title: String, parentId: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.addTask(title.trim(), parentId) }
    }

    fun setDone(task: Task, isDone: Boolean) {
        viewModelScope.launch { repository.setDone(task, isDone) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodoViewModel(repository) as T
        }
    }
}
