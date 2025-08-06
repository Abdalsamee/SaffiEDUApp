package com.example.saffieduapp.presentation.screens.ResetPasswordScreen.OtpVerificationScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    onContinueClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    // ✅ تخزين القيم لكل خانة (6 خانات)
    val otpValues = remember { List(6) { mutableStateOf("") } }

    // ✅ FocusRequesters لكل خانة
    val focusRequesters = List(6) { FocusRequester() }

    var timer by remember { mutableStateOf(60) }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer -= 1
        }
    }

    val code = otpValues.joinToString("") { it.value }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // صندوق الصورة
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFF90A4AE), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("LOGO", color = Color.White, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "لقد أرسلنا رمز تحقق مكوّنًا من 6 أرقام إلى بريدك الإلكتروني / رقم هاتفك.\nيرجى إدخال الرمز في الحقل أدناه لإكمال عملية التحقق.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("رمز التحقق", color = Color.Black, fontSize = 16.sp)
            Text("00:$timer", color = Color(0xFF4A90E2), fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ حقول إدخال الأرقام
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            otpValues.forEachIndexed { index, otpState ->
                OutlinedTextField(
                    value = otpState.value,
                    onValueChange = { newValue ->
                        if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                            otpState.value = newValue

                            // الانتقال للخانة التالية تلقائياً
                            if (newValue.isNotEmpty() && index < otpValues.lastIndex) {
                                focusRequesters[index + 1].requestFocus()
                            }

                            // الرجوع للخانة السابقة عند المسح
                            if (newValue.isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .focusRequester(focusRequesters[index]),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF4A90E2),
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = Color(0xFF4A90E2)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { onContinueClicked(code) },
            enabled = code.length == 6,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("متابعة", color = Color.White, fontSize = 16.sp)
        }
    }
}
