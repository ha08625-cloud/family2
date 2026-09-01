package com.family2.todo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.family2.todo.data.Task
import com.family2.todo.data.TaskNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val expanded = remember { mutableStateMapOf<Long, Boolean>() }
    var newTaskTitle by remember { mutableStateOf("") }
    var addingSubtaskFor by remember { mutableStateOf<Long?>(null) }
    var newSubtaskTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("To-Do") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                TextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text("Add a task") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    viewModel.addTask(newTaskTitle)
                    newTaskTitle = ""
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add task")
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks, key = { it.task.id }) { node ->
                    TaskTree(
                        node = node,
                        depth = 0,
                        expanded = expanded,
                        addingSubtaskFor = addingSubtaskFor,
                        newSubtaskTitle = newSubtaskTitle,
                        onToggleExpand = { id -> expanded[id] = !(expanded[id] ?: true) },
                        onToggleDone = { task, done -> viewModel.setDone(task, done) },
                        onDelete = { task -> viewModel.delete(task) },
                        onStartAddSubtask = { id ->
                            addingSubtaskFor = id
                            newSubtaskTitle = ""
                        },
                        onSubtaskTitleChange = { newSubtaskTitle = it },
                        onConfirmSubtask = { parentId ->
                            viewModel.addTask(newSubtaskTitle, parentId)
                            addingSubtaskFor = null
                            newSubtaskTitle = ""
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskTree(
    node: TaskNode,
    depth: Int,
    expanded: androidx.compose.runtime.snapshots.SnapshotStateMap<Long, Boolean>,
    addingSubtaskFor: Long?,
    newSubtaskTitle: String,
    onToggleExpand: (Long) -> Unit,
    onToggleDone: (Task, Boolean) -> Unit,
    onDelete: (Task) -> Unit,
    onStartAddSubtask: (Long) -> Unit,
    onSubtaskTitleChange: (String) -> Unit,
    onConfirmSubtask: (Long) -> Unit
) {
    val isExpanded = expanded[node.task.id] ?: true

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 20).dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            if (node.children.isNotEmpty()) {
                IconButton(onClick = { onToggleExpand(node.task.id) }) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = "Toggle subtasks"
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            Checkbox(
                checked = node.task.isDone,
                onCheckedChange = { onToggleDone(node.task, it) }
            )
            Text(
                text = node.task.title,
                modifier = Modifier.weight(1f),
                textDecoration = if (node.task.isDone) TextDecoration.LineThrough else TextDecoration.None
            )
            IconButton(onClick = { onStartAddSubtask(node.task.id) }) {
                Icon(Icons.Default.Add, contentDescription = "Add subtask")
            }
            IconButton(onClick = { onDelete(node.task) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete task")
            }
        }

        if (addingSubtaskFor == node.task.id) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = ((depth + 1) * 20).dp, bottom = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                TextField(
                    value = newSubtaskTitle,
                    onValueChange = onSubtaskTitleChange,
                    placeholder = { Text("Add a subtask") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onConfirmSubtask(node.task.id) }) {
                    Icon(Icons.Default.Add, contentDescription = "Confirm subtask")
                }
            }
        }

        if (isExpanded) {
            node.children.forEach { child ->
                TaskTree(
                    node = child,
                    depth = depth + 1,
                    expanded = expanded,
                    addingSubtaskFor = addingSubtaskFor,
                    newSubtaskTitle = newSubtaskTitle,
                    onToggleExpand = onToggleExpand,
                    onToggleDone = onToggleDone,
                    onDelete = onDelete,
                    onStartAddSubtask = onStartAddSubtask,
                    onSubtaskTitleChange = onSubtaskTitleChange,
                    onConfirmSubtask = onConfirmSubtask
                )
            }
        }
    }
}
