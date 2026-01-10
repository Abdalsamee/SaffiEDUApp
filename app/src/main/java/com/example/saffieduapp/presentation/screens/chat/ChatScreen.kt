package com.example.saffieduapp.presentation.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.saffieduapp.presentation.screens.chat.ViewModel.ChatViewModel
import com.example.saffieduapp.presentation.screens.chat.components.ChatItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController, viewModel: ChatViewModel = viewModel()
) {
    val chats by viewModel.chatList.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                Text("الدردشة", style = TextStyle(fontWeight = FontWeight.Bold))
            }, navigationIcon = {
                IconButton(onClick = { /* Handle back */ }) {
                    Icon(Icons.Default.ArrowBackIos, contentDescription = null)
                }
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFF4A90E2),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
            )
        }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchTextField()

            LazyColumn {
                items(chats) { chat ->
                    ChatItemRow(chat)
                }
            }
        }
    }
}

@Composable
fun SearchTextField() {
    TextField(
        value = "",
        onValueChange = {},
        placeholder = {
            Text("البحث", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF2F2F2),
            unfocusedContainerColor = Color(0xFFF2F2F2),
            disabledContainerColor = Color(0xFFF2F2F2),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}