package com.example.saffieduapp.presentation.screens.Chats.chatScreen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DateHeader(date: String) {
    val currentDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())

    val displayDate = if (date == currentDate) {
        "اليوم"
    } else {
        date
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayDate,
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 15.sp,
            fontWeight = FontWeight.W500

        )
    }
}