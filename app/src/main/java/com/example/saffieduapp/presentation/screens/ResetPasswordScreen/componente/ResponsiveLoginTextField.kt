package com.example.saffieduapp.presentation.screens.ResetPasswordScreen.componente

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.Cairo

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveLoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    icon: Int,
    screenWidth: Dp,
    screenHeight: Dp,
    modifier: Modifier = Modifier
) {
    val fieldHeight = screenHeight * 0.10f
    val fontSize: TextUnit = (screenWidth.value * 0.04).sp
    val placeholderFontSize: TextUnit = (screenWidth.value * 0.032).sp
    val cornerRadius = screenWidth * 0.025f

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(fieldHeight),
        placeholder = {
            Text(
                text = placeholder,
                textAlign = TextAlign.End,   // هنا المهم: يكون جهة اليسار داخل الحقل
                color = AppTextSecondary,
                fontSize = placeholderFontSize,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                softWrap = false,
                style = TextStyle(
                    fontFamily = Cairo
                )
            )
        },
        label = {
            Text(
                text = label,
                textAlign = TextAlign.End,
                color = AppTextSecondary,
                fontSize = fontSize,
                style = TextStyle(
                    fontFamily = Cairo
                )
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "icon"
            )
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onToggleVisibility?.invoke() }) {
                    Icon(
                        painter = painterResource(
                            id = if (isPasswordVisible)
                                com.example.saffieduapp.R.drawable.visepel
                            else
                                com.example.saffieduapp.R.drawable.notvisipel
                        ),
                        contentDescription = "Toggle Password"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(cornerRadius),
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTextSecondary,
            unfocusedBorderColor = AppTextSecondary,
            cursorColor = AppTextPrimary,
            focusedLabelColor = AppTextSecondary,
            unfocusedLabelColor = AppTextSecondary
        ),
        textStyle = TextStyle(
            textAlign = TextAlign.End,
            color = AppTextPrimary,
            fontSize = fontSize,
            fontFamily = Cairo
        )
    )
}
