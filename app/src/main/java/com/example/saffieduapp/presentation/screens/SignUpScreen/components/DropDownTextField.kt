package com.example.saffieduapp.presentation.screens.signup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.Cairo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSelector(
    selectedGrade: String,
    onGradeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val grades = listOf(
        "الصف الأول", "الصف الثاني", "الصف الثالث", "الصف الرابع",
        "الصف الخامس", "الصف السادس", "الصف السابع", "الصف الثامن",
        "الصف التاسع", "الصف العاشر", "الصف الحادي عشر", "الصف الثاني عشر"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = selectedGrade,
            onValueChange = {},
            label = {
                Text(
                    text = "اختر الصف الدراسي",
                    style = TextStyle(fontFamily = Cairo, color = AppTextSecondary)
                )
            },
            placeholder = {
                Text(
                    text = "حدد صفك الدراسي",
                    style = TextStyle(fontFamily = Cairo, color = AppTextSecondary)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTextSecondary,
                unfocusedBorderColor = AppTextSecondary,
                cursorColor = AppTextPrimary,
                focusedLabelColor = AppTextSecondary,
                unfocusedLabelColor = AppTextSecondary,
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End,
                color = AppTextPrimary,
                fontFamily = Cairo
            ),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 240.dp)
                .verticalScroll(rememberScrollState())
        ) {
            grades.forEach { grade ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = grade,
                            style = TextStyle(
                                fontFamily = Cairo,
                                color = AppTextPrimary,
                                textAlign = TextAlign.End
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onClick = {
                        onGradeSelected(grade)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
