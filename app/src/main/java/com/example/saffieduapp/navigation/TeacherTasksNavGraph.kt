package com.example.saffieduapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.teacher.tasks.TeacherTasksScreen
import com.example.saffieduapp.presentation.screens.teacher.tasks.details.TeacherTaskDetailsScreen


fun NavGraphBuilder.teacherTasksNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.TEACHER_TASKS_SCREEN,
        route = Routes.TEACHER_TASKS_GRAPH
    ) {
        // شاشة عرض قائمة المهام
        composable(Routes.TEACHER_TASKS_SCREEN) {
            TeacherTasksScreen(navController = navController)
        }

        composable(
            route = "${Routes.TEACHER_TASK_DETAILS_SCREEN}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TeacherTaskDetailsScreen(
                navController = navController
            ) // يمكنك تمرير taskId لاحقاً للـ ViewModel
        }


    }
}
