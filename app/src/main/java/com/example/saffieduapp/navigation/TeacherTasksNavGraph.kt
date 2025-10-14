package com.example.saffieduapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.teacher.tasks.TeacherTasksScreen
import com.example.saffieduapp.presentation.screens.teacher.tasks.details.TeacherTaskDetailsScreen
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.assignmnet.TeacherStudentAssignmentScreen
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam.TeacherStudentExamScreen


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
            )
        }
        composable(Routes.TEACHER_STUDENT_ASSIGNMENT_SCREEN) {
            TeacherStudentAssignmentScreen(navController = navController)
        }

        composable(
            route = Routes.TEACHER_STUDENT_EXAM_ROUTE,
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            TeacherStudentExamScreen(
                navController = navController,
                examId = "demoExam", // يمكنك لاحقاً تمرير معرف حقيقي
                studentId = studentId
            )
        }

    }
}
