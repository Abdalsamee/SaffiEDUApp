package com.example.saffieduapp.presentation.screens.ResetPasswordScreen.NewPasswordScreen.comomnente


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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.Cairo
import com.example.saffieduapp.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPassWoredTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    icon: Int? = null, // 👈 يمكن تمرير null لعدم إظهار أيقونة
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val fieldHeight = maxHeight * 0.10f
        val fontSize = (maxWidth.value * 0.045).sp
        val cornerRadius = maxWidth * 0.03f

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight),
            placeholder = {
                Text(
                    text = placeholder,
                    textAlign = TextAlign.End,
                    color = AppTextSecondary,
                    fontSize = fontSize,
                    style = TextStyle(fontFamily = Cairo)
                )
            },
            label = {
                Text(
                    text = label,
                    textAlign = TextAlign.End,
                    color = AppTextSecondary,
                    fontSize = fontSize,
                    style = TextStyle(fontFamily = Cairo)
                )
            },
            trailingIcon = {
                when {
                    isPassword -> {
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
                    }
                    icon != null -> {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = "icon"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(cornerRadius),
            visualTransformation = if (isPassword && !isPasswordVisible)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,
            keyboardOptions = if (isPassword)
                KeyboardOptions(keyboardType = KeyboardType.Password)
            else
                KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTextSecondary,
                unfocusedBorderColor = AppTextSecondary,
                cursorColor = AppTextPrimary,
                focusedLabelColor = AppTextSecondary,
                unfocusedLabelColor = AppTextSecondary
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End,
                color = AppTextPrimary,
                fontSize = fontSize,
                fontFamily = Cairo
            )
        )
    }
}
