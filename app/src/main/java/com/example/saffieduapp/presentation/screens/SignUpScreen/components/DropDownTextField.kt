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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedGrade by remember { mutableStateOf("") }
    val grades = listOf(
        "الصف الأول", "الصف الثاني", "الصف الثالث", "الصف الرابع",
        "الصف الخامس", "الصف السادس", "الصف السابع", "الصف الثامن",
        "الصف التاسع", "الصف العاشر", "الصف الحادي عشر", "الصف الثاني عشر"
    )

    // الخطوة 1: استخدام ExposedDropdownMenuBox لإدارة القائمة المنسدلة
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        // حقل النص الذي سيظهر دائماً
        OutlinedTextField(
            // الخطوة 2: ربط حقل النص بالقائمة باستخدام menuAnchor
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // هذا السطر مهم جداً لربط القائمة بالحقل
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

        // الخطوة 3: تعريف القائمة المنسدلة التي ستظهر عند الضغط
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                // الخطوة 4: تحديد أقصى ارتفاع للقائمة لجعلها قابلة للتمرير
                .heightIn(max = 240.dp) // ارتفاع تقريبي لـ 5 عناصر
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
                        selectedGrade = grade
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}