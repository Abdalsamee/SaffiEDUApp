package com.example.saffieduapp.presentation.screens.Chats.ChatList.Component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.AppAlert

import com.example.saffieduapp.presentation.screens.Chats.ChatList.Chat

@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // الأفاتار (على اليسار)
            Image(
                painter = painterResource(id = R.drawable.chats),
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // تفاصيل المحادثة (الاسم والرسالة بالوسط)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.W600,
                    fontSize = 15.sp,
                    color = AppTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chat.lastMessage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // الوقت وحالة القراءة (على اليمين)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = chat.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (chat.unreadCount > 0) {
                    // الرسائل غير المقروءة: دائرة حمراء بعدد الرسائل
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AppAlert)
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    // الرسائل المقروءة: استخدم صورة reed
                    Image(
                        painter = painterResource(id = R.drawable.reed),
                        contentDescription = "Read status",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Divider(color = Color(0xFF4A90E2), thickness = 1.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListItemPreview() {
    Column {
        ChatListItem(
            chat = Chat(
                id = "1",
                name = "إبراهيم حمدان",
                avatarUrl = "https://example.com/avatar.jpg",
                lastMessage = "مرحبا أستاذ، كيف حالك اليوم؟",
                time = "٣:٠٠ م",
                unreadCount = 3
            ),
            onClick = {}
        )
        ChatListItem(
            chat = Chat(
                id = "2",
                name = "محمد علي",
                avatarUrl = "https://example.com/avatar.jpg",
                lastMessage = "تم إرسال الواجب.",
                time = "٢:٤٥ م",
                unreadCount = 0
            ),
            onClick = {}
        )
        ChatListItem(
            chat = Chat(
                id = "3",
                name = "أحمد يوسف",
                avatarUrl = "https://example.com/avatar.jpg",
                lastMessage = "هل هناك محاضرة غداً؟",
                time = "الأمس",
                unreadCount = 1
            ),
            onClick = {}
        )
    }
}
