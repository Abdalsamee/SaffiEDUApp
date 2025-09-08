package com.example.saffieduapp.data.FireBase

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
fun scheduleDailyLessonNotification(context: Context) {
    val delay = calculateInitialDelay(8, 0)

    val workRequest = PeriodicWorkRequestBuilder<LessonNotificationWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS) // تمرير delay ووحدة الوقت
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "lesson_notifications",
        ExistingPeriodicWorkPolicy.REPLACE,
        workRequest
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateInitialDelay(hour: Int, minute: Int): Long {
    val now = LocalDateTime.now()
    var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

    if (target.isBefore(now)) {
        target = target.plusDays(1) // لو الوقت فات اليوم، يبدأ غداً
    }

    return Duration.between(now, target).toMillis()
}
