package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

/**
 * حقل الإجابة المقالية
 */
@Composable
fun EssayAnswerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "اكتب إجابتك هنا:",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = {
                Text(
                    text = "اكتب إجابتك هنا...",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = AppPrimary,
                unfocusedIndicatorColor = Color(0xFFE0E0E0),
                cursorColor = AppPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 8
        )
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun EssayAnswerFieldPreview() {
    SaffiEDUAppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            EssayAnswerField(
                value = "",
                onValueChange = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            EssayAnswerField(
                value = "هذه إجابة تجريبية للسؤال المقالي...",
                onValueChange = {}
            )
        }
    }
}