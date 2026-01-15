package com.example.saffieduapp.presentation.screens.chat.ViewModel

import androidx.lifecycle.ViewModel
import com.example.saffieduapp.presentation.screens.chat.model.ChatMessage
import com.example.saffieduapp.presentation.screens.chat.model.MessageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {
    private val _chatList = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatList: StateFlow<List<ChatMessage>> = _chatList

    init {
        // بيانات تجريبية لمحاكاة الصورة
        _chatList.value = listOf(
            ChatMessage(
                1, "احمد", "يا استاذ افهمني حبيبك", "15:00 pm", 3, MessageStatus.UNREAD, 0
            ),
            ChatMessage(
                2, "احمد", "يا استاذ افهمني حبيبك", "15:00 pm", 0, MessageStatus.SENT, 0
            ),
            ChatMessage(3, "احمد", "يا استاذ افهمني حبيبك", "أمس", 0, MessageStatus.READ, 0),
            ChatMessage(4, "احمد", "أنت: فش مناقشة يا احمد", "اليوم", 0, MessageStatus.READ, 0)
        )
    }
}