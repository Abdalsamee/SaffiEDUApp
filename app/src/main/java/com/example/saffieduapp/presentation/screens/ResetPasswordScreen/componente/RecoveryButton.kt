package com.example.saffieduapp.presentation.screens.ResetPasswordScreen.componente

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.saffieduapp.ui.theme.AppBackground
import com.example.saffieduapp.ui.theme.Typography

@Composable
fun RecoveryButton(
    email: String,
    onContinueClicked: (String) -> Unit,
    buttonSpacer: Dp,
    buttonPadding: Dp,
    buttonHeight: Dp,
    fontButton: TextUnit
) {
    Spacer(modifier = Modifier.height(buttonSpacer))

    Button(
        onClick = { onContinueClicked(email) },
        enabled = email.isNotBlank(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = buttonPadding)
            .height(buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4A90E2),  // اللون الأزرق المخصص
            contentColor = Color.White           // لون نص الزر أبيض
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = "متابعة",
            style = Typography.labelLarge.copy(fontSize = fontButton),
            color = Color.White  // نص أبيض
        )
    }
}
