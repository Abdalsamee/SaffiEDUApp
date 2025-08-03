package com.example.saffieduapp.presentation.screens.SignUpScreen

import android.annotation.SuppressLint
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.components.PrimaryButton
import com.example.saffieduapp.presentation.screens.SignUpScreen.components.SineUpAppBar
import com.example.saffieduapp.presentation.screens.SignUpScreen.components.SineUpTextField
import com.example.saffieduapp.ui.theme.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val systemUiController = rememberSystemUiController()
    val statusBarColor = AppPrimary

    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = false
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPrimary)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val fieldSpacing = screenHeight * 0.012f

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SineUpAppBar(
                onBackClick = onBackClick,
                screenWidth = screenWidth
            )

            Spacer(modifier = Modifier.height(screenHeight * 0.015f))

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
                        text = "إنشاء الحساب",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = (screenWidth.value * 0.07).sp
                        ),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = screenHeight * 0.02f)
                    )

                    // الاسم الكامل
                    SineUpTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "الأسم الكامل",
                        placeholder = "الأسم الكامل",
                        icon = R.drawable.fullname,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    // رقم الهوية
                    SineUpTextField(
                        value = idNumber,
                        onValueChange = { idNumber = it },
                        label = "رقم الهوية",
                        placeholder = "123XXXXXXX",
                        icon = R.drawable.id_user_1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    // البريد الإلكتروني
                    SineUpTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "البريد الالكتروني",
                        placeholder = "example@gmail.com",
                        icon = R.drawable.email,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    // كلمة المرور
                    SineUpTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "كلمة المرور",
                        placeholder = "**************",
                        isPassword = true,
                        isPasswordVisible = isPasswordVisible,
                        onToggleVisibility = { isPasswordVisible = !isPasswordVisible },
                        icon = R.drawable.notvisipel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    // تأكيد كلمة المرور
                    SineUpTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "تأكيد كلمة المرور",
                        placeholder = "* * * * * * * *",
                        isPassword = true,
                        isPasswordVisible = isConfirmPasswordVisible,
                        onToggleVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                        icon = R.drawable.confirmpassword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    // زر الاشتراك
                    PrimaryButton(
                        text = "اشتراك",
                        onClick = {
                            // TODO: تنفيذ عملية الاشتراك
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    // ✅ النصوص السفلية: Checkbox + النصوص
                    var agreedToTerms by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppPrimary
                            )
                        )

                        val annotatedText = buildAnnotatedString {
                            append("أقر وأوافـق على ")

                            pushStringAnnotation(tag = "terms", annotation = "terms")
                            withStyle(SpanStyle(color = AppPrimary)) {
                                append("الشروط & الأحكام")
                            }
                            pop()

                            append("    ")

                            pushStringAnnotation(tag = "login", annotation = "login")
                            withStyle(SpanStyle(color = AppTextPrimary)) {
                                append("هل لديك حساب؟")
                            }
                            pop()
                        }

                        ClickableText(
                            text = annotatedText,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations("terms", offset, offset)
                                    .firstOrNull()?.let {
                                        // TODO: فتح صفحة الشروط
                                    }

                                annotatedText.getStringAnnotations("login", offset, offset)
                                    .firstOrNull()?.let {
                                        // TODO: الانتقال إلى شاشة تسجيل الدخول
                                    }
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = AppTextSecondary
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
