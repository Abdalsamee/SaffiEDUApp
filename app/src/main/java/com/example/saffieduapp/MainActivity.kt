package com.example.saffieduapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.navigation.authNavGraph
import com.example.saffieduapp.presentation.screens.MainAppScreen
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaffiEDUAppTheme {
//                val navController = rememberNavController()
//                AppNavGraph(navController = navController)

                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "auth_graph" // ابدأ دائماً بمسار المصادقة
                ) {
                    authNavGraph(navController) // مسار شاشات ما قبل الدخول

                    // تعريف الشاشة التي تحتوي على الـ Scaffold والـ NavGraph الداخلي
                    composable(route = "main_graph") {
                        MainAppScreen()
                    }
                }
            }
        }
    }
}
