package com.example.saffieduapp.presentation.screens.login

<<<<<<< HEAD
import android.annotation.SuppressLint
=======
>>>>>>> integration&UI
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
<<<<<<< HEAD
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
=======
import androidx.compose.runtime.*
>>>>>>> integration&UI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
<<<<<<< HEAD
=======
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
>>>>>>> integration&UI
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
<<<<<<< HEAD
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.imePadding
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.components.PrimaryButton
import com.example.saffieduapp.presentation.screens.login.components.LoginTextField
import com.example.saffieduapp.ui.theme.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsState().value
=======
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
>>>>>>> integration&UI

    // ✅ التحكم في لون شريط الحالة
    val systemUiController = rememberSystemUiController()
    val statusBarColor =AppPrimary  // 🔵 جعل الستاتس بار أزرق

    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = false
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
<<<<<<< HEAD
            .background(AppPrimary)
=======
            .background(primaryBlue)
>>>>>>> integration&UI
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val logoSize = (screenHeight * 0.20f).coerceIn(100.dp, 180.dp)

        Column(
<<<<<<< HEAD
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
=======
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

>>>>>>> integration&UI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
<<<<<<< HEAD
                    .clip(RoundedCornerShape(topEnd = screenWidth * 0.25f))
                    .background(AppBackground)
=======
                    .clip(RoundedCornerShape(topEnd = 120.dp))
                    .background(backgroundWhite)
>>>>>>> integration&UI
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
<<<<<<< HEAD
                        .imePadding()
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
=======
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

>>>>>>> integration&UI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
<<<<<<< HEAD
                                checked = state.rememberMe,
                                onCheckedChange = { viewModel.onEvent(LoginEvent.RememberMeChanged(it)) },
                                colors = CheckboxDefaults.colors(checkedColor = AppPrimary)
                            )
                            Text(
                                text = "تذكرني",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppTextSecondary,
                                textAlign = TextAlign.Start
=======
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it }
                            )
                            Text(
                                text = "تذكرني",
                                color = Color.Gray,
                                fontWeight = FontWeight.W500
>>>>>>> integration&UI
                            )
                        }

                        Text(
                            text = "هل نسيت كلمة المرور؟",
<<<<<<< HEAD
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
=======
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

>>>>>>> integration&UI
                    ClickableText(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
<<<<<<< HEAD
                                    color = AppTextSecondary,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
=======
                                    color = Color.Gray,
                                    fontWeight = FontWeight.W500,
>>>>>>> integration&UI
                                )
                            ) {
                                append("ليس لديك حساب؟ ")
                            }
                            pushStringAnnotation(tag = "signup", annotation = "signup")
                            withStyle(
                                style = SpanStyle(
<<<<<<< HEAD
                                    color = AppTextPrimary,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
=======
                                    color = Color.Black,
                                    fontWeight = FontWeight.W600
>>>>>>> integration&UI
                                )
                            ) {
                                append("اشترك")
                            }
                            pop()
                        },
                        onClick = { offset ->
<<<<<<< HEAD
                            // TODO: الانتقال لصفحة التسجيل
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center
                        )
=======
                            // تنفيذ عند الضغط على "اشترك"
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
>>>>>>> integration&UI
                    )
                }
            }
        }
    }
}