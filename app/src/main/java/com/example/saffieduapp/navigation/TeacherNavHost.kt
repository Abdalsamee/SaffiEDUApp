package com.example.saffieduapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.example.saffieduapp.presentation.screens.chat.ChatScreen
import com.example.saffieduapp.presentation.screens.teacher.home.TeacherHomeScreen
import com.example.saffieduapp.presentation.screens.teacher.add_alert.AddAlertScreen
import com.example.saffieduapp.presentation.screens.teacher.add_assignment.AddAssignmentScreen
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.AddLessonScreen
import com.example.saffieduapp.presentation.screens.teacher.calsses.TeacherClassesScreen
import com.example.saffieduapp.presentation.screens.teacher.profile.TeacherProfileScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TeacherNavHost(
    navController: NavHostController, // ✅ 3. استقبل الكنترولر كبارامتر
    modifier: Modifier = Modifier,
    onLogoutNavigate: () -> Unit
) {


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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

            TeacherClassesScreen()
        }
        teacherTasksNavGraph(navController)
        composable(Routes.CHAT_SCREEN) {
            ChatScreen(navController = navController)
        }
        composable(Routes.TEACHER_PROFILE_SCREEN) {
            TeacherProfileScreen(
                onLogoutNavigate = onLogoutNavigate, // مرّره (إذا كانت الشاشة تحتاجه للتنقل الداخلي)
                navController = navController // ✅ مرّر دالة الخروج
            )
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
