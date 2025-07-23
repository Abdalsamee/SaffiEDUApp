package com.example.saffieduapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    background = AppBackground,
    onPrimary = AppTextPrimary,
    onBackground = AppTextPrimary,
    secondary = AppAccent,
)

@Composable
fun SaffiEDUAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
