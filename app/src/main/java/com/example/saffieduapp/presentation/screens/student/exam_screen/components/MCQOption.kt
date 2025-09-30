package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppAccent
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

/**
 * خيار اختيار واحد (Single Choice / True-False)
 */
@Composable
fun MCQSingleOption(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 20.dp,
                    bottomEnd = 0.dp,
                    bottomStart =20 .dp
                )
            )
            .background(
                color = if (isSelected) AppAccent.copy(alpha = 0.15f)
                else Color(0xFFE8F4FD),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 20.dp,
                    bottomEnd = 0.dp,
                    bottomStart =20 .dp
                )
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) AppAccent else Color.Transparent,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 20.dp,
                    bottomEnd = 0.dp,
                    bottomStart =20 .dp
                )
            )

            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )

        // دائرة خضراء عند التحديد
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color = AppAccent, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color = Color.White, shape = CircleShape)
                )
            }
        } else {
            // دائرة فارغة
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(width = 2.dp, color = Color.Gray, shape = CircleShape)
            )
        }
    }
}

/**
 * خيار اختيار متعدد (Multiple Choice)
 */
@Composable
fun MCQMultipleOption(
    text: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 20.dp,
                    bottomEnd = 0.dp,
                    bottomStart =20 .dp
                )
            )
            .background(
                color = if (isSelected) AppAccent.copy(alpha = 0.15f)
                else Color(0xFFE8F4FD),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 20.dp,
                    bottomEnd = 0.dp,
                    bottomStart =20 .dp
                )
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) AppAccent else Color.Transparent,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 20.dp,
                    bottomEnd = 0.dp,
                    bottomStart =20 .dp
                )
            )
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )

        // Checkbox
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = AppAccent,
                uncheckedColor = Color.Gray,
                checkmarkColor = Color.White
            )
        )
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun MCQOptionPreview() {
    SaffiEDUAppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Single Choice:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            MCQSingleOption(
                text = "الخيار الأول",
                isSelected = true,
                onSelect = {}
            )

            MCQSingleOption(
                text = "الخيار الثاني",
                isSelected = false,
                onSelect = {}
            )

            MCQSingleOption(
                text = "الخيار الثالث",
                isSelected = false,
                onSelect = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Multiple Choice:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            MCQMultipleOption(
                text = "الخيار الأول",
                isSelected = true,
                onToggle = {}
            )

            MCQMultipleOption(
                text = "الخيار الثاني",
                isSelected = true,
                onToggle = {}
            )

            MCQMultipleOption(
                text = "الخيار الثالث",
                isSelected = false,
                onToggle = {}
            )
        }
    }
}