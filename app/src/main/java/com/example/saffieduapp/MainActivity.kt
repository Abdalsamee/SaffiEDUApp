package com.example.saffieduapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.os.postDelayed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.navigation.authNavGraph
import com.example.saffieduapp.presentation.screens.MainAppScreen
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SaffiEDUAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "auth_graph"
                ) {
                    authNavGraph(navController)

                    composable(route = "main_graph") {
                        MainAppScreen()
                    }
                }
            }
        }

        // بدء الاستماع للإشعارات عندما يكون التطبيق مفتوحاً
        startNotificationListening()
    }

    private fun startNotificationListening() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1. جلب صف الطالب من Firestore
        FirebaseFirestore.getInstance().collection("students")
            .document(userId)
            .get()
            .addOnSuccessListener { studentDocument ->
                val studentGrade = studentDocument.getString("grade") ?: return@addOnSuccessListener

                // 2. الاستماع للدروس المجدولة لهذا الصف
                listenForScheduledLessons(studentGrade)
            }
            .addOnFailureListener {
                // إذا فشل جلب grade، حاول بالقيمة الافتراضية
                listenForScheduledLessons("الصف الرابع")
            }
    }

    private fun listenForScheduledLessons(studentGrade: String) {

        Log.d("Notifications", "🔍 جلب الدروس للصف: $studentGrade")

        // استخدم get() بدلاً من addSnapshotListener للتحقق الفوري
        FirebaseFirestore.getInstance().collection("lessons")
            .whereEqualTo("className", studentGrade)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("Notifications", "✅ عدد الدروس الموجودة: ${documents.size()}")

                documents.forEach { document ->
                    val lesson = document.data
                    val title = lesson["title"] as? String ?: "درس جديد"
                    val description = lesson["description"] as? String ?: ""

                    Log.d("Notifications", "📖 عرض درس: $title")
                    showLessonNotification(title, description)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Notifications", "❌ فشل جلب الدروس: ${e.message}")
            }

        // الاستماع للتغييرات الجديدة فقط
        FirebaseFirestore.getInstance().collection("lessons")
            .whereEqualTo("className", studentGrade)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Notifications", "❌ خطأ في الاستماع: ${error.message}")
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val lesson = change.document.data
                        val title = lesson["title"] as? String ?: "درس جديد"
                        val description = lesson["description"] as? String ?: ""

                        Log.d("Notifications", "🎯 درس جديد مضاف: $title")
                        showLessonNotification(title, description)
                    }
                }
            }
    }

    private fun showLessonNotification(title: String, message: String) {
        // تأخير بسيط لضمان تحميل التطبيق بالكامل
        Handler(Looper.getMainLooper()).postDelayed({
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "lesson_channel",
                    "إشعارات الدروس",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(this, "lesson_channel")
                .setSmallIcon(R.drawable.alert)
                .setContentTitle("📚 درس جديد: $title")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, builder.build())

            Log.d("Notifications", "📢 تم عرض الإشعار برقم: $notificationId")

        }, 2000) // تأخير 2 ثانية
    }
}