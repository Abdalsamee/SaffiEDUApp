package com.example.saffieduapp.navigation

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.student.assignment_details.AssignmentDetailsScreen
import com.example.saffieduapp.presentation.screens.student.exam_details.ExamDetailsScreen
import com.example.saffieduapp.presentation.screens.student.exam_screen.ExamActivity
import com.example.saffieduapp.presentation.screens.student.submit_assignment.SubmitAssignmentScreen
import com.example.saffieduapp.presentation.screens.student.tasks.TasksScreen

fun NavGraphBuilder.tasksNavGraph(navController: NavController) {
    navigation(
        startDestination = Routes.TASKS_SCREEN,
        route = Routes.TASKS_NAV_GRAPH
    ) {
        // شاشة المهام
        composable(Routes.TASKS_SCREEN) {
            TasksScreen(
                navController = navController
            )
        }

        // شاشة تفاصيل الاختبار
        composable(
            route = "${Routes.EXAM_DETAILS_SCREEN}/{examId}",
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            val context = LocalContext.current // ✅ الحصول على Context

            ExamDetailsScreen(
                onNavigateUp = { navController.popBackStack() },
                onNavigateToExam = {
                    // ✅ الانتقال لـ ExamActivity
                    val intent = Intent(context, ExamActivity::class.java).apply {
                        putExtra("EXAM_ID", examId)
                    }
                    context.startActivity(intent)
                }
            )
        }

        // شاشة تفاصيل الواجب
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

        // شاشة تسليم الواجب
        composable(
            route = "${Routes.SUBMIT_ASSIGNMENT_SCREEN}/{assignmentId}",
            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
        ) {
            SubmitAssignmentScreen(onNavigateUp = { navController.popBackStack() })
        }


        composable(
            route = "${Routes.STUDENT_ASSIGNMENT_RESULT_SCREEN}/{assignmentId}",
            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: ""
            StudentAssignmentResultScreen(
                navController = navController,
                assignmentId = assignmentId
            )
        }

    }
}