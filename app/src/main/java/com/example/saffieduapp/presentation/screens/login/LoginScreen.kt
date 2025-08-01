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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    // 1. أضفنا هذا الباراميتر لتلقي أمر الانتقال
    onLoginSuccess: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val primaryBlue = Color(0xFF3F86F1)
    val backgroundWhite = Color.White
    val borderColor = Color(0xFFD1D1D1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBlue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_saffi),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(156.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topEnd = 120.dp))
                    .background(backgroundWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل الدخول",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        placeholder = { Text("123XXXXXXXX", textAlign = TextAlign.End) },
                        label = { Text("رقم الهوية", textAlign = TextAlign.End) },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_left),
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            cursorColor = Color.Black
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        placeholder = { Text("", textAlign = TextAlign.End) },
                        label = { Text("كلمة المرور", textAlign = TextAlign.End) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isPasswordVisible)
                                            R.drawable.logo_saffi
                                        else
                                            R.drawable.arrow_left
                                    ),
                                    contentDescription = if (isPasswordVisible)
                                        "إخفاء كلمة المرور"
                                    else
                                        "عرض كلمة المرور"
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (isPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            cursorColor = Color.Black
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it }
                            )
                            Text(
                                text = "تذكرني",
                                color = Color.Gray,
                                fontWeight = FontWeight.W500
                            )
                        }

                        Text(
                            text = "هل نسيت كلمة المرور؟",
                            color = Color.Black,
                            modifier = Modifier.clickable { },
                            fontWeight = FontWeight.W500
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        // 2. عند الضغط على الزر، قم بتنفيذ أمر الانتقال
                        onClick = { onLoginSuccess() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                    ) {
                        Text(
                            text = "ابدأ",
                            fontWeight = FontWeight.W500
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ClickableText(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.W500,
                                )
                            ) {
                                append("ليس لديك حساب؟ ")
                            }
                            pushStringAnnotation(tag = "signup", annotation = "signup")
                            withStyle(
                                style = SpanStyle(
                                    color = Color.Black,
                                    fontWeight = FontWeight.W600
                                )
                            ) {
                                append("اشترك")
                            }
                            pop()
                        },
                        onClick = { offset ->
                            // تنفيذ عند الضغط على "اشترك"
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}