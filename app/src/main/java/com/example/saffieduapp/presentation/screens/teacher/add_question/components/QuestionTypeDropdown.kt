package com.example.saffieduapp.presentation.screens.teacher.add_question.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionType
import com.example.saffieduapp.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTypeDropdown(
    selectedType: QuestionType,
    onTypeSelected: (QuestionType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val questionTypes = remember { QuestionType.values() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            readOnly = true,
            value = selectedType.displayName,
            onValueChange = {},
            placeholder = { Text("اختر نوع السؤال", color = Color.Gray, fontSize = 13.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )


            )


        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            questionTypes.forEach { type ->
                // --- بداية التعديل ---
                DropdownMenuItem(
                    text = {
                        // نضع النص داخل Box ملون لنعطيه شكل الزر
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    // تغيير اللون إذا كان هو الخيار المحدد
                                    if (type == selectedType) AppPrimary.copy(alpha = 0.8f) else AppPrimary
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.displayName,
                                color = Color.White,
                                fontWeight = if (type == selectedType) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    },
                    // نزيل الـ padding الافتراضي للعنصر ليأخذ تصميمنا كامل المساحة
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                )
                // --- نهاية التعديل ---
            }
        }
    }
}