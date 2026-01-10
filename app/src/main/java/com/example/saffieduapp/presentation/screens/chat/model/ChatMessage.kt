package com.example.saffieduapp.presentation.screens.chat.model

enum class MessageStatus { SENT, READ, UNREAD }

data class ChatMessage(
    val id: Int,
    val senderName: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val status: MessageStatus,
    val profileImageRes: Int // سنستخدم ريسورس مؤقتاً
)