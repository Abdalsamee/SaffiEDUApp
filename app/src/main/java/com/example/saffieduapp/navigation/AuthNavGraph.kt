package com.example.saffieduapp.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.navArgument
import com.example.saffieduapp.presentation.screens.SignUpScreen.SignUpScreen
import com.example.saffieduapp.presentation.screens.login.LoginScreen
import com.example.saffieduapp.presentation.screens.onboarding.OnboardingScreen
import com.example.saffieduapp.presentation.screens.splash.SplashScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.SPLASH_SCREEN,
        route = Routes.AUTH_GRAPH   // استخدم ثابت بدل النصّ
    ) {
        composable(Routes.SPLASH_SCREEN) {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        // إزالة شاشة السبلاش من المكدس
                        popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.ONBOARDING_SCREEN) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        popUpTo(Routes.ONBOARDING_SCREEN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.LOGIN_SCREEN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN_GRAPH) {
                        // احذف كامل مخطط المصادقة من المكدس
                        popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGNUP_SCREEN) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.SIGNUP_SCREEN) {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        popUpTo(Routes.SIGNUP_SCREEN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSignUpSuccess = {
                    navController.navigate(Routes.MAIN_GRAPH) {
                        popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
