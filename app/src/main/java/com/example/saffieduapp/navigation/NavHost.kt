package com.example.saffieduapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.saffieduapp.presentation.screens.MainAppScreen
import com.example.saffieduapp.presentation.screens.teacher.TeacherMainScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_SCREEN
    ) {
        // 🔹 Auth Graph
        authNavGraph(navController)

        // 🔹 Main App Graph
        composable(Routes.MAIN_GRAPH) {
            MainAppScreen(
                navController = navController
            )
        }

        // 🔹 Teacher Graph
        composable(Routes.TEACHER_GRAPH) {
            TeacherMainScreen(navController = navController)
        }
    }
}