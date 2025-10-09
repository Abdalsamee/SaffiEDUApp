package com.example.saffieduapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.saffieduapp.presentation.screens.teacher.home.TeacherHomeScreen
import com.example.saffieduapp.presentation.screens.teacher.add_alert.AddAlertScreen
import com.example.saffieduapp.presentation.screens.teacher.add_assignment.AddAssignmentScreen
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.AddLessonScreen
import com.example.saffieduapp.presentation.screens.teacher.calsses.TeacherClassesScreen

@Composable
fun TeacherNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.TEACHER_HOME_SCREEN,
        modifier = modifier
    ) {
        // Teacher main screens
        composable(Routes.TEACHER_HOME_SCREEN) {
            TeacherHomeScreen(navController = navController)
        }
        composable(Routes.TEACHER_CLASSES_SCREEN) {
            // TODO: Replace with actual screen
            TeacherClassesScreen()
        }
        teacherTasksNavGraph(navController)
        composable(Routes.TEACHER_CHAT_SCREEN) {

            TeacherChatScreen(navController = navController)
        }
        composable(Routes.TEACHER_PROFILE_SCREEN) {
            // TODO: Replace with actual screen
             TeacherProfileScreen(navController = navController)
        }

        // Additional screens
        composable(Routes.TEACHER_ADD_LESSON_SCREEN) {
            AddLessonScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(Routes.TEACHER_ADD_ASSIGNMENT_SCREEN) {
            AddAssignmentScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(Routes.TEACHER_ADD_ALERT_SCREEN) {
            AddAlertScreen(onNavigateUp = { navController.popBackStack() })
        }

        // Quiz creation navigation
        createQuizNavGraph(navController)
    }
}
// Add these placeholder screens temporarily to prevent crashes

@Composable
fun TeacherChatScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Teacher Chat Screen - Coming Soon")
    }
}

@Composable
fun TeacherProfileScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Teacher Profile Screen - Coming Soon")
    }
}