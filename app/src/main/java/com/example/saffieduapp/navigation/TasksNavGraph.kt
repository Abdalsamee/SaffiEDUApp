package com.example.saffieduapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.student.assignment_details.AssignmentDetailsScreen
import com.example.saffieduapp.presentation.screens.student.exam_details.ExamDetailsScreen
import com.example.saffieduapp.presentation.screens.student.exam_screen.ExamScreen
import com.example.saffieduapp.presentation.screens.student.submit_assignment.SubmitAssignmentScreen
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
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            ExamDetailsScreen(
                onNavigateUp = { navController.popBackStack() },
                onNavigateToExam = {
                    navController.navigate("${Routes.EXAM_SCREEN}/$examId")
                }
            )
        }

        composable(
            route = "${Routes.EXAM_SCREEN}/{examId}",
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) {
            ExamScreen(
                onNavigateUp = { navController.popBackStack() },
                onExamComplete = {
                    // TODO: الانتقال لشاشة النتائج أو الرجوع للمهام
                    navController.popBackStack(Routes.TASKS_SCREEN, inclusive = false)
                }
            )
        }

        composable(
            route = "${Routes.ASSIGNMENT_DETAILS_SCREEN}/{assignmentId}",
            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
        ) {
            AssignmentDetailsScreen(
                onNavigateUp = { navController.popBackStack() },
                onNavigateToSubmit = { assignmentId ->
                    navController.navigate("${Routes.SUBMIT_ASSIGNMENT_SCREEN}/$assignmentId")
                }
            )
        }

        composable(
            route = "${Routes.SUBMIT_ASSIGNMENT_SCREEN}/{assignmentId}",
            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
        ) {
            SubmitAssignmentScreen(onNavigateUp = { navController.popBackStack() })
        }




    }
}