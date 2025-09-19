package com.example.saffieduapp.presentation.screens.teacher.add_question.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsDropdown(
    selectedPoints: String,
    onPointsSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    // إنشاء قائمة الدرجات من 1 إلى 10
    val pointsOptions = remember { (1..10).map { it.toString() } }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        // الحقل بنفس ستايل باقي الحقول
        TextField(
            modifier = Modifier
                .width(130.dp) // تحديد عرض ثابت للمكون
                .menuAnchor()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            readOnly = true,
            value = if (selectedPoints.isNotEmpty()) "$selectedPoints درجة" else "",
            onValueChange = {},
            placeholder = { Text("الدرجة", color = Color.Gray, fontSize = 13.sp) },
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        // القائمة باللون الأزرق والنصوص باللون الأبيض
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppPrimary) // أزرق
        ) {
            pointsOptions.forEach { points ->
                DropdownMenuItem(
                    text = { Text(text = "$points درجة", color = Color.White) },
                    onClick = {
                        onPointsSelected(points)
                        expanded = false
                    }
                )
            }
        }
    }
}
