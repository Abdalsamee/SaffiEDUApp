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

// استورد شاشة المعلم الرئيسية هنا
// import com.example.saffieduapp.presentation.screens.teacher.home.TeacherHomeScreen

@Composable
fun TeacherNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.TEACHER_HOME_SCREEN, // نقطة البداية هي شاشة المعلم الرئيسية
        route = "teacher_main_graph",
        modifier = modifier
    ) {
        composable(Routes.TEACHER_HOME_SCREEN) {
             TeacherHomeScreen() // سنقوم ببنائها لاحقًا

        }
        composable(Routes.TEACHER_CLASSES_SCREEN) {
            PlaceholderScreen("Teacher Classes Screen")
        }
        composable(Routes.TEACHER_TASKS_SCREEN) {
            PlaceholderScreen("Teacher Tasks Screen")
        }
        composable(Routes.TEACHER_CHAT_SCREEN) {
            PlaceholderScreen("Teacher Chat Screen")
        }
        composable(Routes.TEACHER_PROFILE_SCREEN) {
            PlaceholderScreen("Teacher Profile Screen")
        }
    }
}

// مكون مؤقت للشاشات التي لم نقم ببنائها بعد
@Composable
private fun PlaceholderScreen(screenName: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = screenName)
    }
}