package com.example.saffieduapp.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.chat.ViewModel.ChatViewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.chat.model.ChatMessage
import com.example.saffieduapp.presentation.screens.chat.model.MessageStatus

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val chats by viewModel.chatList.collectAsState()

    // فرض اتجاه اليمين لليسار (RTL) لأن التطبيق باللغة العربية
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                // استخدام الـ Component الموحد الذي أرسلته
                CommonTopAppBar(
                    title = "الدردشة",
                    onNavigateUp = { navController.popBackStack() }
                )
            },
            containerColor = Color.White
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // ١. شريط البحث (Search Bar) أسفل الـ TopAppBar
                SearchTextField()

                // ٢. قائمة المحادثات
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(chats) { chat ->
                        ChatItemRow(chat)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTextField() {
    var text by remember { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = {
            Text("البحث", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        // الأيقونة تكون في الطرف (Trailing)
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
            // الصورة الشخصية على اليمين (بسبب RTL)
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
                color = Color(0xFFE0E0E0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(30.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // النصوص في المنتصف (الاسم والرسالة) محاذاة لليمين
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

            // الوقت والحالة على اليسار (بسبب RTL)
            Column(horizontalAlignment = Alignment.End) {
                Text(text = chat.time, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                when {
                    chat.unreadCount > 0 -> {
                        Surface(color = Color(0xFFF57C00), shape = CircleShape) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
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
        // خط فاصل رفيع
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}