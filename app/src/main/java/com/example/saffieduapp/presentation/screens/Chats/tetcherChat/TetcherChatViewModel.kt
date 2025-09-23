package com.example.saffieduapp.presentation.screens.Chats.tetcherChat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TetcherMessage(
    val id: String,
    val text: String,
    val timestamp: String,
    val isSentByMe: Boolean
)

data class TetcerChatState(
    val messages: List<TetcherMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TetcherChatViewModel : ViewModel() {

    private val _chatState = MutableStateFlow(TetcerChatState())
    val chatState: StateFlow<TetcerChatState> = _chatState

    // دالة لإرسال رسالة جديدة
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // تنسيق الوقت الحالي إلى ساعات ودقائق بصيغة h:mm a
            val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

            val newMessage = TetcherMessage(
                id = System.currentTimeMillis().toString(),
                text = text,
                timestamp = currentTime,
                isSentByMe = true
            )
            val updatedMessages = _chatState.value.messages + newMessage
            _chatState.value = _chatState.value.copy(messages = updatedMessages)
        }
    }
}