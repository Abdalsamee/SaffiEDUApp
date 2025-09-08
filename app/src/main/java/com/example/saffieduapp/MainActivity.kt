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
import com.example.saffieduapp.navigation.authNavGraph
import com.example.saffieduapp.presentation.screens.MainAppScreen
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.AddLessonScreen


import com.example.saffieduapp.presentation.screens.teacher.add_lesson.AddLessonScreen



import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SaffiEDUAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "auth_graph"
                ) {
                    authNavGraph(navController)

                    composable(route = "main_graph") {
                       MainAppScreen()
                    }
                }

<<<<<<< HEAD
=======

>>>>>>> origin/main
            }

        }
    }
}
