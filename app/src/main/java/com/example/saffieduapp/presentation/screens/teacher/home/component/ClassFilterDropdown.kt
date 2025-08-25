package com.example.saffieduapp.presentation.screens.teacher.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.Cairo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassFilterDropdown(
    classes: List<String>,
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = selectedClass,
            onValueChange = {},
            readOnly = true,
            textStyle = TextStyle(fontSize = 12.sp , fontFamily = Cairo, fontWeight = FontWeight.Medium),
            // أيقونة الفلتر
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.filter),
                    contentDescription = "Filter Icon"
                )
            },
            shape = RoundedCornerShape(8.dp),
            // تخصيص الألوان لتبدو مثل شريحة الفلتر
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppPrimary,
                unfocusedContainerColor = AppPrimary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White
            )
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            // تحديد أقصى ارتفاع للقائمة ليعرض 3 عناصر فقط ويصبح قابلًا للتمرير
            modifier = Modifier.heightIn(max = 144.dp) // (3 items * 48.dp per item)
        ) {
            classes.forEach { className ->
                DropdownMenuItem(
                    text = { Text(text = className,
                        fontSize = 12.sp,
                        fontFamily = Cairo

                        ) },
                    onClick = {
                        onClassSelected(className) // تحديث الاختيار
                        isExpanded = false // إغلاق القائمة
                    }
                )
            }
        }
    }
}