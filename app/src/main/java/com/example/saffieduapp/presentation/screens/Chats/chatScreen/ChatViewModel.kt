package com.example.saffieduapp.presentation.screens.Chats.chatScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val id: String,
    val text: String,
    val timestamp: String,
    val isSentByMe: Boolean,
    val date: String // التاريخ الكامل لتجميع الرسائل
)

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StuChatViewModel : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            val currentDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())

            val newMessage = Message(
                id = System.currentTimeMillis().toString(),
                text = text,
                timestamp = currentTime,
                isSentByMe = true,
                date = currentDate
            )
            val updatedMessages = _chatState.value.messages + newMessage
            _chatState.value = _chatState.value.copy(messages = updatedMessages)
        }
    }
}