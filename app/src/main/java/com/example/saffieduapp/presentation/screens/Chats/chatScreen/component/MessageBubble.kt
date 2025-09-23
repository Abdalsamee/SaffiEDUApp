package com.example.saffieduapp.presentation.screens.Chats.chatScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.Chats.chatScreen.Message
//import com.example.saffieduapp.presentation.screens.Chats.chatScreen.StuMessage
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.CardBackgroundColor
import com.example.saffieduapp.ui.theme.AppTextPrimary

@Composable
fun MessageBubble(message: Message) {
    val bubbleColor = if (message.isSentByMe) AppPrimary else CardBackgroundColor
    val textColor = if (message.isSentByMe) Color.White else AppTextPrimary
    val timeColor = Color(0xFF828282)

    val bubbleShape = if (message.isSentByMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isSentByMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W500
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.timestamp,
                color = timeColor,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.W500
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
fun MessageBubblePreview() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        MessageBubble(message = Message("1", "مرحبا استاذ احمد كيف حالك اليوم", "01:22 am", isSentByMe = false, date = "September 23, 2025"))
        Spacer(modifier = Modifier.height(8.dp))
        MessageBubble(message = Message("2", "اهلا, ابراهيم كيف اساعدك", "02:22 am", isSentByMe = true, date = "September 23, 2025"))
        Spacer(modifier = Modifier.height(8.dp))
        MessageBubble(message = Message("3", "مرحبا استاذ احمد كيف حالك اليوم", "01:22 am", isSentByMe = false, date = "September 23, 2025"))
        Spacer(modifier = Modifier.height(8.dp))
        MessageBubble(message = Message("4", "اهلا, ابراهيم كيف اساعدك", "02:22 am", isSentByMe = true, date = "September 23, 2025"))
    }
}