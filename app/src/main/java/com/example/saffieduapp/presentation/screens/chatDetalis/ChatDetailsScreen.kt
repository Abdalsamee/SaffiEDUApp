package com.example.saffieduapp.presentation.screens.chatDetalis

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.LaunchedEffect
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
    val uiState by viewModel.uiState.collectAsState()
    var currentMessage by remember { mutableStateOf("") }

    // حالة التحكم في التوسع
    var isExpanded by remember { mutableStateOf(false) }

    // دالة الرجوع المخصصة التي تبدأ أنميشن التصغير أولاً
    val onBackAction = {
        isExpanded = false
    }

    // مراقبة انتهاء الأنميشن للرجوع الفعلي للشاشة السابقة
    val headerHeight by animateDpAsState(
        targetValue = if (isExpanded) 220.dp else 65.dp,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "HeaderHeight",
        finishedListener = { finalValue ->
            // إذا كان الهدف هو التصغير (65dp) واكتمل الأنميشن، نخرج من الشاشة
            if (finalValue == 65.dp) {
                navController.popBackStack()
            }
        })

    val cornerSize by animateDpAsState(
        targetValue = if (isExpanded) 40.dp else 0.dp,
        animationSpec = tween(durationMillis = 600),
        label = "CornerSize"
    )

    // تفعيل التوسع عند الدخول
    LaunchedEffect(Unit) {
        isExpanded = true
    }

    // التعامل مع زر الرجوع الخاص بالنظام
    BackHandler(enabled = isExpanded) {
        onBackAction()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                // شريط الإدخال يختفي بسلاسة عند البدء بالرجوع
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(800)),
                    exit = fadeOut(tween(300)) + slideOutVertically { it }) {
                    ChatInputBar(
                        textValue = currentMessage,
                        onValueChange = { currentMessage = it },
                        onSendClick = {
                            if (currentMessage.isNotBlank()) {
                                viewModel.sendMessage(currentMessage)
                                currentMessage = ""
                            }
                        })
                }
            }) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                // البار الذي سيتوسع ويصغر
                CustomExpandingHeader(
                    title = senderName,
                    height = headerHeight,
                    cornerRadius = cornerSize,
                    isContentVisible = headerHeight > 160.dp,
                    onBackClick = { onBackAction() } // استدعاء الأكشن المخصص
                )

                // الرسائل تختفي بتلاشي عند الرجوع
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(delayMillis = 400)),
                    exit = fadeOut(tween(300))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item { DateDivider("اليوم") }
                        items(uiState.messages) { message ->
                            ChatBubble(
                                text = message.text, isMe = message.isMe, time = message.time
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomExpandingHeader(
    title: String,
    height: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp,
    isContentVisible: Boolean,
    onBackClick: () -> Unit
) {
    Surface(
        color = Color(0xFF4A90E2),
        shape = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius),
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(35.dp),
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
                Text(
                    "الدردشة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1.2f))
            }

            AnimatedVisibility(
                visible = isContentVisible, enter = fadeIn() + expandVertically(), exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(85.dp),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp),
                            tint = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
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