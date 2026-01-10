package com.example.saffieduapp.presentation.screens.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.saffieduapp.presentation.screens.chat.ViewModel.ChatViewModel
import com.example.saffieduapp.presentation.screens.chat.model.ChatMessage
import com.example.saffieduapp.presentation.screens.chat.model.MessageStatus

@Composable
fun ChatScreen(navController: NavHostController, viewModel: ChatViewModel = viewModel()) {
    val chats by viewModel.chatList.collectAsState()

    // فرض اتجاه اليمين لليسار (RTL) ليتطابق مع واجهة اللغة العربية
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                ChatHeader(onBackClick = { navController.popBackStack() })
            },
            containerColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // شريط البحث
                SearchTextField()

                // قائمة الدردشات
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(chats) { chat ->
                        ChatItemRow(chat)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHeader(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // الارتفاع المطلوب للون الأزرق
        color = Color(0xFF4A90E2),
        shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // أيقونة الرجوع جهة اليمين
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart) // Start في RTL هي اليمين
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // عنوان الصفحة
            Text(
                text = "الدردشة",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun SearchTextField() {
    var text by remember { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("البحث", color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF2F2F2),
            unfocusedContainerColor = Color(0xFFF2F2F2),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun ChatItemRow(chat: ChatMessage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. الصورة الشخصية على اليمين
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
                color = Color(0xFFE0E0E0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. المحتوى في المنتصف (الاسم والرسالة)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.senderName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = chat.lastMessage,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            // 3. الوقت والحالة على اليسار
            Column(horizontalAlignment = Alignment.End) {
                Text(text = chat.time, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                when {
                    chat.unreadCount > 0 -> {
                        Surface(color = Color(0xFFF57C00), shape = CircleShape) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                    chat.status == MessageStatus.READ -> {
                        Icon(Icons.Default.DoneAll, null, tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                    }
                    chat.status == MessageStatus.SENT -> {
                        Icon(Icons.Default.Check, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = Color.Blue.copy(alpha = 0.5f)
        )
    }
}