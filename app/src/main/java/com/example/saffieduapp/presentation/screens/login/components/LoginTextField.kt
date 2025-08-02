package com.example.saffieduapp.presentation.screens.login.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    icon: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        // ✅ تحويل جميع القيم إلى نسب
        val fieldHeight = maxHeight * 0.08f      // ارتفاع الحقل نسبة من ارتفاع الشاشة
        val fontSize = (maxWidth.value * 0.04).sp // حجم الخط نسبة من عرض الشاشة
        val cornerRadius = maxWidth * 0.025f     // نصف القطر للحواف بشكل نسبي

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight),
            placeholder = {
                Text(
                    placeholder,
                    textAlign = TextAlign.End,
                    color = AppTextSecondary,
                    fontSize = fontSize
                )
            },
            label = {
                Text(
                    label,
                    textAlign = TextAlign.End,
                    color = AppTextSecondary,
                    fontSize = fontSize
                )
            },
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { onToggleVisibility?.invoke() }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible)
                                    R.drawable.visepel
                                else
                                    R.drawable.notvisipel
                            ),
                            contentDescription = "Toggle Password"
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "icon"
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(cornerRadius),
            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTextSecondary,
                unfocusedBorderColor = AppTextSecondary,
                cursorColor = AppTextPrimary
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End,
                color = AppTextPrimary,
                fontSize = fontSize
            )
        )
    }
}
