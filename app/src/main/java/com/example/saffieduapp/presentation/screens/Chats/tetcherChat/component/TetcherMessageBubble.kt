package com.example.saffieduapp.presentation.screens.Chats.tetcherChat.component



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

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp

//import com.example.saffieduapp.presentation.screens.Chats.studantChat.Message
import com.example.saffieduapp.presentation.screens.Chats.tetcherChat.TetcherMessage

//import com.example.saffieduapp.presentation.screens.chat.Message

import com.example.saffieduapp.ui.theme.AppPrimary

import com.example.saffieduapp.ui.theme.CardBackgroundColor

import com.example.saffieduapp.ui.theme.AppTextPrimary

import com.example.saffieduapp.ui.theme.AppTextSecondary



@Composable

fun TetcherMessageBubble(message: TetcherMessage) {

    val bubbleColor = if (message.isSentByMe) AppPrimary else CardBackgroundColor

    val textColor = if (message.isSentByMe) Color.White else AppTextPrimary

    val timeColor = if (message.isSentByMe) Color.White.copy(alpha = 0.6f) else AppTextSecondary.copy(alpha = 0.6f)



    val bubbleShape = if (message.isSentByMe) {

// رسالة صادرة: زاوية مسطحة في الأسفل لليمين

        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)

    } else {

// رسالة واردة: زاوية مسطحة في الأسفل لليسار

        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)

    }



    Row(

        modifier = Modifier

            .fillMaxWidth()

            .padding(horizontal = 8.dp, vertical = 4.dp),

        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start

    ) {

        Box(

            modifier = Modifier

                .widthIn(max = 280.dp) // لا تزيد عن 75% من عرض الشاشة

                .clip(bubbleShape)

                .background(bubbleColor)

                .padding(horizontal = 12.dp, vertical = 8.dp)

        ) {

            Column {

                Text(

                    text = message.text,

                    color = textColor,

                    style = MaterialTheme.typography.bodyLarge,

                    modifier = Modifier.padding(bottom = 4.dp)

                )

                Text(

                    text = message.timestamp,

                    color = timeColor,

                    style = MaterialTheme.typography.labelSmall,

                    modifier = Modifier.align(Alignment.End)

                )

            }

        }

    }

}



@Preview(showBackground = true, locale = "ar")

@Composable

fun MessageBubblePreview() {

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        TetcherMessageBubble(message = TetcherMessage("1", "مرحبا استاذ احمد كيف حالك اليوم", "01:22 am", false))

        Spacer(modifier = Modifier.height(8.dp))

        TetcherMessageBubble(message = TetcherMessage("2", "اهلا, ابراهيم كيف اساعدك", "02:22 am", true))

        Spacer(modifier = Modifier.height(8.dp))

        TetcherMessageBubble(message = TetcherMessage("3", "مرحبا استاذ احمد كيف حالك اليوم", "01:22 am", false))

        Spacer(modifier = Modifier.height(8.dp))

        TetcherMessageBubble(message = TetcherMessage("4", "اهلا, ابراهيم كيف اساعدك", "02:22 am", true))

    }

}