package com.example.saffieduapp.presentation.screens.login

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.components.PrimaryButton
import com.example.saffieduapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPrimary)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_new__4___4_),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(156.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topEnd = 120.dp))
                    .background(AppBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // العنوان الرئيسي
                    Text(
                        text = "تسجيل الدخول",
                        style = MaterialTheme.typography.displayLarge,
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // حقل رقم الهوية
                    OutlinedTextField(
                        value = state.id,
                        onValueChange = { viewModel.onEvent(LoginEvent.IdChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        placeholder = {
                            Text(
                                "123XXXXXXXX",
                                textAlign = TextAlign.End,
                                color = AppTextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        label = {
                            Text(
                                "رقم الهوية",
                                textAlign = TextAlign.End,
                                color = AppTextSecondary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.id_user_1),
                                contentDescription = "رقم الهوية"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTextSecondary,
                            unfocusedBorderColor = AppTextSecondary,
                            cursorColor = AppTextPrimary
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.End,
                            color = AppTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // حقل كلمة المرور
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        placeholder = {
                            Text(
                                "********",
                                textAlign = TextAlign.End,
                                color = AppTextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        label = {
                            Text(
                                "كلمة المرور",
                                textAlign = TextAlign.End,
                                color = AppTextSecondary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.onEvent(LoginEvent.TogglePasswordVisibility) }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (state.isPasswordVisible)
                                            R.drawable.visepel
                                        else
                                            R.drawable.notvisipel
                                    ),
                                    contentDescription = if (state.isPasswordVisible)
                                        "إخفاء كلمة المرور"
                                    else
                                        "عرض كلمة المرور"
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (state.isPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTextSecondary,
                            unfocusedBorderColor = AppTextSecondary,
                            cursorColor = AppTextPrimary
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.End,
                            color = AppTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // تذكرني و هل نسيت كلمة المرور
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
                            modifier = Modifier.clickable { /* نفذ الانتقال هنا */ },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = "ابدأ",
                        onClick = { viewModel.onEvent(LoginEvent.LoginClicked) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ClickableText(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = AppTextSecondary,
                                    fontWeight = FontWeight.W500,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                )
                            ) {
                                append("ليس لديك حساب؟ ")
                            }
                            pushStringAnnotation(tag = "signup", annotation = "signup")
                            withStyle(
                                style = SpanStyle(
                                    color = AppTextPrimary,
                                    fontWeight = FontWeight.W600,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                )
                            ) {
                                append("اشترك")
                            }
                            pop()
                        },
                        onClick = { offset ->
                            // تنفيذ الانتقال لصفحة الاشتراك
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
