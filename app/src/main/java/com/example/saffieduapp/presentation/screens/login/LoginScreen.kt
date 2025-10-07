@file:Suppress("DEPRECATION")

package com.example.saffieduapp.presentation.screens.login

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.components.PrimaryButton
import com.example.saffieduapp.presentation.screens.login.components.LoginTextField
import com.example.saffieduapp.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoginScreen(
    onStudentLogin: () -> Unit,
    onTeacherLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    // 🔹 متابعة الأحداث
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LoginViewModel.UiEvent.LoginSuccess -> {
                    when (event.role) {
                        "student" -> onStudentLogin()
                        "teacher" -> onTeacherLogin() // <- هنا
                        else -> {
                            Toast.makeText(
                                context,
                                "تعذر تحديد دور المستخدم، يرجى التواصل مع الإدارة",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is LoginViewModel.UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPrimary)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val logoSize = (screenHeight * 0.20f).coerceIn(100.dp, 180.dp)

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            Image(
                painter = painterResource(id = R.drawable.logo_new__4___4_),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(logoSize)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topEnd = screenWidth * 0.25f))
                    .background(AppBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .padding(
                            horizontal = screenWidth * 0.06f,
                            vertical = screenHeight * 0.02f
                        )
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل الدخول",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = (screenWidth.value * 0.07).sp
                        ),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = screenHeight * 0.03f)
                    )

                    LoginTextField(
                        value = state.id,
                        onValueChange = { viewModel.onEvent(LoginEvent.IdChanged(it)) },
                        label = "رقم الهوية",
                        placeholder = "123XXXXXXXX",
                        icon = R.drawable.id_user_1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.025f))

                    LoginTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                        label = "كلمة المرور",
                        placeholder = "********",
                        isPassword = true,
                        isPasswordVisible = state.isPasswordVisible,
                        onToggleVisibility = { viewModel.onEvent(LoginEvent.TogglePasswordVisibility) },
                        icon = R.drawable.notvisipel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = state.rememberMe,
                                onCheckedChange = {
                                    viewModel.onEvent(LoginEvent.RememberMeChanged(it))
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AppPrimary,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(
                                text = "تذكرني",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppTextSecondary,
                                textAlign = TextAlign.Start
                            )
                        }
                        // ✅ هنا نضيف الحدث للنص
                        TextButton(onClick = { onNavigateToForgotPassword() }) {
                            Text(
                                text = "هل نسيت كلمة المرور؟",
                                color = AppTextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                    PrimaryButton(
                        text = if (state.isLoading) "جاري الدخول..." else "ابدأ",
                        onClick = { viewModel.onEvent(LoginEvent.LoginClicked) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    state.errorMessage?.let {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    val annotatedText = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = AppTextSecondary,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            )
                        ) {
                            append("ليس لديك حساب؟ ")
                        }
                        pushStringAnnotation(tag = "signup", annotation = "signup")
                        withStyle(
                            style = SpanStyle(
                                color = AppTextPrimary,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            )
                        ) {
                            append("اشترك")
                        }
                        pop()
                    }

                    ClickableText(
                        text = annotatedText,
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(
                                tag = "signup",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { onNavigateToSignUp() }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}
