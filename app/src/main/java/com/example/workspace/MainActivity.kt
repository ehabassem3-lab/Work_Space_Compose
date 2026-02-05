package com.example.workspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.example.workspace.screens.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.workspace.ui.theme.WorkspaceTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkspaceTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    // --- 1. Home ---
                    composable("home") {
                        HomeScreen(onCreateTaskClick = { navController.navigate("create_task") })
                    }

                    // --- 2. Create Task (Handles both New and Edit/Draft) ---
                    composable(
                        route = "create_task?taskId={taskId}",
                        arguments = listOf(
                            navArgument("taskId") {
                                defaultValue = ""
                                type = NavType.StringType
                            }
                        )
                    ) {
                        CreateTaskScreen(
                            onDismiss = { navController.popBackStack() },
                            onNavigateToDrafts = { navController.navigate("drafts") }
                        )
                    }

                    // --- 3. Drafts Screen ---
                    composable("drafts") {
                        DraftsScreen(
                            onBack = { navController.popBackStack() },
                            onEditDraft = { taskId ->
                                // Navigate back to CreateTask and clear the draft screen from history
                                navController.navigate("create_task?taskId=$taskId") {
                                    popUpTo("home") { saveState = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}