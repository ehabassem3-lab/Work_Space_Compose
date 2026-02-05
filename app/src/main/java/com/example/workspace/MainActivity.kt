package com.example.workspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.workspace.screens.CreateTaskScreen
import com.example.workspace.screens.HomeScreen
import com.example.workspace.screens.HomeScreen
import com.example.workspace.viewmodel.CreateTaskViewModel // This should work now!

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(onCreateTaskClick = { navController.navigate("create_task") })
                        }
                        composable("create_task") {
                            CreateTaskScreen(onDismiss = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}