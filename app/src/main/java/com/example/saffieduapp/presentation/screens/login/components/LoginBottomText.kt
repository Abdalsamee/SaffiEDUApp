package com.example.saffieduapp.presentation.screens.login.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppAccent
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary

@Composable
fun LoginBottomText(
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    onForgotPassword: () -> Unit
) {
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
                onCheckedChange = onRememberMeChange,
                colors = CheckboxDefaults.colors(checkedColor = AppAccent)
            )
            Text(
                text = "تذكرني",
                fontSize = 15.sp,
                color = AppTextSecondary,
                fontWeight = FontWeight.W400,
                textAlign = TextAlign.Start
            )
        }

        Text(
            text = "هل نسيت كلمة المرور؟",
            color = AppTextPrimary,
            modifier = Modifier.clickable { onForgotPassword() },
            fontWeight = FontWeight.W400,
            fontSize = 15.sp,
            textAlign = TextAlign.End
        )
    }
}
