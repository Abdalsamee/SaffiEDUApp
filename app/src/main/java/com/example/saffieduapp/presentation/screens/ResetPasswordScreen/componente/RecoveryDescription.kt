package com.example.saffieduapp.presentation.screens.ResetPasswordScreen.componente


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.Typography

@Composable
fun RecoveryDescription(
    fontTitle: TextUnit,
    fontBody: TextUnit,
    topSpacer: Dp,
    smallSpacer: Dp
) {
    Spacer(modifier = Modifier.height(topSpacer))

    Text(
        text = "إعادة تعيين كلمة المرور",
        style = Typography.titleLarge.copy(fontSize = fontTitle),
        color = AppTextPrimary
    )

    Spacer(modifier = Modifier.height(smallSpacer))

    Text(
        text = "يرجى إدخال بريدك الإلكتروني المرتبط بحسابك، وسنرسل لك رابطًا لإعادة تعيين كلمة المرور\nexample****an@gmail.com",
        style = Typography.bodyMedium.copy(fontSize = fontBody),
        color = AppTextSecondary
    )
}
