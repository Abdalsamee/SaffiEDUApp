package com.example.saffieduapp.presentation.screens.SignUpScreen.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.Cairo

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDropdown(
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val classOptions = listOf(
        "الصف الأول", "الصف الثاني", "الصف الثالث",
        "الصف الرابع", "الصف الخامس", "الصف السادس",
        "الصف السابع", "الصف الثامن", "الصف التاسع",
        "الصف العاشر", "الصف الحادي عشر", "الصف الثاني عشر"
    )

    var expanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val fontSize = (maxWidth.value * 0.04).sp
        val cornerRadius = maxWidth * 0.025f
        val fieldHeight = maxHeight * 0.08f

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedClass,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        text = "اختر الصف الدراسي",
                        color = AppTextSecondary,
                        fontSize = fontSize,
                        textAlign = TextAlign.End,
                        style = TextStyle(fontFamily = Cairo)
                    )
                },
                placeholder = {
                    Text(
                        text = "حدد صفك الدراسي",
                        color = AppTextSecondary,
                        fontSize = fontSize,
                        textAlign = TextAlign.End,
                        style = TextStyle(fontFamily = Cairo)
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                shape = RoundedCornerShape(cornerRadius),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(fieldHeight),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTextSecondary,
                    unfocusedBorderColor = AppTextSecondary,
                    cursorColor = AppTextPrimary,
                    focusedLabelColor = AppTextSecondary,
                    unfocusedLabelColor = AppTextSecondary
                ),
                textStyle = TextStyle(
                    fontSize = fontSize,
                    textAlign = TextAlign.End,
                    fontFamily = Cairo,
                    color = AppTextPrimary
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                classOptions.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item,
                                fontSize = fontSize,
                                textAlign = TextAlign.End,
                                color = AppTextPrimary,
                                style = TextStyle(fontFamily = Cairo)
                            )
                        },
                        onClick = {
                            onClassSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
