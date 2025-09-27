package com.example.saffieduapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.student.exam_details.ExamDetailsScreen
import com.example.saffieduapp.presentation.screens.student.tasks.TasksScreen

fun NavGraphBuilder.tasksNavGraph(navController: NavController) {
    navigation(
        startDestination = Routes.TASKS_SCREEN,
        route = Routes.TASKS_NAV_GRAPH
    ) {
        composable(Routes.TASKS_SCREEN) {
            TasksScreen(
                navController = navController
            )
        }
        composable(
            route = "${Routes.EXAM_DETAILS_SCREEN}/{examId}",
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) {
            ExamDetailsScreen(onNavigateUp = { navController.popBackStack() })
        }
    }
}