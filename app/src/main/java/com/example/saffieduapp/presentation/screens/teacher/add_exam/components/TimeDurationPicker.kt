package com.example.saffieduapp.presentation.screens.teacher.add_exam.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDurationPicker(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // زر الإنقاص
        IconButton(
            onClick = {
                val currentValue = value.toIntOrNull() ?: 0
                if (currentValue > 0) {
                    onValueChange((currentValue - 5).toString()) // ينقص 5 دقائق
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease time")
        }

        // حقل الإدخال الرقمي بنفس ستايل الحقول الأخرى
        TextField(
            value = value,
            onValueChange = { newValue ->
                // السماح فقط بإدخال الأرقام
                if (newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier
                .weight(1f)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            placeholder = { Text("المدة (بالدقائق)", color = Color.Gray, fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        // زر الزيادة
        IconButton(
            onClick = {
                val currentValue = value.toIntOrNull() ?: 0
                onValueChange((currentValue + 5).toString()) // يزيد 5 دقائق
            }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase time")
        }
    }
}
