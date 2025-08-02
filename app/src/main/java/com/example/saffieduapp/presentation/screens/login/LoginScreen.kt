package com.example.saffieduapp.presentation.screens.login

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.imePadding
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.components.PrimaryButton
import com.example.saffieduapp.presentation.screens.login.components.LoginTextField
import com.example.saffieduapp.ui.theme.*

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsState().value

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPrimary)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val logoSize = (screenHeight * 0.20f).coerceIn(100.dp, 180.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            // ✅ الشعار
            Image(
                painter = painterResource(id = R.drawable.logo_new__4___4_),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(logoSize)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            // ✅ صندوق الحقول
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
                        .imePadding() // يمنع تصغير المحتوى عند ظهور الكيبورد
                        .padding(
                            horizontal = screenWidth * 0.06f,
                            vertical = screenHeight * 0.02f
                        )
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ✅ العنوان
                    Text(
                        text = "تسجيل الدخول",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = (screenWidth.value * 0.07).sp
                        ),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = screenHeight * 0.03f)
                    )

                    // ✅ حقل رقم الهوية
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

                    // ✅ حقل كلمة المرور
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

                    // ✅ النصوص السفلية
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
                                onCheckedChange = { viewModel.onEvent(LoginEvent.RememberMeChanged(it)) },
                                colors = CheckboxDefaults.colors(checkedColor = AppAccent)
                            )
                            Text(
                                text = "تذكرني",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppTextSecondary,
                                textAlign = TextAlign.Start
                            )
                        }

                        Text(
                            text = "هل نسيت كلمة المرور؟",
                            color = AppTextPrimary,
                            modifier = Modifier.clickable { },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                    // ✅ زر الدخول
                    PrimaryButton(
                        text = "ابدأ",
                        onClick = { viewModel.onEvent(LoginEvent.LoginClicked) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    // ✅ رابط التسجيل
                    ClickableText(
                        text = buildAnnotatedString {
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
                        },
                        onClick = { offset ->
                            // TODO: الانتقال لصفحة التسجيل
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
