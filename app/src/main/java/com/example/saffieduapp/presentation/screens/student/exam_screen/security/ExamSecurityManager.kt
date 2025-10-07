package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * مدير الأمان المركزي للاختبار
 * ✅ مع نظام Whitelist للـ Dialogs الداخلية
 * ✅ إصلاح: Overlay Detection يعمل دائماً في الخلفية
 */
class ExamSecurityManager(
    private val context: Context,
    private val activity: Activity
) {
    private val TAG = "ExamSecurityManager"

    private val _violations = MutableStateFlow<List<SecurityViolation>>(emptyList())
    val violations: StateFlow<List<SecurityViolation>> = _violations.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _shouldShowWarning = MutableStateFlow(false)
    val shouldShowWarning: StateFlow<Boolean> = _shouldShowWarning.asStateFlow()

    private val _shouldAutoSubmit = MutableStateFlow(false)
    val shouldAutoSubmit: StateFlow<Boolean> = _shouldAutoSubmit.asStateFlow()

    private val _showExitWarning = MutableStateFlow(false)
    val showExitWarning: StateFlow<Boolean> = _showExitWarning.asStateFlow()

    private val _showNoFaceWarning = MutableStateFlow(false)
    val showNoFaceWarning: StateFlow<Boolean> = _showNoFaceWarning.asStateFlow()

    private val _showMultipleFacesWarning = MutableStateFlow(false)
    val showMultipleFacesWarning: StateFlow<Boolean> = _showMultipleFacesWarning.asStateFlow()

    private var appPausedTime: Long = 0
    private var totalTimeOutOfApp: Long = 0
    private var exitAttempts = 0
    private val maxExitAttempts = 2

    private var noFaceViolationCount = 0
    private val maxNoFaceWarnings = 2
    private val maxNoFaceBeforeTerminate = 5

    private var multipleFacesCount = 0
    private val maxMultipleFacesWarnings = 2

    private var examStarted = false

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    private var overlayDetector: OverlayDetector? = null
    private var cameraMonitor: CameraMonitor? = null

    // ✅ نظام Whitelist للـ Dialogs الداخلية
    @Volatile
    private var internalDialogActive = false

    // ✅ تتبع الـ Dialogs النشطة
    private val activeInternalDialogs = mutableSetOf<String>()

    // ✅ Handler للفحص الدوري
    private val periodicCheckHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var periodicCheckRunnable: Runnable? = null

    companion object {
        const val DIALOG_EXIT_WARNING = "EXIT_WARNING"
        const val DIALOG_NO_FACE_WARNING = "NO_FACE_WARNING"
        const val DIALOG_MULTIPLE_FACES = "MULTIPLE_FACES"
        const val DIALOG_EXIT_RETURN = "EXIT_RETURN"
        const val DIALOG_OVERLAY_DETECTED = "OVERLAY_DETECTED"
        const val DIALOG_SUBMIT_CONFIRM = "SUBMIT_CONFIRM"
    }

    /**
     * تفعيل جميع الميزات الأمنية
     */
    fun enableSecurityFeatures() {
        try {
            setupExternalDisplayMonitoring()
            setupOverlayDetection()
            Log.d(TAG, "Security features enabled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling security features", e)
        }
    }

    /**
     * إعداد كشف الـ Overlay
     */
    private fun setupOverlayDetection() {
        overlayDetector = OverlayDetector(activity) {
            // ✅ فحص إذا كان في dialog داخلي نشط
            if (!isInternalDialogActive()) {
                Log.e(TAG, "🚨 Real overlay detected!")
                logViolation("OVERLAY_DETECTED")
                handleCriticalViolation()
            } else {
                Log.d(TAG, "🟢 Overlay detected but internal dialog is active - IGNORED")
            }
        }

        // ✅ فحص دوري مع مراعاة الـ Dialogs الداخلية
        startOverlayPeriodicCheck()
    }

    /**
     * ✅ فحص دوري للكشف عن Overlays (مُصلح)
     * الفحص يعمل دائماً، لكن التسجيل يحدث فقط عندما لا يوجد dialog داخلي
     */
    private fun startOverlayPeriodicCheck() {
        periodicCheckRunnable = object : Runnable {
            override fun run() {
                if (examStarted && overlayDetector != null) {
                    // ✅ الفحص دائماً، لكن التسجيل مشروط
                    if (!activity.hasWindowFocus()) {
                        if (!isInternalDialogActive()) {
                            // ✅ overlay حقيقي!
                            Log.w(TAG, "⚠️ Periodic check: Lost window focus - possible overlay")
                            logViolation("OVERLAY_FOCUS_LOST")
                            handleCriticalViolation()
                            return // لا نعيد الجدولة بعد اكتشاف overlay
                        } else {
                            // ✅ dialog داخلي نشط، تجاهل
                            Log.d(TAG, "🟢 Periodic check: Focus lost but internal dialog active")
                        }
                    }

                    // ✅ إعادة الجدولة دائماً (خارج الـ if)
                    periodicCheckHandler.postDelayed(this, 3000)
                }
            }
        }

        // بدء الفحص الدوري
        periodicCheckHandler.postDelayed(periodicCheckRunnable!!, 3000)
        Log.d(TAG, "✅ Periodic overlay check started")
    }

    /**
     * ✅ إيقاف الفحص الدوري
     */
    private fun stopOverlayPeriodicCheck() {
        periodicCheckRunnable?.let {
            periodicCheckHandler.removeCallbacks(it)
            periodicCheckRunnable = null
            Log.d(TAG, "❌ Periodic overlay check stopped")
        }
    }

    /**
     * ✅ تسجيل dialog داخلي قبل إظهاره
     */
    fun registerInternalDialog(dialogName: String) {
        synchronized(activeInternalDialogs) {
            activeInternalDialogs.add(dialogName)
            internalDialogActive = true

            Log.d(TAG, "🟢 Internal Dialog Registered: $dialogName")
            Log.d(TAG, "📋 Active dialogs: ${activeInternalDialogs.joinToString()}")
        }
    }

    /**
     * ✅ إلغاء تسجيل dialog داخلي عند إغلاقه
     */
    fun unregisterInternalDialog(dialogName: String) {
        synchronized(activeInternalDialogs) {
            val wasRemoved = activeInternalDialogs.remove(dialogName)

            if (!wasRemoved) {
                Log.w(TAG, "⚠️ Tried to unregister dialog that wasn't registered: $dialogName")
                return
            }

            // ✅ إذا لم يعد هناك dialogs نشطة
            if (activeInternalDialogs.isEmpty()) {
                internalDialogActive = false
                Log.d(TAG, "🔴 All Internal Dialogs Closed - Detection Active")
            } else {
                Log.d(TAG, "🟡 Dialog Closed: $dialogName")
                Log.d(TAG, "📋 Remaining dialogs: ${activeInternalDialogs.joinToString()}")
            }
        }
    }

    /**
     * ✅ فحص إذا كان هناك dialog داخلي نشط
     */
    fun isInternalDialogActive(): Boolean {
        return synchronized(activeInternalDialogs) {
            internalDialogActive || activeInternalDialogs.isNotEmpty()
        }
    }

    /**
     * إعداد مراقبة الشاشات الخارجية
     */
    private fun setupExternalDisplayMonitoring() {
        try {
            displayManager.registerDisplayListener(
                object : DisplayManager.DisplayListener {
                    override fun onDisplayAdded(displayId: Int) {
                        if (displayId != 0) {
                            Log.e(TAG, "External display detected: $displayId")
                            logViolation("EXTERNAL_DISPLAY_CONNECTED")
                            handleCriticalViolation()
                        }
                    }

                    override fun onDisplayRemoved(displayId: Int) {}
                    override fun onDisplayChanged(displayId: Int) {}
                },
                null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up display monitoring", e)
        }
    }

    /**
     * معالجة المخالفات الحرجة
     */
    private fun handleCriticalViolation() {
        _shouldAutoSubmit.value = true
    }

    /**
     * ربط CameraMonitor مع SecurityManager
     */
    fun setCameraMonitor(monitor: CameraMonitor) {
        this.cameraMonitor = monitor
        monitor.getLastDetectionResult()
        Log.d(TAG, "Camera monitor linked")
    }

    /**
     * إعادة تعيين عداد الوجوه المتعددة عند اكتشاف وجه صحيح
     */
    fun resetMultipleFacesCount() {
        if (multipleFacesCount > 0) {
            Log.d(TAG, "Resetting multiple faces count (was: $multipleFacesCount)")
            multipleFacesCount = 0
        }
    }

    /**
     * بدء المراقبة
     */
    fun startMonitoring() {
        try {
            overlayDetector?.startMonitoring()
            Log.d(TAG, "✅ Monitoring started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting monitoring", e)
        }
    }

    /**
     * بدء الاختبار الفعلي
     */
    fun startExam() {
        examStarted = true
        Log.d(TAG, "✅ Exam officially started - exit tracking enabled")
    }

    /**
     * إيقاف المراقبة مؤقتاً
     */
    fun pauseMonitoring() {
        _isPaused.value = true
        // ✅ لا نوقف overlayDetector - فقط الكاميرا
        cameraMonitor?.pauseMonitoring()
        Log.d(TAG, "Monitoring paused (camera only)")
    }

    /**
     * استئناف المراقبة
     */
    fun resumeMonitoring() {
        _isPaused.value = false
        // ✅ لا نحتاج إعادة تشغيل overlayDetector - هو يعمل دائماً
        cameraMonitor?.resumeMonitoring()
        Log.d(TAG, "Monitoring resumed")
    }

    /**
     * إيقاف كامل للمراقبة
     */
    fun stopMonitoring() {
        stopOverlayPeriodicCheck()
        overlayDetector?.stopMonitoring()
        overlayDetector = null
        cameraMonitor?.cleanup()
        cameraMonitor = null
        Log.d(TAG, "Monitoring stopped and cleaned up")
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        stopMonitoring()
        activeInternalDialogs.clear()
        internalDialogActive = false
        Log.d(TAG, "Cleanup completed")
    }

    /**
     * معالجة توقف التطبيق
     */
    fun onAppPaused() {
        if (!examStarted) {
            Log.d(TAG, "App paused but exam not started yet - ignoring")
            return
        }

        appPausedTime = System.currentTimeMillis()
        pauseMonitoring()
        Log.w(TAG, "App paused during exam")
    }

    /**
     * معالجة استئناف التطبيق
     */
    fun onAppResumed() {
        if (!examStarted) {
            Log.d(TAG, "App resumed but exam not started yet - ignoring")
            return
        }

        if (appPausedTime > 0) {
            val timeOut = System.currentTimeMillis() - appPausedTime
            totalTimeOutOfApp += timeOut
            exitAttempts++

            logViolation("APP_RESUMED_AFTER_EXIT_$exitAttempts")

            Log.w(TAG, "App resumed after ${timeOut}ms - Exit attempt #$exitAttempts")

            when {
                exitAttempts > maxExitAttempts -> {
                    _shouldAutoSubmit.value = true
                    Log.e(TAG, "Auto-submit - max exit attempts")
                }
                else -> {
                    registerInternalDialog(DIALOG_EXIT_RETURN)
                    _showExitWarning.value = true
                    resumeMonitoring()
                }
            }

            appPausedTime = 0
        }
    }

    /**
     * معالجة فقدان التركيز على النافذة
     */
    fun onWindowFocusChanged(hasFocus: Boolean) {
        // ✅ تمرير للـ OverlayDetector بدون شروط
        // الـ OverlayDetector نفسه يتحقق من isMonitoring
        overlayDetector?.onWindowFocusChanged(hasFocus)

        if (!hasFocus && examStarted && !isInternalDialogActive()) {
            Log.w(TAG, "⚠️ Window focus lost during exam (no internal dialog)")
        }
    }

    /**
     * إخفاء التحذيرات
     */
    fun dismissWarning() {
        _shouldShowWarning.value = false
    }

    fun dismissExitWarning() {
        unregisterInternalDialog(DIALOG_EXIT_RETURN)
        _showExitWarning.value = false
    }

    fun dismissNoFaceWarning() {
        unregisterInternalDialog(DIALOG_NO_FACE_WARNING)
        _showNoFaceWarning.value = false
    }

    fun dismissMultipleFacesWarning() {
        unregisterInternalDialog(DIALOG_MULTIPLE_FACES)
        _showMultipleFacesWarning.value = false
    }

    /**
     * تسجيل مخالفة
     */
    fun logViolation(type: String) {
        val violation = SecurityViolation(
            type = type,
            timestamp = System.currentTimeMillis(),
            description = getViolationDescription(type),
            severity = getViolationSeverity(type)
        )

        _violations.value = _violations.value + violation
        Log.w(TAG, "⚠️ Violation logged: $type (${violation.severity})")

        when {
            type == "NO_FACE_DETECTED_LONG" -> handleNoFaceDetected()
            type == "MULTIPLE_FACES_DETECTED" -> handleMultipleFacesDetected()
        }
    }

    /**
     * معالجة عدم اكتشاف وجه
     */
    private fun handleNoFaceDetected() {
        noFaceViolationCount++
        Log.w(TAG, "No face violation #$noFaceViolationCount")

        when {
            noFaceViolationCount >= maxNoFaceBeforeTerminate -> {
                _shouldAutoSubmit.value = true
                _showNoFaceWarning.value = false
                Log.e(TAG, "🚨 Auto-submit triggered - max no face violations")
            }
            noFaceViolationCount > maxNoFaceWarnings -> {
                registerInternalDialog(DIALOG_NO_FACE_WARNING)
                _showNoFaceWarning.value = true
                pauseMonitoring()
                Log.w(TAG, "⚠️ No face warning shown - count: $noFaceViolationCount")
            }
        }
    }

    /**
     * معالجة اكتشاف أكثر من وجه
     */
    private fun handleMultipleFacesDetected() {
        multipleFacesCount++
        Log.w(TAG, "Multiple faces violation #$multipleFacesCount")

        when {
            multipleFacesCount > maxMultipleFacesWarnings -> {
                _shouldAutoSubmit.value = true
                _showMultipleFacesWarning.value = false
                Log.e(TAG, "🚨 Auto-submit triggered - multiple faces")
            }
            else -> {
                registerInternalDialog(DIALOG_MULTIPLE_FACES)
                _showMultipleFacesWarning.value = true
                pauseMonitoring()
                Log.w(TAG, "⚠️ Multiple faces warning shown")
            }
        }
    }

    /**
     * الحصول على عدد التحذيرات المتبقية
     */
    fun getRemainingAttempts(): Int = maxExitAttempts - exitAttempts
    fun getNoFaceViolationCount(): Int = noFaceViolationCount
    fun getRemainingNoFaceWarnings(): Int = maxNoFaceBeforeTerminate - noFaceViolationCount

    /**
     * إنشاء تقرير أمني
     */
    fun generateReport(): SecurityReport {
        return SecurityReport(
            violations = _violations.value,
            totalExitAttempts = exitAttempts,
            totalTimeOutOfApp = totalTimeOutOfApp,
            noFaceViolations = noFaceViolationCount
        )
    }

    /**
     * الحصول على وصف المخالفة
     */
    private fun getViolationDescription(type: String): String {
        return when {
            type.startsWith("OVERLAY_") -> "تم اكتشاف تطبيق يعمل فوق الاختبار"
            type.startsWith("NO_FACE") -> "لم يتم اكتشاف وجه الطالب"
            type.startsWith("APP_RESUMED") -> "خروج من التطبيق"
            type.startsWith("MULTIPLE_FACES") -> "تم اكتشاف أكثر من وجه"
            type.startsWith("MULTI_WINDOW") -> "تم اكتشاف وضع تقسيم الشاشة"
            type.startsWith("PIP_MODE") -> "تم اكتشاف وضع Picture-in-Picture"
            type.startsWith("EXTERNAL_DISPLAY") -> "تم اكتشاف شاشة خارجية"
            type.contains("BACK_BUTTON") -> "محاولة الضغط على زر الرجوع"
            type.contains("USER_LEFT") -> "مغادرة التطبيق"
            else -> "مخالفة أمنية"
        }
    }

    /**
     * تحديد شدة المخالفة
     */
    private fun getViolationSeverity(type: String): Severity {
        return when {
            type.contains("MULTI_WINDOW") -> Severity.CRITICAL
            type.contains("EXTERNAL_DISPLAY") -> Severity.CRITICAL
            type.contains("PIP_MODE") -> Severity.CRITICAL
            type.startsWith("OVERLAY_") -> Severity.CRITICAL
            type.contains("AUTO_SUBMIT") -> Severity.CRITICAL
            type.startsWith("MULTIPLE_FACES") -> Severity.HIGH
            type.startsWith("NO_FACE") -> Severity.MEDIUM
            type.contains("APP_RESUMED") -> Severity.MEDIUM
            else -> Severity.LOW
        }
    }
}

enum class Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class SecurityViolation(
    val type: String,
    val timestamp: Long,
    val description: String,
    val severity: Severity = Severity.LOW
)

data class SecurityReport(
    val violations: List<SecurityViolation>,
    val totalExitAttempts: Int,
    val totalTimeOutOfApp: Long,
    val noFaceViolations: Int
)