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
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam_answers.TeacherStudentExamAnswersScreen


fun NavGraphBuilder.teacherTasksNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.TEACHER_TASKS_SCREEN, route = Routes.TEACHER_TASKS_GRAPH
    ) {
        // شاشة عرض قائمة المهام
        composable(Routes.TEACHER_TASKS_SCREEN) {
            TeacherTasksScreen(navController = navController)
        }

        // 🎯 FIX: تم تحديث المسار ليشمل taskId و taskType
        composable(
            route = "${Routes.TEACHER_TASK_DETAILS_SCREEN}/{taskId}/{taskType}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
                // يجب إضافة taskType هنا ليتمكن نظام التنقل من مطابقة المسار
                navArgument("taskType") { type = NavType.StringType })) {
            // لا حاجة لاستخراج الـ taskId هنا، الـ ViewModel سيفعل ذلك
            TeacherTaskDetailsScreen(
                navController = navController
            )
        }
        composable(Routes.TEACHER_STUDENT_ASSIGNMENT_SCREEN) {
            TeacherStudentAssignmentScreen(navController = navController)
        }

        composable(
            route = Routes.TEACHER_STUDENT_EXAM_ROUTE, arguments = listOf(
            navArgument("studentId") { type = NavType.StringType })) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            TeacherStudentExamScreen(
                navController = navController, examId = "demoExam", // يمكنك لاحقاً تمرير معرف حقيقي
                studentId = studentId
            )
        }

        composable(
            route = Routes.TEACHER_STUDENT_EXAM_ANSWERS_SCREEN_WITH_ARGS,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            TeacherStudentExamAnswersScreen()
        }

    }
}
