package com.example.saffieduapp.data.FireBase.WorkManager

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import java.util.Date

object AlertScheduler {
    fun scheduleAlert(context: Context, alertId: String, title: String, message: String, triggerTime: Date) {
        val delay = triggerTime.time - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .build()

        val workManager = WorkManager.getInstance(context)

        // أولاً نلغي أي Work سابق بنفس الـ alertId
        workManager.cancelAllWorkByTag(alertId)

        // بعدها نضيف الجديد
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(alertId) // مهم جداً
            .build()

        workManager.enqueue(workRequest)
    }
}
