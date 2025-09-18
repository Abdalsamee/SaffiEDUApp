package com.example.saffieduapp.data.FireBase

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.saffieduapp.data.FireBase.LessonRepository
import com.example.saffieduapp.presentation.screens.student.subject_details.components.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LessonPublishWorker(
    @ApplicationContext context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()
    private val lessonRepository = LessonRepository(
        storage = com.google.firebase.storage.FirebaseStorage.getInstance(),
        firestore = firestore
    )

    override suspend fun doWork(): Result {
        try {
            val lessonId = inputData.getString("lessonId") ?: return Result.failure()

            // جلب بيانات الدرس
            val lessonRef = firestore.collection("lessons").document(lessonId)
            val lessonSnapshot = lessonRef.get().await()
            val lesson = lessonSnapshot.toObject(Lesson::class.java) ?: return Result.failure()

            // إرسال إشعارات الطلاب بناءً على المادة والصف
            lessonRepository.sendNotificationToStudents(
                subjectId = lesson.subjectId ?: return Result.failure(),
                grade = lesson.className ?: return Result.failure(),
                title = lesson.title ?: "",
                description = lesson.description ?: ""
            )

            // تحديث حالة الدرس
            lessonRef.update(
                mapOf(
                    "notificationStatus" to "sent",
                    "notifiedAt" to System.currentTimeMillis()
                )
            ).await()

            return Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}
