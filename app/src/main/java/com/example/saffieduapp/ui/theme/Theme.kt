package com.example.saffieduapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    background = AppBackground,
    onPrimary = AppTextPrimary,
    onBackground = AppTextPrimary,
    secondary = AppAccent,

    // --- الإضافات المقترحة ---
    surface = AppBackground, // مهم جداً للمكونات التي تظهر كطبقة علوية مثل القوائم
    onSurface = AppTextPrimary
)

@Composable
fun SaffiEDUAppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {

        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = LightColorScheme.primary.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}