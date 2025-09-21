package com.example.saffieduapp;

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent;
import android.os.Build
import android.util.Log;
import androidx.core.app.NotificationCompat

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyLessonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DailyCheck", "🔔 تم استدعاء الفحص اليومي")

        // استخدام Context مباشرة للوصول إلى Resources والخدمات
        checkTodaysLessons(context)
    }

    private fun checkTodaysLessons(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("students")
            .document(userId)
            .get()
            .addOnSuccessListener { studentDocument ->
                val studentGrade = studentDocument.getString("grade") ?: "الصف الرابع"
                val todayDate = getTodayDateFormatted()

                FirebaseFirestore.getInstance().collection("lessons")
                    .whereEqualTo("className", studentGrade)
                    .whereEqualTo("publicationDate", todayDate)
                    .whereEqualTo("isNotified", false)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val lesson = document.data
                            val title = lesson["title"] as? String ?: "درس جديد"
                            val description = lesson["description"] as? String ?: ""

                            showLessonNotification(context, title, description)
                            document.reference.update("isNotified", true)
                        }
                    }
            }
    }

    private fun showLessonNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "daily_lessons",
                "دروس اليوم",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, "daily_lessons")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // أو أي أيقونة أخرى
            .setContentTitle("📚 درس اليوم: $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun getTodayDateFormatted(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}