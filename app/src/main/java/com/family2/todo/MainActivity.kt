package com.family2.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.family2.todo.ui.TodoScreen
import com.family2.todo.ui.TodoViewModel
import com.family2.todo.ui.theme.Family2TodoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TodoViewModel by viewModels {
        TodoViewModel.Factory((application as TodoApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Family2TodoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }
    }
}
