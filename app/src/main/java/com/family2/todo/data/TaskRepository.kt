package com.family2.todo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** A task together with its subtasks, recursively. */
data class TaskNode(
    val task: Task,
    val children: List<TaskNode>
)

/**
 * [onChanged] is invoked after every mutation so callers (e.g. the home
 * screen widget) can refresh anything that doesn't observe the database
 * directly.
 */
class TaskRepository(
    private val dao: TaskDao,
    private val onChanged: suspend () -> Unit = {}
) {

    fun observeTree(): Flow<List<TaskNode>> {
        return dao.observeAll().map { all -> buildTree(all, parentId = null) }
    }

    private fun buildTree(all: List<Task>, parentId: Long?): List<TaskNode> {
        return all
            .filter { it.parentId == parentId }
            .map { task -> TaskNode(task, buildTree(all, task.id)) }
    }

    suspend fun addTask(title: String, parentId: Long? = null) {
        dao.insert(Task(title = title, parentId = parentId))
        onChanged()
    }

    suspend fun setDone(task: Task, isDone: Boolean) {
        dao.update(task.copy(isDone = isDone))
        onChanged()
    }

    suspend fun rename(task: Task, title: String) {
        dao.update(task.copy(title = title))
        onChanged()
    }

    suspend fun delete(task: Task) {
        deleteRecursively(task.id)
        onChanged()
    }

    private suspend fun deleteRecursively(taskId: Long) {
        val children = dao.getChildren(taskId)
        children.forEach { deleteRecursively(it.id) }
        dao.deleteById(taskId)
    }
}
