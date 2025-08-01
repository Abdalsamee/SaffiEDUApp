package com.example.saffieduapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignUpScreen() {
    val fullName = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val grade = remember { mutableStateOf("") }
    val agreedToTerms = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header with background and title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(color = Color(0xFF6200EE)) // purple_500
                .clip(
                    shape = RoundedCornerShape(bottomStart = 50.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "اشترك",
                    color = Color.White,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "رجوع",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "إنشاء الحساب",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CustomOutlinedTextField(
                value = fullName.value,
                onValueChange = { fullName.value = it },
                label = "الاسم الكامل",
                placeholder = "الاسم الكامل",
                icon = Icons.Default.Person
            )

            CustomOutlinedTextField(
                value = phoneNumber.value,
                onValueChange = { phoneNumber.value = it },
                label = "رقم الهوية",
                placeholder = "123000XXX",
                icon = Icons.Default.DateRange,
                keyboardType = KeyboardType.Number
            )

            CustomOutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = "البريد الإلكتروني",
                placeholder = "example@gmail.com",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            CustomOutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = "كلمة المرور",
                placeholder = "********",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            CustomOutlinedTextField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                label = "تأكيد كلمة المرور",
                placeholder = "********",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            CustomOutlinedTextField(
                value = grade.value,
                onValueChange = { grade.value = it },
                label = "اختر الصف الدراسي",
                placeholder = "حدد صفك الدراسي",
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* تنفيذ عملية التسجيل */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)) // purple_500
            ) {
                Text(text = "اشترك", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreedToTerms.value,
                    onCheckedChange = { agreedToTerms.value = it }
                )
                Text(
                    text = "أقر وأوافق على الشروط والأحكام",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "هل لديك حساب؟",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
