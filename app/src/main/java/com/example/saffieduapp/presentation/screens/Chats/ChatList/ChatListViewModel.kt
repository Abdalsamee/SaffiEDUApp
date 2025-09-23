package com.example.saffieduapp.presentation.screens.Chats.ChatList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
//import com.example.saffieduapp.presentation.screens.Chats.ChatList.Chat


// مستودع البيانات (Repository) للمحاكاة
class MockChatRepository {
    fun getChatList(): List<Chat> {
        return listOf(
            Chat(
                id = "1",
                name = "ابراهيم حمدان",
                avatarUrl = "https://example.com/avatar1.jpg",
                lastMessage = "مرحبا استاذ، كيف حالك اليوم؟",
                time = "7:00 م",
                unreadCount = 3
            ),
            Chat(
                id = "2",
                name = "محمد علي",
                avatarUrl = "https://example.com/avatar2.jpg",
                lastMessage = "تم إرسال الواجب.",
                time = "2:30 م",
                unreadCount = 0
            ),
            Chat(
                id = "3",
                name = "أحمد يوسف",
                avatarUrl = "https://example.com/avatar3.jpg",
                lastMessage = "هل هناك محاضرة غداً؟",
                time = "الأمس",
                unreadCount = 1
            )
        )
    }
}

// ViewModel
class ChatListViewModel(private val repository: MockChatRepository = MockChatRepository()) : ViewModel() {
    private val _chatListState = MutableStateFlow<ChatListState>(ChatListState.Loading)
    val chatListState: StateFlow<ChatListState> = _chatListState.asStateFlow()

    init {
        loadChatList()
    }

    fun loadChatList() {
        viewModelScope.launch {
            try {
                val chats = repository.getChatList()
                _chatListState.value = if (chats.isEmpty()) ChatListState.Empty else ChatListState.Success(chats)
            } catch (e: Exception) {
                _chatListState.value = ChatListState.Error("فشل تحميل المحادثات")
            }
        }
    }
}

// حالات الشاشة
sealed class ChatListState {
    object Loading : ChatListState()
    object Empty : ChatListState()
    data class Success(val chats: List<Chat>) : ChatListState()
    data class Error(val message: String) : ChatListState()
}
