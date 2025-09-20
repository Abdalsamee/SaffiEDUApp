package com.example.saffieduapp.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.OtpVerificationScreen.componente.OtpVerificationTopBar
import com.example.saffieduapp.ui.theme.AppBackground
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.NewPasswordScreen.comomnente.NewPassWoredTextField
import androidx.compose.foundation.layout.BoxWithConstraints

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPasswordScreen(
    onBackClicked: () -> Unit,
    onContinueClicked: (String, String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val paddingHorizontal = screenWidth * 0.06f
        val paddingVertical = screenHeight * 0.03f
        val textFieldHeight = screenHeight * 0.11f
        val buttonHeight = screenHeight * 0.07f
        val spacerSmall = screenHeight * 0.02f
        val spacerLarge = screenHeight * 0.04f
        val fontSize = (screenWidth.value * 0.045).sp

        Scaffold(
            topBar = {
                OtpVerificationTopBar(
                    title = "كلمة المرور الجديدة",
                    onBackClicked = onBackClicked
                )
            },
            containerColor = AppBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = paddingHorizontal, vertical = paddingVertical)
                    .verticalScroll(rememberScrollState()), // ✅ إضافة التمرير
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(spacerSmall))

                NewPassWoredTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "كلمة المرور",
                    placeholder = "أدخل كلمة المرور",
                    isPassword = true,
                    isPasswordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible },
                    icon = R.drawable.visepel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = textFieldHeight)
                )

                Spacer(modifier = Modifier.height(spacerSmall))

                NewPassWoredTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "تأكيد كلمة المرور",
                    placeholder = "أعد كتابة كلمة المرور",
                    isPassword = true,
                    isPasswordVisible = confirmPasswordVisible,
                    onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                    icon = R.drawable.visepel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = textFieldHeight)
                )

                Spacer(modifier = Modifier.height(spacerLarge))

                Button(
                    onClick = { onContinueClicked(password, confirmPassword) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppPrimary,
                        contentColor = AppBackground
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = "متابعة", fontSize = fontSize)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNewPasswordScreen() {
    NewPasswordScreen(onBackClicked = {}, onContinueClicked = { _, _ -> })
}
