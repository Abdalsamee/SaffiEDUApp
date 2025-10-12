package com.example.saffieduapp.presentation.screens.teacher.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.R
@Composable
fun ClassFilterButton(
    selectedClass: String,
    onClassSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val classes = listOf("الصف الرابع", "الصف الخامس", "الصف السادس", "الصف السابع")

    // شكل الزوايا (منحنية فقط من الأسفل)
    val bottomRoundedShape: Shape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )

    // 🔹 نستخدم Surface لإضافة الظل (elevation)
    Surface(
        shape = bottomRoundedShape,
        color = Color.White,
        shadowElevation = 8.dp, // ظل ناعم واضح
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth(0.40f)
            .wrapContentHeight()
            .clickable { expanded = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedClass,
                color = Color(0xFF1C1C1C),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = R.drawable.filter),
                contentDescription = "فلترة الصفوف",
                tint = AppPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        // 🔹 القائمة المنسدلة
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .padding(vertical = 4.dp)
        ) {
            classes.forEach { className ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = className,
                            color = if (className == selectedClass) AppPrimary else AppTextSecondary,
                            fontSize = 15.sp
                        )
                    },
                    onClick = {
                        expanded = false
                        onClassSelected(className)
                    }
                )
            }
        }
    }
}
