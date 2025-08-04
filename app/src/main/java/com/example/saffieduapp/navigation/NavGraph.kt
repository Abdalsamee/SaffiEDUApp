package com.example.saffieduapp.navigation

//import SignUpScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.saffieduapp.presentation.screens.SignUpScreen.SignUpScreen
import com.example.saffieduapp.presentation.screens.login.LoginScreen
//import com.example.saffieduapp.presentation.screens.login.LoginScreen
//import com.example.saffieduapp.presentation.screens.login.LooginScreen
//import com.example.saffieduapp.presentation.screens.login.LoginScreen
import com.example.saffieduapp.presentation.screens.onboarding.OnboardingScreen
//import com.example.saffieduapp.presentation.screens.signup.SignUpScreen
//import com.example.saffieduapp.presentation.screens.signup.SignUpScreen
import com.example.saffieduapp.presentation.screens.splash.SplashScreen
import com.example.saffieduapp.presentation.screens.terms.TermsAndConditionsScreen

//import com.example.saffieduapp.ui.screens.SignUpScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_SCREEN
    ) {
        composable(Routes.SPLASH_SCREEN) {
            SplashScreen(navController)
        }

        composable(Routes.ONBOARDING_SCREEN) {
            OnboardingScreen(navController)
        }


        composable(Routes.LOGIN_SCREEN) {
            SignUpScreen()
            //LoginScreen()
            //TermsAndConditionsScreen()
        }
    }
}

