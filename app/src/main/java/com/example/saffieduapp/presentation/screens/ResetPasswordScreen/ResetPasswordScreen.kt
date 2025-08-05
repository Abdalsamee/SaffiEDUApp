package com.example.saffieduapp.presentation.screens.resetpassword

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.ViewModel.ResetPasswordViewModel
import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.componente.ResponsiveLoginTextField
//import com.example.saffieduapp.presentation.screens.resetpassword.viewmodel.ResetPasswordViewModel
//import com.example.saffieduapp.presentation.screens.resetpassword.components.ResponsiveLoginTextField
import com.example.saffieduapp.ui.theme.*

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordRecoveryScreen(
    onBackClicked: () -> Unit,
    viewModel: ResetPasswordViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "استعادة كلمة المرور",
                                style = Typography.titleLarge.copy(
                                    fontFamily = Cairo,
                                    fontSize = (screenWidth.value * 0.05).sp
                                ),
                                color = AppTextPrimary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_left),
                                contentDescription = "رجوع",
                                tint = AppTextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
                )
            },
            containerColor = AppBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = screenWidth * 0.05f),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                Text(
                    text = "إعادة تعيين كلمة المرور",
                    style = Typography.titleLarge.copy(
                        fontFamily = Cairo,
                        fontSize = (screenWidth.value * 0.05).sp
                    ),
                    color = AppTextPrimary
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.01f))

                Text(
                    text = "يرجى إدخال بريدك الإلكتروني المرتبط بحسابك، وسنرسل لك رابطًا لإعادة تعيين كلمة المرور\nexample****an@gmail.com",
                    style = Typography.bodyMedium.copy(
                        fontFamily = Cairo,
                        fontSize = (screenWidth.value * 0.035).sp
                    ),
                    color = AppTextSecondary
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                ResponsiveLoginTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = "البريد الإلكتروني",
                    placeholder = "example@gmail.com",
                    icon = R.drawable.email,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.15f))

                Button(
                    onClick = { viewModel.sendResetLink() },
                    enabled = state.email.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.07f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "متابعة",
                        style = Typography.labelLarge.copy(
                            fontFamily = Cairo,
                            fontSize = (screenWidth.value * 0.035).sp
                        ),
                        color = AppBackground
                    )
                }

                if (state.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                state.errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontFamily = Cairo)
                }

                if (state.isSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✅ تم إرسال رابط إعادة تعيين كلمة المرور بنجاح", color = AppPrimary, fontFamily = Cairo)
                }
            }
        }
    }
}
