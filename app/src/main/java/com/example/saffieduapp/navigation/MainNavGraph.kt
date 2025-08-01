package com.example.saffieduapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.saffieduapp.presentation.screens.student.home.HomeScreen

// Add imports for your other screens if they exist, or leave them for now

@Composable
fun MainNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME_SCREEN,
        route = "main_graph",
        modifier = modifier
    ) {
        // 1. Home Screen (Already done)
        composable(Routes.HOME_SCREEN) {
            HomeScreen()
        }

        // 2. Subjects Screen (Placeholder)
        composable(Routes.SUBJECT_SCREEN) {
            // Replace this with your actual SubjectsScreen later
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Subjects Screen")
            }
        }

        // 3. Tasks Screen (Placeholder)
        composable(Routes.TASKS_SCREEN) {
            // Replace this with your actual TasksScreen later
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Tasks Screen")
            }
        }

        // 4. Chat Screen (Placeholder)
        composable(Routes.CHAT_SCREEN) {
            // Replace this with your actual ChatScreen later
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Chat Screen")
            }
        }

        // 5. Profile Screen (Placeholder)
        composable(Routes.PROFILE_SCREEN) {
            // Replace this with your actual ProfileScreen later
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Profile Screen")
            }
        }
    }
}