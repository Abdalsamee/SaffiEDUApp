package com.example.saffieduapp.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.*

@Composable
fun NewPasswordScreen() {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // العنوان
        Text(
            text = "كلمة المرور الجديدة",
            style = MaterialTheme.typography.titleLarge.copy(
                color = AppPrimary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))

        // حقل كلمة المرور
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text(
                    text = "كلمة المرور",
                    style = MaterialTheme.typography.bodyMedium.copy(color = AppTextSecondary)
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    val iconRes = if (passwordVisible) R.drawable.visepel else R.drawable.notvisipel
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Toggle Password",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppPrimary,
                unfocusedBorderColor = AppTextSecondary,
                cursorColor = AppPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // حقل تأكيد كلمة المرور
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = {
                Text(
                    text = "تأكيد كلمة المرور",
                    style = MaterialTheme.typography.bodyMedium.copy(color = AppTextSecondary)
                )
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    val iconRes = if (confirmPasswordVisible) R.drawable.visepel else R.drawable.notvisipel
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Toggle Confirm Password",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppPrimary,
                unfocusedBorderColor = AppTextSecondary,
                cursorColor = AppPrimary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // زر المتابعة
        Button(
            onClick = {
                // TODO: تنفيذ العملية بعد الضغط
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppPrimary,
                contentColor = AppBackground
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "متابعة",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = AppBackground
                )
            )
        }
    }
}
