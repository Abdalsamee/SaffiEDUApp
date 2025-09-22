package com.example.saffieduapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.teacher.add_alert.AddAlertScreen
import com.example.saffieduapp.presentation.screens.teacher.add_assignment.AddAssignmentScreen
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.AddLessonScreen
import com.example.saffieduapp.presentation.screens.teacher.home.TeacherHomeScreen

// --- ١. تم تغيير اسم ووظيفة الملف ---
// هذا هو المسار الرئيسي للمعلم الذي يتم استدعاؤه من MainActivity
fun NavGraphBuilder.teacherGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.TEACHER_HOME_SCREEN, // نقطة البداية هي الشاشة الرئيسية
        route = Routes.TEACHER_GRAPH
    ) {
        // --- ٢. هنا نعرّف كل شاشات المعلم ---
        composable(Routes.TEACHER_HOME_SCREEN) { TeacherHomeScreen(navController = navController) }
        composable(Routes.TEACHER_CLASSES_SCREEN) { /* ... */ }
        composable(Routes.TEACHER_TASKS_SCREEN) { /* ... */ }
        composable(Routes.TEACHER_CHAT_SCREEN) { /* ... */ }
        composable(Routes.TEACHER_PROFILE_SCREEN) { /* ... */ }

        // شاشات الإضافة
        composable(Routes.TEACHER_ADD_LESSON_SCREEN) { AddLessonScreen(onNavigateUp = { navController.popBackStack() }) }
        composable(Routes.TEACHER_ADD_ASSIGNMENT_SCREEN) { AddAssignmentScreen(onNavigateUp = { navController.popBackStack() }) }
        composable(Routes.TEACHER_ADD_ALERT_SCREEN) { AddAlertScreen(onNavigateUp = { navController.popBackStack() }) }

        // مسار إنشاء الاختبار
        createQuizNavGraph(navController)
    }
}