package com.example.saffieduapp.presentation.screens.signup

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.Cairo

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSelector(
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedGrade by remember { mutableStateOf("") }
    val grades = listOf(
        "الصف الأول", "الصف الثاني", "الصف الثالث", "الصف الرابع",
        "الصف الخامس", "الصف السادس", "الصف السابع", "الصف الثامن",
        "الصف التاسع", "الصف العاشر", "الصف الحادي عشر", "الصف الثاني عشر"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        // ✅ تحويل القيم إلى نسب
        val fieldHeight = maxHeight * 0.08f
        val fontSize = (maxWidth.value * 0.04).sp
        val cornerRadius = maxWidth * 0.025f

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedGrade,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        text = "اختر الصف الدراسي",
                        textAlign = TextAlign.End,
                        color = AppTextSecondary,
                        fontSize = fontSize,
                        style = TextStyle(fontFamily = Cairo)
                    )
                },
                placeholder = {
                    Text(
                        text = "حدد صفك الدراسي",
                        textAlign = TextAlign.End,
                        color = AppTextSecondary,
                        fontSize = fontSize,
                        style = TextStyle(fontFamily = Cairo)
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = "Expand",
                        tint = AppTextSecondary
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(fieldHeight),
                shape = RoundedCornerShape(cornerRadius),
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
                ),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                grades.forEach { grade ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = grade,
                                fontSize = fontSize,
                                color = AppTextPrimary,
                                fontFamily = Cairo
                            )
                        },
                        onClick = {
                            selectedGrade = grade
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}
