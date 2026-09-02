package com.family2.todo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.family2.todo.data.Task
import com.family2.todo.data.TaskDatabase
import com.family2.todo.data.TaskNode
import com.family2.todo.data.TaskRepository
import kotlinx.coroutines.flow.first

/** Flattened, indented row used to render a [TaskNode] tree in a Glance LazyColumn. */
private data class WidgetRow(val task: Task, val depth: Int)

private fun flatten(nodes: List<TaskNode>, depth: Int = 0): List<WidgetRow> =
    nodes.flatMap { node -> listOf(WidgetRow(node.task, depth)) + flatten(node.children, depth + 1) }

class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = TaskDatabase.getInstance(context).taskDao()
        val repository = TaskRepository(dao)
        val tree = repository.observeTree().first()
        val rows = flatten(tree)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Text(
                    text = "To-Do",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
                if (rows.isEmpty()) {
                    Text(text = "No tasks yet. Open the app to add one.")
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                        items(rows, itemId = { it.task.id }) { row -> TaskRow(row) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(row: WidgetRow) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = (row.depth * 16).dp, top = 4.dp, bottom = 4.dp)
            .clickable(actionRunCallback<ToggleTaskAction>(
                actionParametersOf(TaskIdKey to row.task.id, TaskDoneKey to !row.task.isDone)
            ))
    ) {
        Text(
            text = if (row.task.isDone) "☑" else "☐",
            style = TextStyle(fontSize = 14.sp)
        )
        Text(
            text = " " + row.task.title,
            style = TextStyle(
                fontSize = 14.sp,
                textDecoration = if (row.task.isDone) TextDecoration.LineThrough else TextDecoration.None
            )
        )
    }
}

private val TaskIdKey = ActionParameters.Key<Long>("task_id")
private val TaskDoneKey = ActionParameters.Key<Boolean>("task_done")

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TaskIdKey] ?: return
        val isDone = parameters[TaskDoneKey] ?: return
        val dao = TaskDatabase.getInstance(context).taskDao()
        val repository = TaskRepository(dao) { TodoWidget().updateAll(context) }
        val task = dao.getById(taskId) ?: return
        repository.setDone(task, isDone)
    }
}
