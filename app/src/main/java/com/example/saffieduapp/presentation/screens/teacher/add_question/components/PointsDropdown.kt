package com.example.saffieduapp.presentation.screens.teacher.add_question.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsDropdown(
    selectedPoints: String,
    onPointsSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val pointsOptions = remember { (1..10).map { it.toString() } }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, // 180 درجة عند الفتح
        label = ""
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {

        TextField(
            modifier = Modifier
                .width(130.dp)
                .menuAnchor()
                .wrapContentWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(AppPrimary, shape = RoundedCornerShape(12.dp)),
            readOnly = true,
            value = if (selectedPoints.isNotEmpty()) "$selectedPoints درجة" else "",
            onValueChange = {},
          placeholder = { Text("أدخل الدرجة", color = Color.White, fontSize = 13.sp) },
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.arrowdown),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation) // دوران الأيقونة
                        .clickable { expanded = !expanded }
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(AppPrimary)
                .heightIn(max = 250.dp) // يحدد أقصى ارتفاع للقائمة
        ) {
            pointsOptions.forEachIndexed { index, points ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$points درجة",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onClick = {
                        onPointsSelected(points)
                        expanded = false
                    }
                )
                // خط فاصل بين العناصر
                if (index < pointsOptions.lastIndex) {
                    Divider(
                        color = Color.White,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}
