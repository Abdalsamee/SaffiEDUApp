package com.example.saffieduapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.navigation.authNavGraph
import com.example.saffieduapp.presentation.screens.MainAppScreen
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.jvm.java

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var notificationListener: ChildEventListener

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startDailyLessonCheck()

        // تشغيل فحص اليوم مباشرة عند فتح التطبيق (اختياري)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            FirebaseFirestore.getInstance().collection("students")
                .document(it)
                .get()
                .addOnSuccessListener { studentDocument ->
                    val grade = studentDocument.getString("grade") ?: "الصف الرابع"
                    checkTodaysLessons(grade)
                }
        }
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

                // 2. الاستماع للدروس الجديدة لهذا الصف
                listenForScheduledLessons(studentGrade)

                // 3. الاستماع للإشعارات الفورية
                listenForInstantNotifications(studentGrade)
            }
            .addOnFailureListener {
                // إذا فشل جلب grade، حاول بالقيمة الافتراضية
                listenForScheduledLessons("الصف الرابع")
                listenForInstantNotifications("الصف الرابع")
            }
    }

    private fun listenForScheduledLessons(studentGrade: String) {
        // الاستماع للتغييرات الجديدة فقط (الدروس المضافة حديثاً)
        FirebaseFirestore.getInstance().collection("lessons")
            .whereEqualTo("className", studentGrade)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Notifications", "❌ خطأ في الاستماع للدروس: ${error.message}")
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val lesson = change.document.data
                        val title = lesson["title"] as? String ?: "درس جديد"
                        val description = lesson["description"] as? String ?: ""

                        Log.d("Notifications", "📚 درس جديد: $title")
                        showLessonNotification(title, description)
                    }
                }
            }
    }

    private fun listenForInstantNotifications(studentGrade: String) {
        val database = FirebaseDatabase.getInstance()
        val notificationsRef = database.getReference("instant_notifications")

        notificationListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val notification = snapshot.getValue(Map::class.java)
                val grade = notification?.get("grade") as? String ?: ""
                val title = notification?.get("title") as? String ?: "إشعار جديد"
                val message = notification?.get("message") as? String ?: ""
                val shouldNotify = notification?.get("shouldNotify") as? Boolean ?: true

                // التحقق من تطابق الصف وإذن الإشعار
                if (grade == studentGrade && shouldNotify) {
                    Log.d("InstantNotify", "🎯 إشعار فوري: $title")
                    showLessonNotification(title, message)

                    // حذف الإشعار بعد المعالجة لمنع التكرار
                    snapshot.ref.removeValue()
                } else {
                    Log.d(
                        "InstantNotify",
                        "⏸️ تم تجاهل الإشعار (عدم تطابق الصف أو shouldNotify = false)"
                    )
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // لا داعي للتعامل مع التغييرات
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // لا داعي للتعامل مع الحذف
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // لا داعي للتعامل مع الحركة
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InstantNotify", "❌ خطأ في الاستماع للإشعارات الفورية: ${error.message}")
            }
        }

        // بدء الاستماع للإشعارات الفورية
        notificationsRef.addChildEventListener(notificationListener)
    }

    private fun showLessonNotification(title: String, message: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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
                .setContentTitle("📚 $title")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, builder.build())

        }, 2000) // تأخير 2 ثانية لضمان تحميل التطبيق
    }

    override fun onDestroy() {
        super.onDestroy()
        // إيقاف الاستماع للإشعارات عند إغلاق التطبيق
        try {
            val database = FirebaseDatabase.getInstance()
            val notificationsRef = database.getReference("instant_notifications")
            notificationsRef.removeEventListener(notificationListener)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error removing listener: ${e.message}")
        }
    }

    private fun startDailyLessonCheck() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DailyLessonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // بدء الفحص فوراً ثم كل 30 ثانية
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(), // يبدأ فوراً
            30 * 1000, // كل 30 ثانية (30 * 1000 مللي ثانية)
            pendingIntent
        )

        Log.d("DailyCheck", "⏰ تم جدولة الفحص كل 30 ثانية")
    }

    // دالة للفحص اليومي للدروس
    fun checkTodaysLessons(studentGrade: String) {
        val todayDate = getTodayDateFormatted()

        FirebaseFirestore.getInstance().collection("lessons")
            .whereEqualTo("className", studentGrade)
            .whereEqualTo("publicationDate", todayDate)
            .whereEqualTo("isNotified", false) // فقط الدروس التي لم يُشعر بها
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val lesson = document.data
                    val title = lesson["title"] as? String ?: "درس جديد"
                    val description = lesson["description"] as? String ?: ""

                    Log.d("DailyCheck", "📚 درس اليوم: $title")
                    showLessonNotification(title, description)

                    // تحديث حالة الإشعار لمنع التكرار
                    document.reference.update("isNotified", true)
                }

                if (documents.isEmpty) {
                    Log.d("DailyCheck", "📅 لا يوجد دروس لليوم")
                }
            }
            .addOnFailureListener { e ->
                Log.e("DailyCheck", "❌ خطأ في فحص دروس اليوم: ${e.message}")
            }
    }

    // دالة للحصول على تاريخ اليوم بالتنسيق الصحيح
    private fun getTodayDateFormatted(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    // أضف هذه الدالة في MainActivity للاختبار
    private fun testDailyCheck() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DailyLessonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // تشغيل بعد 10 ثواني للاختبار
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 10000,
            pendingIntent
        )

        Log.d("Test", "⏰ سيتم التشغيل بعد 10 ثواني للاختبار")
    }
}
