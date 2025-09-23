package com.example.saffieduapp

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.saffieduapp.presentation.screens.Chats.ChatList.ChatListScreen
//import com.example.saffieduapp.presentation.screens.Chats.studantChat.ChatScreen // تأكد من استيراد المسار الصحيح
import com.example.saffieduapp.presentation.screens.Chats.studantChat.StuChatScreen
import com.example.saffieduapp.presentation.screens.Chats.tetcherChat.TetcherChatScreen
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f // تثبيت حجم الخط على القيمة الافتراضية
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SaffiEDUAppTheme {
                //ChatListScreen(onChatClick = {}) // تم تعطيل شاشة قائمة الدردشات
                StuChatScreen(onNavigateUp = {}) // تم تفعيل شاشة الدردشة
                //TetcherChatScreen(onNavigateUp = {})
            }
        }
    }
}