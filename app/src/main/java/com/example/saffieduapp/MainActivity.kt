package com.example.saffieduapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.navigation.AppNavGraph
//import com.example.saffieduapp.presentation.screens.signup.SignUpScreen
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaffiEDUAppTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
           // SignUpScreen()
        }
    }
}
