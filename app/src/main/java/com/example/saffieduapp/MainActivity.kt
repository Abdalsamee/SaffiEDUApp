package com.example.saffieduapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
<<<<<<< HEAD
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.navigation.AppNavGraph
import com.example.saffieduapp.presentation.screens.NewPasswordScreen
import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.OtpVerificationScreen.OtpVerificationScreen
import com.example.saffieduapp.presentation.screens.resetpassword.PasswordRecoveryScreen
//import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.PasswordRecoveryScreen
import com.example.saffieduapp.presentation.screens.terms.TermsAndConditionsScreen
//import com.example.saffieduapp.presentation.screens.signup.SignUpScreen
=======
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.navigation.authNavGraph
import com.example.saffieduapp.presentation.screens.MainAppScreen

>>>>>>> integration&UI
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f // تثبيت حجم الخط على القيمة الافتراضية
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
<<<<<<< HEAD

            NewPasswordScreen()
//            OtpVerificationScreen(
//                onContinueClicked = { code ->
//                    // هنا ضع ما يحدث بعد الضغط على متابعة
//                    println("رمز التحقق الذي أدخله المستخدم هو: $code")
//                }
//            )

//                    PasswordRecoveryScreen(
//                        onBackClicked = { finish() }  // يرجع للخلف عند الضغط
//                    )
                }
=======
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




>>>>>>> integration&UI
            }
        }

//        setContent {
//            PasswordRecoveryScreen()
//
////            TermsAndConditionsScreen(
////                onAccept = {
////                    // TODO: الانتقال إلى شاشة أخرى بعد الموافقة
////                    println("تمت الموافقة على الشروط")
////                },
////                onDecline = {
////                    // TODO: التعامل مع رفض الشروط
////                    println("تم رفض الشروط")
////                }
////            )
//
////            SaffiEDUAppTheme {
////                val navController = rememberNavController()
////                AppNavGraph(navController = navController)
////            }
//           // SignUpScreen()
//        }
//    }
//}
