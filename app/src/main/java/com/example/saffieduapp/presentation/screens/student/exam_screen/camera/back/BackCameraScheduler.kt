package com.example.saffieduapp.presentation.screens.student.exam_screen.camera.back

import android.util.Log
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.CameraMonitoringConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * ⏰ مجدول التسجيل العشوائي للكاميرا الخلفية
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/back/BackCameraScheduler.kt
 *
 * 🎯 الهدف:
 * اختيار لحظة عشوائية واحدة فقط خلال الاختبار للتسجيل
 *
 * 📊 مثال:
 * اختبار 60 دقيقة:
 * - أقل وقت: 9 دقائق (15%)
 * - أكبر وقت: 51 دقيقة (85%)
 * - النتيجة: وقت عشوائي بينهم (مثلاً 23:45 أو 37:12)
 *
 * 🎲 عنصر المفاجأة = أمان أقوى!
 */
class BackCameraScheduler(
    private val examDurationMs: Long,
    private val onRecordingTime: () -> Unit
) {
    private val TAG = "BackCameraScheduler"

    // Coroutine Scope للعمل في الخلفية
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Job للتحكم في المهمة
    private var schedulerJob: Job? = null

    // وقت بداية الاختبار
    private var examStartTime: Long = 0

    // حالة المجدول
    private val _state = MutableStateFlow(SchedulerState())
    val state: StateFlow<SchedulerState> = _state.asStateFlow()

    // ═══════════════════════════════════════════
    // 📊 حالة المجدول
    // ═══════════════════════════════════════════

    data class SchedulerState(
        val isScheduled: Boolean = false,      // هل تمت الجدولة؟
        val scheduledTimeMs: Long = 0,         // الوقت المجدول (من بداية الاختبار)
        val scheduledAt: Long = 0,             // الوقت الفعلي (timestamp)
        val isWaiting: Boolean = false,        // هل في انتظار؟
        val isTriggered: Boolean = false,      // هل تم التفعيل؟
        val examStartTime: Long = 0            // وقت بداية الاختبار
    )

    // ═══════════════════════════════════════════
    // ▶️ البدء والإيقاف
    // ═══════════════════════════════════════════

    /**
     * بدء المجدول
     *
     * يتحقق من:
     * 1. أن التسجيل مفعّل
     * 2. أن الاختبار طويل كفاية (> 20 ثانية للاختبار)
     * 3. يحسب الوقت العشوائي
     * 4. يجدول التسجيل
     */
    fun start() {
        // 1️⃣ التحقق من أن التسجيل مفعّل
        if (!CameraMonitoringConfig.BackCamera.RECORDING_ENABLED) {
            Log.w(TAG, "⚠️ Back camera recording is disabled")
            return
        }

        // 2️⃣ التحقق من مدة الاختبار
        val minDuration = CameraMonitoringConfig.BackCamera.MIN_EXAM_DURATION_FOR_RECORDING
        if (examDurationMs < minDuration) {
            Log.w(TAG, "⚠️ Exam too short for recording!")
            Log.w(TAG, "   Exam duration: ${examDurationMs / 1000}s")
            Log.w(TAG, "   Min required: ${minDuration / 1000}s")
            return
        }

        // 3️⃣ حفظ وقت بداية الاختبار
        examStartTime = System.currentTimeMillis()

        // 4️⃣ جدولة التسجيل
        scheduleRecording()
    }

    /**
     * إيقاف المجدول
     */
    fun stop() {
        schedulerJob?.cancel()
        schedulerJob = null

        _state.value = SchedulerState()

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "🛑 Scheduler stopped")
        }
    }

    /**
     * إعادة الجدولة (في حالة الفشل)
     */
    fun reschedule() {
        Log.d(TAG, "🔄 Rescheduling...")
        stop()
        start()
    }

    // ═══════════════════════════════════════════
    // 🎲 منطق الجدولة
    // ═══════════════════════════════════════════

    /**
     * جدولة التسجيل في وقت عشوائي
     */
    private fun scheduleRecording() {
        // 1️⃣ حساب الوقت العشوائي
        val randomTime = calculateRandomRecordingTime()

        if (randomTime < 0) {
            Log.e(TAG, "❌ Failed to calculate random time")
            return
        }

        // 2️⃣ حساب الوقت الفعلي (timestamp)
        val scheduledTimestamp = examStartTime + randomTime

        // 3️⃣ تحديث الحالة
        _state.value = SchedulerState(
            isScheduled = true,
            scheduledTimeMs = randomTime,
            scheduledAt = scheduledTimestamp,
            isWaiting = true,
            examStartTime = examStartTime
        )

        // 4️⃣ طباعة معلومات الجدولة
        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            val minutes = randomTime / 60000
            val seconds = (randomTime % 60000) / 1000
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "⏰ Recording scheduled!")
            Log.d(TAG, "   Time from start: ${minutes}m ${seconds}s")
            Log.d(TAG, "   Timestamp: $scheduledTimestamp")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }

        // 5️⃣ بدء الانتظار
        startWaitingJob(randomTime)
    }

    /**
     * حساب الوقت العشوائي للتسجيل
     *
     * مثال: اختبار 60 دقيقة (3,600,000 ms)
     * - minTime = 3,600,000 × 0.15 = 540,000 ms (9 دقائق)
     * - maxTime = 3,600,000 × 0.85 = 3,060,000 ms (51 دقيقة)
     * - النتيجة = رقم عشوائي بين 540,000 و 3,060,000
     */
    private fun calculateRandomRecordingTime(): Long {
        val minTime = (examDurationMs * CameraMonitoringConfig.BackCamera.EARLIEST_RECORDING_PERCENT).toLong()
        val maxTime = (examDurationMs * CameraMonitoringConfig.BackCamera.LATEST_RECORDING_PERCENT).toLong()

        // التأكد من أن النطاق صحيح
        if (minTime >= maxTime) {
            Log.e(TAG, "❌ Invalid time range: min=$minTime, max=$maxTime")
            return -1
        }

        // توليد رقم عشوائي
        return Random.nextLong(minTime, maxTime)
    }

    /**
     * بدء job الانتظار
     *
     * ينتظر المدة المحددة ثم يفعّل التسجيل
     */
    private fun startWaitingJob(delayMs: Long) {
        schedulerJob = scope.launch {
            try {
                // الانتظار حتى الوقت المحدد
                delay(delayMs)

                // التحقق من أن المجدول لم يُلغى
                if (isActive) {
                    triggerRecording()
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "⚠️ Scheduler job cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in scheduler job", e)
            }
        }
    }

    /**
     * تفعيل التسجيل
     *
     * يتم استدعاؤها عندما يحين الوقت
     */
    private fun triggerRecording() {
        _state.value = _state.value.copy(
            isWaiting = false,
            isTriggered = true
        )

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "🎬 ⏰ RECORDING TIME REACHED!")
            Log.d(TAG, "   Triggering recording now...")
        }

        // استدعاء الـ callback
        onRecordingTime()
    }

    // ═══════════════════════════════════════════
    // ℹ️ معلومات مساعدة
    // ═══════════════════════════════════════════

    /**
     * الحصول على الوقت المتبقي حتى التسجيل
     *
     * @return الوقت المتبقي بالميلي ثانية
     */
    fun getRemainingTimeMs(): Long {
        if (!_state.value.isScheduled || _state.value.isTriggered) {
            return 0
        }

        val currentTime = System.currentTimeMillis()
        val remainingTime = _state.value.scheduledAt - currentTime

        return if (remainingTime > 0) remainingTime else 0
    }

    /**
     * التحقق من أن الوقت قد حان
     */
    fun isTimeReached(): Boolean {
        if (!_state.value.isScheduled || _state.value.isTriggered) {
            return false
        }

        return System.currentTimeMillis() >= _state.value.scheduledAt
    }

    /**
     * معلومات الجدولة بصيغة قابلة للقراءة
     */
    fun getScheduleInfo(): ScheduleInfo {
        val state = _state.value
        val remainingMs = getRemainingTimeMs()

        return ScheduleInfo(
            isScheduled = state.isScheduled,
            scheduledTimeFromStart = formatDuration(state.scheduledTimeMs),
            remainingTime = formatDuration(remainingMs),
            isWaiting = state.isWaiting,
            isTriggered = state.isTriggered,
            examStartTime = state.examStartTime,
            scheduledTimestamp = state.scheduledAt
        )
    }

    /**
     * تنسيق المدة الزمنية
     *
     * مثال: 125000 ms → "2m 5s"
     */
    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "${minutes}m ${seconds}s"
    }

    /**
     * طباعة معلومات الجدولة
     */
    fun printScheduleInfo() {
        val info = getScheduleInfo()
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📊 Schedule Info:")
        Log.d(TAG, "   Scheduled: ${info.isScheduled}")
        Log.d(TAG, "   Time from start: ${info.scheduledTimeFromStart}")
        Log.d(TAG, "   Remaining: ${info.remainingTime}")
        Log.d(TAG, "   Waiting: ${info.isWaiting}")
        Log.d(TAG, "   Triggered: ${info.isTriggered}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    // ═══════════════════════════════════════════
    // 🧹 تنظيف الموارد
    // ═══════════════════════════════════════════

    /**
     * تنظيف وإيقاف كل شيء
     */
    fun cleanup() {
        stop()
        scope.cancel()

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "🧹 Scheduler cleaned up")
        }
    }
}

// ═══════════════════════════════════════════
// 📊 معلومات الجدولة
// ═══════════════════════════════════════════

/**
 * معلومات الجدولة بصيغة منظمة
 */
data class ScheduleInfo(
    val isScheduled: Boolean,
    val scheduledTimeFromStart: String,
    val remainingTime: String,
    val isWaiting: Boolean,
    val isTriggered: Boolean,
    val examStartTime: Long,
    val scheduledTimestamp: Long
)