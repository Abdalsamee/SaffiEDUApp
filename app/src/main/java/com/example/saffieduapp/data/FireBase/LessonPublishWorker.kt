package com.example.saffieduapp.data.FireBase

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.saffieduapp.presentation.screens.student.subject_details.components.Lesson
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await

class LessonPublishWorker(
    @ApplicationContext context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()


    override suspend fun doWork(): Result {
        try {
            Log.d("NotificationDebug", "بدأ العمل المجدول")
            val lessonId = inputData.getString("lessonId") ?: return Result.failure()
            Log.d("NotificationDebug", "معرف الدرس: $lessonId")

            val lessonSnapshot = firestore.collection("lessons").document(lessonId).get().await()
            val lessonData = lessonSnapshot.data ?: return Result.failure()

            Log.d("NotificationDebug", "بيانات الدرس: $lessonData")

            // استخراج البيانات مع معالجة الاختلافات
            val className = lessonData["className"] as? String ?: return Result.failure()
            val title = lessonData["title"] as? String ?: ""
            val description = lessonData["description"] as? String ?: ""

            // معالجة تاريخ النشر - قد يكون بتنسيق مختلف
            val publicationDate = (lessonData["publicationDate"] as? String) ?: ""

            // معالجة teacherId - قد يكون رقمي أو نصي
            val teacherId = lessonData["teacherId"]?.toString() ?: ""


            // تحديث حالة الدرس
            firestore.collection("lessons").document(lessonId).update(
                "notificationStatus", "scheduled",
                "scheduledAt", System.currentTimeMillis()
            ).await()

            Log.d("LessonPublishWorker", "تم جدولة الإشعار بنجاح")
            return Result.success()

        } catch (e: Exception) {
            Log.e("LessonPublishWorker", "فشل جدولة الإشعار", e)
            return Result.retry()
        }
    }
}