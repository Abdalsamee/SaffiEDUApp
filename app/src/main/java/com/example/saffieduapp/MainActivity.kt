package com.example.saffieduapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

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
