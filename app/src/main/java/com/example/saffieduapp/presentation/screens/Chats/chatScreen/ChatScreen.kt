package com.example.saffieduapp.presentation.screens.Chats.chatScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.Chats.chatScreen.component.ChatHeader
import com.example.saffieduapp.presentation.screens.Chats.chatScreen.component.DateHeader
import com.example.saffieduapp.presentation.screens.Chats.chatScreen.component.MessageBubble
import com.example.saffieduapp.presentation.screens.Chats.chatScreen.component.MessageInputBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: StuChatViewModel = viewModel(),
    onNavigateUp: () -> Unit
) {
    val chatState by viewModel.chatState.collectAsState()

    val sortedMessages = chatState.messages.sortedBy { it.id }

    val chatItems = mutableListOf<Any>()
    var lastDate: String? = null

    sortedMessages.forEach { message ->
        if (message.date != lastDate) {
            chatItems.add(message.date)
            lastDate = message.date
        }
        chatItems.add(message)
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "الدردشة",
                onNavigateUp = onNavigateUp
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .imePadding() // هنا التعديل الجديد: لتجنب التمدد فوق لوحة المفاتيح
            ) {
                MessageInputBar(onSendMessage = viewModel::sendMessage)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppPrimary)
                .padding(paddingValues)
        ) {
            ChatHeader(
                userName = "أحمد عمران",
                userImageResId = R.drawable.tetcher
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 70.dp, topEnd = 70.dp))
                    .background(Color.White)
                    .padding(top = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(chatItems.reversed()) { item ->
                        when (item) {
                            is String -> DateHeader(date = item)
                            is Message -> Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (item.isSentByMe) Alignment.End else Alignment.Start
                            ) {

                                Spacer(modifier = Modifier.height(4.dp))
                                MessageBubble(message = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
fun ChatScreenPreview() {
    ChatScreen(onNavigateUp = {})
}