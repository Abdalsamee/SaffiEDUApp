package com.example.saffieduapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.SignUpScreen.SignUpScreen
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
                    navController.navigate("main_graph") {
                        popUpTo("auth_graph") {
                            inclusive = true
                        }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGNUP_SCREEN)
                }
            )
        }

        composable(Routes.SIGNUP_SCREEN) {
            SignUpScreen(
                onBackClick = {
                    navController.popBackStack() // للرجوع إلى الخلف
                },
                // --- التعديل هنا ---
                // أضفنا هنا منطق الانتقال من شاشة التسجيل إلى شاشة الدخول
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        // نحذف شاشة التسجيل الجديدة من المكدس لمنع العودة إليها بالخطأ
                        popUpTo(Routes.SIGNUP_SCREEN) { inclusive = true }
                    }
                }
            )
        }
    }
}