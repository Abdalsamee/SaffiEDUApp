package com.example.saffieduapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.login.LoginScreen
import com.example.saffieduapp.presentation.screens.onboarding.OnboardingScreen
import com.example.saffieduapp.presentation.screens.splash.SplashScreen

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        startDestination = Routes.SPLASH_SCREEN,
        route = "auth_graph"
    ) {
        composable(Routes.SPLASH_SCREEN) {

            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        // نحذف الـ Splash Screen من الـ back stack
                        popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ONBOARDING_SCREEN) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        popUpTo(Routes.ONBOARDING_SCREEN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN_SCREEN) {
            LoginScreen(
                onLoginSuccess = {
                    // عند نجاح تسجيل الدخول، انتقل للمسار الرئيسي
                    navController.navigate("main_graph") {
                        // احذف مسار المصادقة بالكامل من الـ back stack
                        popUpTo("auth_graph") {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}