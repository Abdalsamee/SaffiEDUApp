package com.example.saffieduapp.presentation.screens.Chats.tetcherChat.component


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.Cairo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TetcherMessageInputBar(
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {




        // حقل إدخال الرسالة
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("اكتب رسالة..", color = Color.Gray, fontFamily = Cairo) },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE5E5E5)),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // أيقونة إرسال (سهم أزرق)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E88E5)) // لون أزرق
                .clickable(enabled = text.isNotBlank()) {
                    onSendMessage(text)
                    text = ""
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.send), // استبدل بمسار أيقونة السهم
                contentDescription = "Send Message",
                tint = Color.White
            )
        }

        //Spacer(modifier = Modifier.width(8.dp))

        // أيقونة الميكروفون
//        IconButton(
//            onClick = { /* Handle voice input */ },
//            modifier = Modifier.size(48.dp)
//        ) {
//            Icon(
//                painter = painterResource(id = R.drawable.send), // استبدل بمسار أيقونة الميكروفون
//                contentDescription = "Microphone",
//                tint = Color.Gray
//            )
//        }
    }
}

@Preview(showBackground = true  , locale = "ar")
@Composable
fun MessageInputBarPreview() {
    TetcherMessageInputBar(onSendMessage = {})
}