package com.example.saffieduapp.navigation

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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.saffieduapp.presentation.screens.student.video_player.VideoPlayerScreen
import com.example.saffieduapp.presentation.screens.teacher.TeacherMainScreen
import com.example.saffieduapp.presentation.screens.teacher.add_alert.AddAlertScreen
import com.example.saffieduapp.presentation.screens.teacher.add_assignment.AddAssignmentScreen
import com.example.saffieduapp.presentation.screens.teacher.add_exam.AddExamScreen
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.AddLessonScreen

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
                    navController.navigate(Routes.TEACHER_MAIN_SCREEN) {
                        popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGNUP_SCREEN)
                }
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


        composable(Routes.TEACHER_MAIN_SCREEN) {
            TeacherMainScreen(
                navController = navController as NavHostController,
                onNavigateToAddLesson = {
                    navController.navigate(Routes.TEACHER_ADD_LESSON_SCREEN)
                },
                onNavigateToAddAlert = {
                    navController.navigate(Routes.TEACHER_ADD_ALERT_SCREEN)
                },
                onNavigateToAddAssignmnet = {
                    navController.navigate(Routes.TEACHER_ADD_ASSIGNMENT_SCREEN)
                },
                onNavigateToAddExam = {
                    navController.navigate(Routes.TEACHER_ADD_EXAM_SCREEN)
                }
            )
        }
        composable(route = Routes.TEACHER_ADD_LESSON_SCREEN) {
            AddLessonScreen(onNavigateUp = { navController.popBackStack() })
        }

        composable(route = Routes.TEACHER_ADD_ALERT_SCREEN) {
            AddAlertScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(route = Routes.TEACHER_ADD_ASSIGNMENT_SCREEN) {
            AddAssignmentScreen(onNavigateUp = { navController.popBackStack() })
        }
        composable(route = Routes.TEACHER_ADD_EXAM_SCREEN) {
            AddExamScreen(onNavigateUp = { navController.popBackStack() })
        }
    }

}