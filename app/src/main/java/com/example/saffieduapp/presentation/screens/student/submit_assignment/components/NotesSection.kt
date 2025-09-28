package com.example.saffieduapp.presentation.screens.student.submit_assignment.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.Cairo

@Composable
fun NotesSection() {
    Column {
        Text(text = "ملاحظات:", fontWeight = FontWeight.Bold)
        InstructionItem(text = "تأكد من إرفاق جميع الملفات المطلوبة بشكل صحيح.")
        InstructionItem(text = "راجع الواجب بدقة وتأكد من استكمال كل النقاط المطلوبة.")
        InstructionItem(text = "تأكد من الالتزام بالموعد النهائي لتفادي أي خصم أو تأخير.")
        InstructionItem(text = "حجم أقصى للملف: 10 MB")
        InstructionItem(text = " يدعم: PDF, DOCX, PNG, JPG")
    }
}

@Composable
private fun InstructionItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(8.dp)
        ) {
            drawCircle(color = Color.Black)
        }

        Text(text = text, fontSize = 14.sp, color = Color.Black, fontFamily = Cairo, fontWeight = FontWeight.SemiBold)
    }
}