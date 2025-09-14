package com.example.saffieduapp.presentation.screens.teacher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun ClassDropdown(
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val classes = listOf(
        "الصف الأول", "الصف الثاني", "الصف الثالث", "الصف الرابع",
        "الصف الخامس", "الصف السادس", "الصف السابع", "الصف الثامن",
        "الصف التاسع", "الصف العاشر", "الصف الحادي عشر", "الصف الثاني عشر"
    )

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
            value = selectedClass,
            onValueChange = {},
            placeholder = { Text(text = "حدد الصف الدراسي", color = Color.Gray , fontSize = 13.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            classes.forEach { className ->
                DropdownMenuItem(
                    text = { Text(text = className) },
                    onClick = {
                        onClassSelected(className)
                        expanded = false
                    }
                )
            }
        }
    }
}