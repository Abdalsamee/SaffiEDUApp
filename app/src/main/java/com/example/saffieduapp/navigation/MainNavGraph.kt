package com.example.saffieduapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.saffieduapp.presentation.screens.student.chat.ChatScreen
import com.example.saffieduapp.presentation.screens.student.home.HomeScreen
import com.example.saffieduapp.presentation.screens.student.profile.ProfileScreen
import com.example.saffieduapp.presentation.screens.student.subjects.SubjectScreen

import com.example.saffieduapp.presentation.screens.student.task.TasksScreen

@Composable
fun MainNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME_SCREEN,
        route = "main_graph",
        modifier = modifier
    ) {
        composable(Routes.HOME_SCREEN) {
            HomeScreen()
        }

        // ٢. تم استبدال الواجهة المؤقتة بالشاشة الحقيقية
        composable(Routes.SUBJECTS_SCREEN) { // تأكد من أن اسم المسار صحيح
            SubjectScreen()
        }

        // بقية الشاشات (لا تزال مؤقتة)
        composable(Routes.TASKS_SCREEN) {
            PlaceholderScreen("Tasks Screen")
        }
        composable(Routes.CHAT_SCREEN) {
            PlaceholderScreen("Chat Screen")
        }
        composable(Routes.PROFILE_SCREEN) {
            PlaceholderScreen("Profile Screen")
        }
    }
}

// يمكنك إبقاء هذا المكون المؤقت للشاشات الأخرى
@Composable
private fun PlaceholderScreen(screenName: String) {
    // ...
}