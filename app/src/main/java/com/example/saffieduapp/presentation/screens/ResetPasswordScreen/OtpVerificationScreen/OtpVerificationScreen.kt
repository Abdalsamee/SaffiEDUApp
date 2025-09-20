package com.example.saffieduapp.presentation.screens.ResetPasswordScreen.OtpVerificationScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.OtpVerificationScreen.componente.OtpVerificationTopBar
import com.example.saffieduapp.ui.theme.AppBackground
import com.example.saffieduapp.ui.theme.AppTextPrimary

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OtpVerificationScreen(
    onContinueClicked: (String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val otpValues = remember { List(6) { mutableStateOf("") } }
    val focusRequesters = List(6) { FocusRequester() }
    var timer by remember { mutableStateOf(60) }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer -= 1
        }
    }

    val code = otpValues.joinToString("") { it.value }

    Scaffold(
        topBar = {
            OtpVerificationTopBar(
                title = "رمز التحقق",
                onBackClicked = onBackClicked
            )
        }
    ) { paddingValues ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            val paddingHorizontal = screenWidth * 0.05f
            val imageSize = screenWidth * 0.25f
            val fontSmall = screenWidth.value * 0.030f
            val fontMedium = screenWidth.value * 0.040f
            val otpFieldSize = screenWidth * 0.12f
            val spacingLarge = screenHeight * 0.04f
            val spacingMedium = screenHeight * 0.02f

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = paddingHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(spacingMedium))

                Box(
                    modifier = Modifier
                        .size(imageSize)
                        .background(Color(0xf4A90E2), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.group),
                        contentDescription = "شعار التطبيق",
                        modifier = Modifier.size(imageSize * 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(spacingMedium))

                Text(
                    text = "لقد أرسلنا رمز تحقق مكوّنًا من 6 أرقام إلى بريدك الإلكتروني / رقم هاتفك.\nيرجى إدخال الرمز في الحقل أدناه لإكمال عملية التحقق.",
                    color = Color.Gray,
                    fontSize = fontSmall.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacingLarge))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("أدخل رمز التحقق", color = Color.Black, fontSize = fontMedium.sp)
                    Text("00:$timer", color = Color.Black, fontSize = fontMedium.sp)
                }

                Spacer(modifier = Modifier.height(spacingMedium))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    //horizontalArrangement = Arrangement.Center.s,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    otpValues.forEachIndexed { index, otpState ->
                        OutlinedTextField(
                            value = otpState.value,
                            onValueChange = { newValue ->
                                if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                    otpState.value = newValue
                                    if (newValue.isNotEmpty() && index < otpValues.lastIndex) {
                                        focusRequesters[index + 1].requestFocus()
                                    }
                                    if (newValue.isEmpty() && index > 0) {
                                        focusRequesters[index - 1].requestFocus()
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .width(otpFieldSize)
                                .aspectRatio(0.8f)
                                .focusRequester(focusRequesters[index]),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 15.sp,
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

                Spacer(modifier = Modifier.height(spacingLarge))

                Button(
                    onClick = { onContinueClicked(code) },
                    enabled = code.length == 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.065f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        disabledContainerColor = Color(0xFF4A90E2),
                        contentColor = Color.White,
                        disabledContentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("متابعة", fontSize = fontMedium.sp)
                }
            }
        }
    }
}
