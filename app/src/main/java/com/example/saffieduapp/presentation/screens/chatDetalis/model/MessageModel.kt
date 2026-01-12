package com.example.saffieduapp.presentation.screens.chatDetalis.model

data class MessageModel(
    val id: Int, val text: String, val isMe: Boolean, val time: String
)

data class ChatUiState(
    val messages: List<MessageModel> = emptyList(),
    val isLoading: Boolean = false,
    val senderName: String = ""
)
