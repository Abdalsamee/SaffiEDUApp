package com.example.saffieduapp.presentation.screens.teacher.add_exam.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilePickerBox(
    label: String,
    enabled: Boolean,
    selectedFileName: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onClearSelection: () -> Unit, // <-- باراميتر جديد لإلغاء الاختيار
    iconContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            color = if (enabled) Color.Black else Color.Gray
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (selectedFileName != null) {
                // --- بداية التعديل ---
                // استخدام Row لعرض النص وبجانبه زر الإلغاء
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedFileName,
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f), // ليأخذ المساحة المتبقية
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = onClearSelection) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Selection"
                        )
                    }
                }
                // --- نهاية التعديل ---
            } else {
                iconContent()
            }
        }
    }
}