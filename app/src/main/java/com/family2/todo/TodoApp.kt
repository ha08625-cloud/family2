package com.family2.todo

import android.app.Application
import com.family2.todo.data.TaskDatabase
import com.family2.todo.data.TaskRepository
import com.family2.todo.widget.TodoWidget

class TodoApp : Application() {

    lateinit var repository: TaskRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val dao = TaskDatabase.getInstance(this).taskDao()
        repository = TaskRepository(dao) { TodoWidget().updateAll(this) }
    }
}
