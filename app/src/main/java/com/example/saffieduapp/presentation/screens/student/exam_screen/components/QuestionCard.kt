package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

/**
 * بطاقة عرض نص السؤال
 */
@Composable
fun QuestionCard(
    questionText: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = questionText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Start,
                lineHeight = 28.sp
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun QuestionCardPreview() {
    SaffiEDUAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuestionCard(
                questionText = "هو طلب للمعلومة أو المعرفة أو البيانات، وهو أسلوب يستخدم لجمع المعلومات، أو طلب شيء، أو طلب تركيع أو طلب شيء؟"
            )
        }
    }
}