package com.example.saffieduapp

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.navigation.authNavGraph
import com.example.saffieduapp.presentation.screens.MainAppScreen
import com.example.saffieduapp.presentation.screens.NewPasswordScreen
import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.OtpVerificationScreen.OtpVerificationScreen
//import com.example.saffieduapp.navigation.authNavGraph
//import com.example.saffieduapp.presentation.screens.MainAppScreen
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SaffiEDUAppTheme {

<<<<<<< HEAD
                    composable(route = "main_graph") {
                       MainAppScreen()
=======
                NewPasswordScreen(
                    onBackClicked = {
                        // هنا ضع كود الرجوع أو التنقل للخلف
                    },
                    onContinueClicked = { password, confirmPassword ->
                        // هنا تضع كود التحقق من كلمة المرور والتنقل أو أي عملية أخرى
                        println("Password: $password, Confirm: $confirmPassword")
>>>>>>> fdb2e7b (تصميم جميع الواجهات)
                    }
                )
                //NewPasswordScreen()
//                OtpVerificationScreen(
//                    onContinueClicked = { code ->
//                        // التعامل مع الرمز
//                    },
//                    onBackClicked = {
//                        // العودة إلى الشاشة السابقة مثلاً
//                       // navController.popBackStack()
//                    }
//                )

//                val navController = rememberNavController()
//                NavHost(
//                    navController = navController,
//                    startDestination = "auth_graph"
//                ) {
//                    authNavGraph(navController)
//
//                    composable(route = "main_graph") {
//                        MainAppScreen()
//                    }
//                }
            }
        }
    }
}
