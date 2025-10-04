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

    private var appPausedTime: Long = 0
    private var totalTimeOutOfApp: Long = 0
    private var exitAttempts = 0
    private val maxExitAttempts = 2

    private var noFaceViolationCount = 0
    private val maxNoFaceWarnings = 2
    private val maxNoFaceBeforeTerminate = 5

    // ✅ للتمييز بين pause/resume الطبيعي والخروج الفعلي
    private var examStarted = false

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    // ✅ Overlay Detector
    private var overlayDetector: OverlayDetector? = null

    // ✅ Camera Monitor
    private var cameraMonitor: CameraMonitor? = null

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
            logViolation("OVERLAY_DETECTED")
            handleCriticalViolation()
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
        Log.d(TAG, "Camera monitor linked")
    }

    /**
     * بدء المراقبة
     */
    fun startMonitoring() {
        try {
            overlayDetector?.startMonitoring()
            Log.d(TAG, "Monitoring started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting monitoring", e)
        }
    }

    /**
     * ✅ بدء الاختبار الفعلي - يتم استدعاؤها عند دخول شاشة الاختبار
     */
    fun startExam() {
        examStarted = true
        Log.d(TAG, "Exam officially started - exit tracking enabled")
    }

    /**
     * إيقاف المراقبة مؤقتاً
     */
    fun pauseMonitoring() {
        _isPaused.value = true
        overlayDetector?.stopMonitoring()
        cameraMonitor?.pauseMonitoring()
        Log.d(TAG, "Monitoring paused")
    }

    /**
     * استئناف المراقبة
     */
    fun resumeMonitoring() {
        _isPaused.value = false
        overlayDetector?.startMonitoring()
        cameraMonitor?.resumeMonitoring()
        Log.d(TAG, "Monitoring resumed")
    }

    /**
     * إيقاف كامل للمراقبة
     */
    fun stopMonitoring() {
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
        _violations.value = emptyList()
        exitAttempts = 0
        noFaceViolationCount = 0
        examStarted = false
        Log.d(TAG, "Cleanup completed")
    }

    /**
     * ربط CameraMonitor مع SecurityManager
     */
    fun setCameraMonitor(monitor: CameraMonitor) {
        this.cameraMonitor = monitor
        Log.d(TAG, "Camera monitor linked")
    }

    /**
     * معالجة مخالفة عدم وجود وجه
     */
    fun handleNoFaceDetected() {
        noFaceViolationCount++
        Log.w(TAG, "No face violation #$noFaceViolationCount")

        when {
            noFaceViolationCount > maxNoFaceBeforeTerminate -> {
                _shouldAutoSubmit.value = true
                logViolation("NO_FACE_AUTO_SUBMIT")
            }
            noFaceViolationCount > maxNoFaceWarnings -> {
                _showNoFaceWarning.value = true
                logViolation("NO_FACE_WARNING")
            }
            else -> {
                logViolation("NO_FACE_DETECTED")
            }
        }
    }

    /**
     * إعادة تعيين عداد عدم وجود وجه
     */
    fun resetNoFaceCount() {
        noFaceViolationCount = 0
    }

    /**
     * الحصول على عدد مخالفات عدم ظهور الوجه
     */
    fun getNoFaceViolationCount(): Int = noFaceViolationCount

    /**
     * الحصول على التحذيرات المتبقية قبل الإنهاء التلقائي
     */
    fun getRemainingNoFaceWarnings(): Int = maxNoFaceBeforeTerminate - noFaceViolationCount

    /**
     * الحصول على عدد المحاولات المتبقية للخروج
     */
    fun getRemainingAttempts(): Int = maxExitAttempts - exitAttempts

    /**
     * إخفاء تحذير عدم ظهور الوجه
     */
    fun dismissNoFaceWarning() {
        _showNoFaceWarning.value = false
    }

    /**
     * إخفاء تحذير الخروج
     */
    fun dismissExitWarning() {
        _showExitWarning.value = false
    }

    /**
     * الحصول على تقرير الأمان
     */
    fun getSecurityReport(): SecurityReport {
        return SecurityReport(
            violations = _violations.value,
            totalExitAttempts = exitAttempts,
            totalTimeOutOfApp = totalTimeOutOfApp,
            noFaceViolations = noFaceViolationCount
        )
    }

    /**
     * إنشاء تقرير الأمان النهائي
     */
    fun generateReport(): String {
        val report = getSecurityReport()
        return """
            |=== Security Report ===
            |Total Violations: ${report.violations.size}
            |Exit Attempts: ${report.totalExitAttempts}
            |Time Out of App: ${report.totalTimeOutOfApp}ms
            |No Face Violations: ${report.noFaceViolations}
            |
            |Violations:
            |${report.violations.joinToString("\n") { "- ${it.type}: ${it.description} at ${it.timestamp}" }}
            |======================
        """.trimMargin()
    }

    /**
     * معالجة خروج التطبيق للخلفية
     */
    fun onAppPaused() {
        if (examStarted) {
            appPausedTime = System.currentTimeMillis()
            pauseMonitoring()
            Log.d(TAG, "App paused - exam is active")
        }
    }

    /**
     * معالجة عودة التطبيق من الخلفية
     */
    fun onAppResumed() {
        // ✅ تجاهل resume إذا لم يبدأ الاختبار بعد
        if (!examStarted) {
            Log.d(TAG, "App resumed but exam not started yet - ignoring")
            appPausedTime = 0
            return
        }

        if (appPausedTime > 0) {
            val duration = System.currentTimeMillis() - appPausedTime
            totalTimeOutOfApp += duration
            exitAttempts++

            Log.d(TAG, "App resumed - Exit attempt #$exitAttempts (duration: ${duration}ms)")
            logViolation("APP_RESUMED_AFTER_${duration}ms")

            when {
                exitAttempts > maxExitAttempts -> {
                    _shouldAutoSubmit.value = true
                    Log.e(TAG, "Auto-submit - max exit attempts")
                }
                else -> {
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
        if (!hasFocus && examStarted) {
            Log.w(TAG, "Window focus lost during exam")
        }
    }

    /**
     * إخفاء التحذيرات
     */
    fun dismissWarning() {
        _shouldShowWarning.value = false
        _showExitWarning.value = false
        _showNoFaceWarning.value = false
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
        Log.w(TAG, "Violation logged: $type (${violation.severity})")
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

/**
 * مستويات شدة المخالفة
 */
enum class Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * بيانات المخالفة الأمنية
 */
data class SecurityViolation(
    val type: String,
    val timestamp: Long,
    val description: String,
    val severity: Severity = Severity.LOW
)

/**
 * تقرير الأمان الكامل
 */
data class SecurityReport(
    val violations: List<SecurityViolation>,
    val totalExitAttempts: Int,
    val totalTimeOutOfApp: Long,
    val noFaceViolations: Int
)