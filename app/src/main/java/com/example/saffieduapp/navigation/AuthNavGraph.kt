package com.example.saffieduapp.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.SignUpScreen.SignUpScreen
import com.example.saffieduapp.presentation.screens.login.LoginScreen
import com.example.saffieduapp.presentation.screens.onboarding.OnboardingScreen
import com.example.saffieduapp.presentation.screens.splash.SplashScreen
import com.example.saffieduapp.presentation.screens.login.LoginViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.resetpassword.PasswordRecoveryScreen

@SuppressLint("ComposableDestinationInComposeScope")
fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(
        startDestination = Routes.SPLASH_SCREEN,
        route = Routes.AUTH_GRAPH
    ) {
        // 🔹 Splash Screen
        composable(Routes.SPLASH_SCREEN) {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        // 🔹 Onboarding Screen
        composable(Routes.ONBOARDING_SCREEN) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        popUpTo(Routes.ONBOARDING_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        // 🔹 Login Screen
        composable(Routes.LOGIN_SCREEN) { backStackEntry ->
            val loginViewModel: LoginViewModel = hiltViewModel(backStackEntry)

            LoginScreen(
                viewModel = loginViewModel,
                onStudentLogin = {
                    navController.navigate(Routes.MAIN_GRAPH) {
                        popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                    }
                },
                onTeacherLogin = {
                    navController.navigate(Routes.TEACHER_GRAPH) {
                        popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGNUP_SCREEN)
                },
                onNavigateToForgotPassword = { navController.navigate(Routes.RESET_PASSWORD_SCREEN) } // ✅ هنا التنقل إلى شاشة البريد
            )
        }

        // 🔹 ForgetPassword Screen
        composable(Routes.RESET_PASSWORD_SCREEN) {
            PasswordRecoveryScreen(
                onBackClicked = { navController.popBackStack() } // 🔙 للرجوع
            )
        }
        // 🔹 SignUp Screen
        composable(Routes.SIGNUP_SCREEN) {
            SignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        popUpTo(Routes.SIGNUP_SCREEN) { inclusive = true }
                    }
                }
            )
        }

    }

}