package com.example.saffieduapp.presentation.screens.Chats.tetcherChat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.saffieduapp.presentation.screens.Chats.studantChat.component.ChatHeader
//import com.example.saffieduapp.presentation.screens.Chats.studantChat.component.MessageBubble
//import com.example.saffieduapp.presentation.screens.Chats.studantChat.component.MessageInputBar
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.ui.theme.AppPrimary
//import com.example.saffieduapp.presentation.screens.chat.ChatViewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.Chats.tetcherChat.component.TetcherChatHeader
import com.example.saffieduapp.presentation.screens.Chats.tetcherChat.component.TetcherMessageBubble
import com.example.saffieduapp.presentation.screens.Chats.tetcherChat.component.TetcherMessageInputBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TetcherChatScreen(
    viewModel: TetcherChatViewModel = viewModel(),
    onNavigateUp: () -> Unit
) {
    val chatState by viewModel.chatState.collectAsState()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "الدردشة",
                onNavigateUp = onNavigateUp
            )
        },
        bottomBar = {
            // شريط إدخال الرسائل داخل بوكس أبيض
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // تم تطبيق الخلفية البيضاء هنا
            ) {
                TetcherMessageInputBar(onSendMessage = viewModel::sendMessage)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppPrimary)
                .padding(paddingValues)
        ) {
            // الهيدر المخصص أسفل شريط التطبيق
            TetcherChatHeader(
                userName = "ابراهيم حمدان",
                userImageResId = R.drawable.tetcher // استبدل بمسار صورتك
            )

            Spacer(modifier = Modifier.height(16.dp))

            // البوكس الأبيض الذي يحتوي على الرسائل
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // لجعل البوكس يأخذ المساحة المتاحة
                    .clip(RoundedCornerShape(topStart = 70.dp, topEnd = 70.dp))
                    .background(Color.White)
                    .padding(top = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatState.messages.reversed()) { message ->
                        TetcherMessageBubble(message = message)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
fun ChatScreenPreview() {
    TetcherChatScreen(onNavigateUp = {})
}