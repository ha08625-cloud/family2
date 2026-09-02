package com.family2.todo.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single to-do item. [parentId] is null for a top-level task and points at
 * the owning task's [id] for a subtask, so a task can be broken down into
 * an arbitrary tree of subtasks.
 */
@Entity(
    tableName = "tasks",
    indices = [Index("parentId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isDone: Boolean = false,
    val parentId: Long? = null,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
