package com.example.saffieduapp.presentation.screens.chatDetalis.ViewModel

import androidx.lifecycle.ViewModel
import com.example.saffieduapp.presentation.screens.chatDetalis.model.ChatUiState
import com.example.saffieduapp.presentation.screens.chatDetalis.model.MessageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadFakeMessages()
    }

    private fun loadFakeMessages() {
        // محاكاة تحميل بيانات من قاعدة بيانات أو API
        val fakeMessages = listOf(
            MessageModel(1, "أهلاً ديل، لا يوجد مناقشة لك", false, "01:22 am"),
            MessageModel(2, "مرحباً أنا الطالب ديلفراسو، أريد أن أناقش", true, "02:22 am"),
            MessageModel(3, "أهلاً ديل، لا يوجد مناقشة لك", false, "01:22 am")
        )
        _uiState.value = _uiState.value.copy(messages = fakeMessages)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val newMessage = MessageModel(
            id = (_uiState.value.messages.size + 1),
            text = text,
            isMe = true,
            time = "الآن"
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + newMessage
        )
    }
}