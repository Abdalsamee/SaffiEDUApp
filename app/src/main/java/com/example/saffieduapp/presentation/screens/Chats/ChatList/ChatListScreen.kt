package com.example.saffieduapp.presentation.screens.Chats.ChatList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.saffieduapp.presentation.screens.Chats.ChatList.Chat
import com.example.saffieduapp.presentation.screens.Chats.ChatList.Component.ChatListItem
import com.example.saffieduapp.presentation.screens.Chats.ChatList.Component.CustomSearchBar
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
//import com.example.saffieduapp.presentation.screens.Chats.ChatList.Chat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (chatId: String) -> Unit,
    viewModel: ChatListViewModel = viewModel()
) {
    val chatListState by viewModel.chatListState.collectAsState()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "الدردشة",
                onNavigateUp = null
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            CustomSearchBar()
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = chatListState) {
                is ChatListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ChatListState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لا توجد محادثات",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is ChatListState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items = state.chats, key = { it.id }) { chat: Chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = { onChatClick(chat.id) }
                            )
                        }
                    }
                }
                is ChatListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
fun ChatListScreenPreview() {
    ChatListScreen(onChatClick = {})
}
