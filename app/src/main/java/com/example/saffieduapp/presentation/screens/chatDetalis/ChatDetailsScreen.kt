package com.example.saffieduapp.presentation.screens.chatDetalis

import android.R.attr.alpha
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.chatDetalis.ViewModel.ChatDetailViewModel
import com.example.saffieduapp.presentation.screens.chatDetalis.component.ChatInputBar
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.ui.theme.Cairo

@Composable
fun ChatDetailScreen(
    navController: NavController,
    senderName: String,
    viewModel: ChatDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentMessage by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    // يبدأ من 100dp (نفس الشاشة السابقة) ويتمدد لـ 240dp
    val headerHeight by animateDpAsState(
        targetValue = if (isExpanded) 240.dp else 100.dp,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        finishedListener = { if (it == 100.dp) navController.popBackStack() })

    // يبدأ من 20dp (نفس الشاشة السابقة) وينتهي بـ 40dp
    val cornerSize by animateDpAsState(
        targetValue = if (isExpanded) 40.dp else 20.dp, animationSpec = tween(durationMillis = 600)
    )

    LaunchedEffect(Unit) { isExpanded = true }

    BackHandler(enabled = isExpanded) { isExpanded = false }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    title = if (headerHeight > 160.dp) "" else "الدردشة",
                    height = headerHeight,
                    bottomCorner = cornerSize,
                    onNavigateUp = { isExpanded = false },
                    expandableContent = {
                        // محتوى الصورة والاسم
                        if (headerHeight > 160.dp) {
                            DetailHeaderContent(senderName)
                        }
                    })
            }) { padding ->
            // التعديل هنا: نستخدم Column بسيط بدون AnimatedVisibility معقدة
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                // القائمة تظل موجودة لكن شفافيتها مرتبطة بحجم البار
                LazyColumn(
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        // الشفافية تقل كلما صغر البار (عندما يقترب من 100dp)
                        alpha =
                            ((headerHeight.toPx() - 100.dp.toPx()) / 140.dp.toPx()).coerceIn(
                                0f, 1F
                            )
                    }, contentPadding = PaddingValues(16.dp)
                ) {
                    item { DateDivider("اليوم") }
                    items(uiState.messages) { message ->
                        ChatBubble(message.text, message.isMe, message.time)
                    }
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
@Composable
fun DetailHeaderContent(senderName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 10.dp)
    ) {
        // دائرة الصورة الشخصية
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(80.dp),
            border = BorderStroke(2.dp, Color.White),
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // اسم المرسل
        Text(
            text = senderName,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            fontFamily = Cairo // تأكد من أن Cairo معرفة لديك أو استبدلها بـ FontFamily.Default
        )
    }
}