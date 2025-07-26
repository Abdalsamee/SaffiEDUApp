package com.example.saffieduapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.saffieduapp.presentation.screens.splash.SplashScreen
import com.example.saffieduapp.presentation.screens.onboarding.OnboardingScreen
// import com.example.saffieduapp.presentation.screens.login.LoginScreen // ← لاحقًا

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("onboarding") {
            OnboardingScreen(navController)
        }


    }
}
