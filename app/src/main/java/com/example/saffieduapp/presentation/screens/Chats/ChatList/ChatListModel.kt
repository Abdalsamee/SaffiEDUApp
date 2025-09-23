package com.example.saffieduapp.presentation.screens.Chats.ChatList

// احذف هذا التعريف:
data class Chat(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0
)
