package com.example.saffieduapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.teacher.tasks.TeacherTasksScreen


fun NavGraphBuilder.teacherTasksNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.TEACHER_TASKS_SCREEN,
        route = Routes.TEACHER_TASKS_GRAPH
    ) {
        // شاشة عرض قائمة المهام
        composable(Routes.TEACHER_TASKS_SCREEN) {
            TeacherTasksScreen(navController = navController)
        }

//        // شاشة تفاصيل مهمة
//        composable(Routes.TEACHER_TASK_DETAILS_SCREEN + "/{taskId}") { backStackEntry ->
//            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
//            TaskDetailsScreen(navController = navController, taskId = taskId)
//        }


    }
}
