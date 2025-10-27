package com.example.saffieduapp

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.navigation.Routes
import com.example.saffieduapp.navigation.authNavGraph
import com.example.saffieduapp.presentation.screens.MainAppScreen
import com.example.saffieduapp.presentation.screens.teacher.TeacherMainScreen
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            SaffiEDUAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController, startDestination = Routes.AUTH_GRAPH
                ) {
                    authNavGraph(navController)


                    composable(route = Routes.MAIN_GRAPH) {
                        MainAppScreen(
                            // ✅ هنا نمرر المنطق الصحيح
                            onLogoutNavigate = {
                                navController.navigate(Routes.AUTH_GRAPH) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            })
                    }


                    // Define the teacher main screen here instead of teacherGraph
                    composable(route = Routes.TEACHER_GRAPH) {
                        TeacherMainScreen(
                            onLogoutNavigate = {
                                navController.navigate(Routes.AUTH_GRAPH) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            })
                    }
                }
            }


        }
    }
}