package com.example.saffieduapp.presentation.screens.teacher.quiz_summary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.presentation.screens.teacher.quiz_summary.QuizSummaryScreen
import com.example.saffieduapp.ui.theme.AppAlert
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun QuestionSummaryItem(
    questionText: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // النص مع خلفية منفصلة
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFACD2FF), // لون أزرق فاتح للنص
            tonalElevation = 1.dp
        ) {
            Text(
                text = questionText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
        }

        // زر التعديل
        Button(
            onClick = onEditClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
            modifier = Modifier.height(48.dp)
        ) {
            Text("تعديل", color = Color.White)
        }

        // زر الحذف
        Button(
            onClick = onDeleteClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppAlert),
            modifier = Modifier.height(48.dp)
        ) {
            Text("حذف", color = Color.White)
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun QuizSummitem() {
    SaffiEDUAppTheme {
        Column {
            QuestionSummaryItem(
                "السؤال الأول",
                {},
                {}
            )
            QuestionSummaryItem(
                "السؤال الأول السؤال الأول السؤال الأول السؤال الأول السؤال الأول",
                {},
                {}
            )
        }
    }
}