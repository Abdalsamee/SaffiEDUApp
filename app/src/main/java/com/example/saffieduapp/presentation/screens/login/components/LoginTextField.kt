package com.example.saffieduapp.presentation.screens.login.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary

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
    icon: Int
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        placeholder = { Text(placeholder, textAlign = TextAlign.End, color = AppTextSecondary) },
        label = { Text(label, textAlign = TextAlign.End, color = AppTextSecondary) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onToggleVisibility?.invoke() }) {
                    Icon(
                        painter = painterResource(id = if (isPasswordVisible) R.drawable.visepel else R.drawable.notvisipel),
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
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTextSecondary,
            unfocusedBorderColor = AppTextSecondary,
            cursorColor = AppTextPrimary
        ),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = AppTextPrimary)
    )
}
