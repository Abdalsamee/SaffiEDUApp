package com.example.saffieduapp.presentation.screens.signup

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.login.components.LoginTextField
import com.example.saffieduapp.ui.theme.*

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SignUpScreen() {
    var fullName by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var schoolLevel by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPrimary)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth

        // ✅ الهيدر
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.28f)
                .background(AppPrimary)
        ) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = screenHeight * 0.05f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Text(
                text = "اشترك",
                fontSize = (screenWidth.value * 0.07).sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = screenHeight * 0.05f)
            )
        }

        // ✅ محتوى الشاشة
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = screenHeight * 0.22f)
                .clip(RoundedCornerShape(topStart = screenWidth * 0.08f))
                .background(Color.White)
                .padding(horizontal = screenWidth * 0.06f, vertical = screenHeight * 0.02f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "إنشاء الحساب",
                fontSize = (screenWidth.value * 0.05).sp,
                fontWeight = FontWeight.Bold,
                color = AppTextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.1f),
                textAlign = TextAlign.Center
            )

            // ✅ حقول الإدخال
            LoginTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "الاسم الكامل",
                placeholder = "الاسم الكامل",
                icon = R.drawable.fullname,
                modifier = Modifier.weight(0.11f)
            )

            LoginTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = "رقم الهوية",
                placeholder = "123000XXXX",
                icon = R.drawable.id_user_1,
                modifier = Modifier.weight(0.11f)
            )

            LoginTextField(
                value = email,
                onValueChange = { email = it },
                label = "البريد الإلكتروني",
                placeholder = "example@gmail.com",
                icon = R.drawable.email,
                modifier = Modifier.weight(0.11f)
            )

            LoginTextField(
                value = password,
                onValueChange = { password = it },
                label = "كلمة السر",
                placeholder = "********",
                isPassword = true,
                isPasswordVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                icon = R.drawable.notvisipel,
                modifier = Modifier.weight(0.11f)
            )

            LoginTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "تأكيد كلمة السر",
                placeholder = "********",
                isPassword = true,
                isPasswordVisible = confirmPasswordVisible,
                onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                icon = R.drawable.confirmpassword,
                modifier = Modifier.weight(0.11f)
            )

            LoginTextField(
                value = schoolLevel,
                onValueChange = { schoolLevel = it },
                label = "اختر الصف الدراسي",
                placeholder = "حدد صفك الدراسي",
                icon = R.drawable.arrow_left,
                modifier = Modifier.weight(0.11f)
            )

            Spacer(Modifier.weight(0.05f))

            // ✅ زر الاشتراك
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.1f),
                shape = RoundedCornerShape(screenWidth * 0.025f),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
            ) {
                Text(
                    text = "اشترك",
                    fontSize = (screenWidth.value * 0.045).sp,
                    color = Color.White
                )
            }

            Spacer(Modifier.weight(0.05f))

            // ✅ الشروط ورابط تسجيل الدخول
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.08f)
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = {},
                    enabled = false
                )
                Text(
                    text = "لقد وافقت على الشروط و الأحكام",
                    color = AppTextSecondary,
                    fontSize = (screenWidth.value * 0.03).sp
                )
                Spacer(modifier = Modifier.weight(1f))
                ClickableText(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = AppTextSecondary)) {
                            append("هل لديك حساب؟ ")
                        }
                        withStyle(style = SpanStyle(color = AppPrimary)) {
                            append("تسجيل الدخول")
                        }
                    },
                    onClick = { /* TODO: الانتقال لشاشة تسجيل الدخول */ },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}
