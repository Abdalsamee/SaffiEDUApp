package com.example.saffieduapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam_answers.TeacherStudentExamAnswersScreen


@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.teacherTasksNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.TEACHER_TASKS_SCREEN, route = Routes.TEACHER_TASKS_GRAPH
    ) {
        // شاشة عرض قائمة المهام
        composable(Routes.TEACHER_TASKS_SCREEN) {
            TeacherTasksScreen(navController = navController)
        }

        // وجهة تفاصيل المهمة
        composable(
            route = "${Routes.TEACHER_TASK_DETAILS_SCREEN}/{taskId}/{taskType}", arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
                navArgument("taskType") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            // taskTypeString يتم استخدامه فقط في ViewModel
            // val taskTypeString = backStackEntry.arguments?.getString("taskType") ?: "ASSIGNMENT"

            TeacherTaskDetailsScreen(
                navController = navController,
                taskId = taskId,
            )
        }


        // وجهة تفاصيل واجب الطالب (ASSIGNMENT)
        composable(
            route = "${Routes.TEACHER_STUDENT_ASSIGNMENT_SCREEN}/{studentId}/{assignmentId}",
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType },
                navArgument("assignmentId") { type = NavType.StringType })
        ) {
            TeacherStudentAssignmentScreen(
                navController = navController,
                studentId = it.arguments?.getString("studentId") ?: "",
                assignmentId = it.arguments?.getString("assignmentId") ?: ""
            )
        }

        // 🟢 الوجهة الصحيحة لتفاصيل اختبار الطالب (EXAM)
        // يتم استخدام هذا المسار من شاشة تفاصيل المهمة
        composable(
            route = "${Routes.TEACHER_STUDENT_EXAM_SCREEN}/{studentId}/{examId}",
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType },
                navArgument("examId") {
                    type = NavType.StringType
                })
        ) { backStackEntry ->
            TeacherStudentExamScreen(
                navController = navController,
                studentId = backStackEntry.arguments?.getString("studentId") ?: "",
                examId = backStackEntry.arguments?.getString("examId") ?: ""
            )
        }

        // 🟢 الوجهة الصحيحة لشاشة إجابات اختبار الطالب (Exam Answers)
        composable(
            route = "${Routes.TEACHER_STUDENT_EXAM_ANSWERS_SCREEN_WITH_ARGS}/{studentId}/{examId}",
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType },
                navArgument("examId") {
                    type = NavType.StringType
                })
        ) { backStackEntry ->
            TeacherStudentExamAnswersScreen(
                navController = navController,
                studentId = backStackEntry.arguments?.getString("studentId") ?: "",
                examId = backStackEntry.arguments?.getString("examId") ?: ""
            )
        }
    }
}