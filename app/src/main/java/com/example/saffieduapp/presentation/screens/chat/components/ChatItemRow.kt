package com.example.saffieduapp.presentation.screens.chat.components

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
    navController: NavController, viewModel: ChatViewModel = viewModel()
) {
    val chats by viewModel.chatList.collectAsState()

    // فرض اتجاه اليمين لليسار (RTL) ليتطابق مع واجهة اللغة العربية والصورة
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                // استخدام المكون المشترك الخاص بك
                CommonTopAppBar(
                    title = "الدردشة", onNavigateUp = { navController.popBackStack() })
            }, containerColor = Color.White
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // شريط البحث
                SearchTextField()

                // قائمة الدردشات
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
            Text(
                "البحث",
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        // الأيقونة جهة اليسار (في نظام RTL تكون TrailingIcon هي اليسار)
        trailingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
        },
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
            // 1. الصورة الشخصية (تظهر على اليمين بسبب RTL)
            Surface(
                shape = CircleShape, modifier = Modifier.size(52.dp), color = Color(0xFFE0E0E0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(30.dp)
                    )
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
                    text = chat.lastMessage, color = Color.Gray, fontSize = 14.sp, maxLines = 1
                )
            }

            // 3. الوقت والحالة (تظهر على اليسار بسبب RTL)
            Column(horizontalAlignment = Alignment.End) {
                Text(text = chat.time, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                when {
                    chat.unreadCount > 0 -> {
                        Surface(
                            color = Color(0xFFF57C00), // اللون البرتقالي للإشعارات
                            shape = CircleShape
                        ) {
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
                        Icon(
                            Icons.Default.DoneAll,
                            null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    chat.status == MessageStatus.SENT -> {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // الخط الفاصل الأزرق المعدل: يبدأ من اليمين وينتهي قبل الصورة الشخصية
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 80.dp), // الإزاحة لتجنب الوصول لتحت الصورة
            thickness = 1.5.dp, // زيادة السمك ليصبح واضحاً مثل الصورة
            color = Color(0xFF4A90E2) // هذا هو كود اللون الأزرق السماوي الموجود في التصميم العلوي
        )
    }
}