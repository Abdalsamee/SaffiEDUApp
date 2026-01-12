package com.example.saffieduapp.presentation.screens.chatDetalis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.chatDetalis.ViewModel.ChatDetailViewModel
import com.example.saffieduapp.presentation.screens.chatDetalis.component.ChatInputBar

@Composable
fun ChatDetailScreen(
    navController: NavController,
    senderName: String,
    viewModel: ChatDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // مراقبة الحالة من الـ ViewModel
    val uiState by viewModel.uiState.collectAsState()
    var currentMessage by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(topBar = {
            CustomChatDetailHeader(
                title = senderName, onBackClick = { navController.popBackStack() })
        }, bottomBar = {
            ChatInputBar(
                textValue = currentMessage,
                onValueChange = { currentMessage = it },
                onSendClick = {
                    viewModel.sendMessage(currentMessage)
                    currentMessage = "" // مسح الحقل بعد الإرسال
                })
        }) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White),
                contentPadding = PaddingValues(16.dp)
            ) {
                item { DateDivider("اليوم") }

                // عرض الرسائل من الـ State
                items(uiState.messages) { message ->
                    ChatBubble(
                        text = message.text, isMe = message.isMe, time = message.time
                    )
                }
            }
        }
    }
}


@Composable
fun CustomChatDetailHeader(title: String, onBackClick: () -> Unit) {
    Surface(
        color = Color(0xFF4A90E2),
        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // صف الرجوع
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("الدردشة", color = Color.White, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1.2f))
            }

            // الصورة الشخصية كما في الصور
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(85.dp),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun ChatBubble(text: String, isMe: Boolean, time: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) Color(0xFF4A90E2) else Color(0xFFE1EDF9), shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 0.dp,
                bottomEnd = if (isMe) 0.dp else 16.dp
            ), modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                color = if (isMe) Color.White else Color.Black
            )
        }
        Text(
            text = time,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun DateDivider(date: String) {
    Text(
        text = date,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        textAlign = TextAlign.Center,
        color = Color.Gray,
        fontSize = 14.sp
    )
}

@Composable
fun ChatInputBar() {
    // شريط الإرسال مع أيقونة التسجيل الصوتي كما في صورة الطالب
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = Color(0xFF4A90E2), modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            placeholder = { Text("اكتب رسالتك...") },
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}