@file:Suppress("DEPRECATION")

package com.example.saffieduapp.presentation.screens.SignUpScreen

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.components.PrimaryButton
import com.example.saffieduapp.presentation.screens.SignUpScreen.Events.SignUpEvent
import com.example.saffieduapp.presentation.screens.SignUpScreen.ViewModel.SignUpViewModel
import com.example.saffieduapp.presentation.screens.SignUpScreen.components.SineUpAppBar
import com.example.saffieduapp.presentation.screens.SignUpScreen.components.SineUpTextField
import com.example.saffieduapp.presentation.screens.signup.GradeSelector
import com.example.saffieduapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()

    // الاستماع لأحداث التنقل القادمة من الـ ViewModel
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SignUpViewModel.UiEvent.SignUpSuccessWithVerification -> {
                    auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                context,
                                "تم إرسال رابط التفعيل إلى بريدك الإلكتروني. يرجى التحقق قبل تسجيل الدخول.",
                                Toast.LENGTH_LONG
                            ).show()
                            auth.signOut()
                            onNavigateToLogin()
                        } else {
                            Toast.makeText(
                                context,
                                "حدث خطأ أثناء إرسال رابط التفعيل.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is SignUpViewModel.UiEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPrimary)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val fieldSpacing = screenHeight * 0.012f

        Column(modifier = Modifier.fillMaxSize()) {
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "إنشاء الحساب",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = (screenWidth.value * 0.07).sp
                        ),
                        color = AppTextPrimary,
                        modifier = Modifier.padding(bottom = screenHeight * 0.02f)
                    )

                    SineUpTextField(
                        value = state.fullName,
                        onValueChange = { viewModel.onEvent(SignUpEvent.FullNameChanged(it)) },
                        label = "الأسم الكامل",
                        placeholder = "الأسم الكامل",
                        icon = R.drawable.fullname,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    SineUpTextField(
                        value = state.idNumber,
                        onValueChange = { viewModel.onEvent(SignUpEvent.IdNumberChanged(it)) },
                        label = "رقم الهوية",
                        placeholder = "123XXXXXXX",
                        icon = R.drawable.id_user_1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    SineUpTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEvent(SignUpEvent.EmailChanged(it)) },
                        label = "البريد الالكتروني",
                        placeholder = "example@gmail.com",
                        icon = R.drawable.email,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    SineUpTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(SignUpEvent.PasswordChanged(it)) },
                        label = "كلمة المرور",
                        placeholder = "**************",
                        isPassword = true,
                        isPasswordVisible = state.isPasswordVisible,
                        onToggleVisibility = { viewModel.onEvent(SignUpEvent.TogglePasswordVisibility) },
                        icon = R.drawable.notvisipel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    SineUpTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                        label = "تأكيد كلمة المرور",
                        placeholder = "**************",
                        isPassword = true,
                        isPasswordVisible = state.isConfirmPasswordVisible,
                        onToggleVisibility = { viewModel.onEvent(SignUpEvent.ToggleConfirmPasswordVisibility) },
                        icon = if (state.isConfirmPasswordVisible) R.drawable.visepel else R.drawable.notvisipel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenHeight * 0.065f)
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    GradeSelector(
                        modifier = Modifier.fillMaxWidth(),
                        selectedGrade = state.selectedGrade,
                        onGradeSelected = { grade ->
                            viewModel.onEvent(SignUpEvent.GradeChanged(grade))
                        }
                    )

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    PrimaryButton(
                        text = "اشتراك",
                        onClick = { viewModel.onEvent(SignUpEvent.SignUpClicked) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.signUpError != null) {
                        Text(
                            text = state.signUpError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Checkbox(
                            checked = state.agreedToTerms,
                            onCheckedChange = { viewModel.onEvent(SignUpEvent.TermsAgreementChanged(it)) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppPrimary,
                                checkmarkColor = AppTextPrimary
                            )
                        )

                        val annotatedText = buildAnnotatedString {
                            append("أقر وأوافـق على ")
                            pushStringAnnotation(tag = "terms", annotation = "terms")
                            withStyle(SpanStyle(color = AppTextPrimary, fontFamily = Cairo, fontWeight = FontWeight.Medium)) {
                                append("الشروط & الأحكام")
                            }
                            pop()
                            append("    ")
                            pushStringAnnotation(tag = "login", annotation = "login")
                            withStyle(SpanStyle(color = AppTextPrimary, fontFamily = Cairo, fontWeight = FontWeight.Medium)) {
                                append("هل لديك حساب؟")
                            }
                            pop()
                        }

                        ClickableText(
                            text = annotatedText,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations("terms", offset, offset)
                                    .firstOrNull()?.let { /* افتح الشروط */ }
                                annotatedText.getStringAnnotations("login", offset, offset)
                                    .firstOrNull()?.let { onNavigateToLogin() }
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = AppTextSecondary,
                                fontFamily = Cairo,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
