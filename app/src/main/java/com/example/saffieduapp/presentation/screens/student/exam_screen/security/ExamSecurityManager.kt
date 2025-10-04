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

    private var appPausedTime: Long = 0
    private var totalTimeOutOfApp: Long = 0
    private var exitAttempts = 0
    private val maxExitAttempts = 2 // بعد محاولتين يُنهى الاختبار

    // ✅ تتبع مخالفات عدم ظهور الوجه
    private var noFaceViolationCount = 0
    private val maxNoFaceWarnings = 2 // تحذيرين
    private val maxNoFaceBeforeTerminate = 5 // 5 مرات → إنهاء

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    // ✅ Overlay Detector
    private var overlayDetector: OverlayDetector? = null

    // ✅ Camera Monitor
    private var cameraMonitor: CameraMonitor? = null

    /**
     * تعيين Camera Monitor
     */
    fun setCameraMonitor(monitor: CameraMonitor) {
        this.cameraMonitor = monitor
        Log.d(TAG, "Camera monitor set successfully")
    }

    /**
     * تفعيل جميع ميزات الحماية
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
     * بدء المراقبة
     */
    fun startMonitoring() {
        try {
            // بدء مراقبة Overlays
            overlayDetector?.startMonitoring()
            Log.d(TAG, "Monitoring started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting monitoring", e)
        }
    }

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() {
        try {
            overlayDetector?.stopMonitoring()
            cameraMonitor?.stopMonitoring()
            Log.d(TAG, "Monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping monitoring", e)
        }
    }

    /**
     * إيقاف مؤقت للمراقبة
     */
    fun pauseMonitoring() {
        try {
            cameraMonitor?.pauseMonitoring()
            Log.d(TAG, "Monitoring paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing monitoring", e)
        }
    }

    /**
     * استئناف المراقبة
     */
    fun resumeMonitoring() {
        try {
            cameraMonitor?.resumeMonitoring()
            Log.d(TAG, "Monitoring resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming monitoring", e)
        }
    }

    /**
     * ✅ إعداد كشف Overlays
     */
    private fun setupOverlayDetection() {
        overlayDetector = OverlayDetector(activity) {
            // عند اكتشاف Overlay
            logViolation("OVERLAY_DETECTED")
            handleCriticalViolation()
        }
    }

    /**
     * تمرير Window Focus Changes للـ OverlayDetector
     */
    fun onWindowFocusChanged(hasFocus: Boolean) {
        overlayDetector?.onWindowFocusChanged(hasFocus)
    }

    /**
     * مراقبة الشاشات الخارجية
     */
    private fun setupExternalDisplayMonitoring() {
        try {
            displayManager.registerDisplayListener(
                object : DisplayManager.DisplayListener {
                    override fun onDisplayAdded(displayId: Int) {
                        logViolation("EXTERNAL_DISPLAY_CONNECTED")
                        handleCriticalViolation()
                    }

                    override fun onDisplayRemoved(displayId: Int) {
                        // تسجيل فقط
                        Log.d(TAG, "External display removed: $displayId")
                    }

                    override fun onDisplayChanged(displayId: Int) {
                        // تسجيل فقط
                        Log.d(TAG, "Display changed: $displayId")
                    }
                },
                null
            )

            // فحص الشاشات الحالية
            if (displayManager.displays.size > 1) {
                logViolation("EXTERNAL_DISPLAY_ALREADY_CONNECTED")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up external display monitoring", e)
        }
    }

    /**
     * تسجيل مخالفة أمنية
     */
    fun logViolation(type: String) {
        try {
            Log.w(TAG, "🚨 Violation logged: $type") // ✅ Log واضح

            val violation = SecurityViolation(
                type = type,
                timestamp = System.currentTimeMillis(),
                severity = calculateSeverity(type)
            )

            _violations.value = _violations.value + violation
            Log.w(TAG, "Total violations: ${_violations.value.size}, Severity: ${violation.severity}")

            // اتخاذ إجراء حسب الشدة
            when (violation.severity) {
                Severity.CRITICAL -> {
                    Log.e(TAG, "⚠️ CRITICAL violation - calling handleCriticalViolation()")
                    handleCriticalViolation()
                }
                Severity.HIGH -> {
                    Log.w(TAG, "⚠️ HIGH violation - calling handleHighViolation()")
                    handleHighViolation()
                }
                Severity.MEDIUM -> {
                    Log.i(TAG, "ℹ️ MEDIUM violation - calling handleMediumViolation()")
                    handleMediumViolation()
                }
                Severity.LOW -> {
                    Log.i(TAG, "ℹ️ LOW violation - calling handleLowViolation()")
                    handleLowViolation()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging violation", e)
        }
    }

    /**
     * حساب شدة المخالفة
     */
    private fun calculateSeverity(type: String): Severity {
        return when (type) {
            "EXTERNAL_DISPLAY_CONNECTED",
            "EXTERNAL_DISPLAY_ALREADY_CONNECTED",
            "MULTI_WINDOW_DETECTED",
            "MULTI_WINDOW_ON_RESUME",
            "MULTI_WINDOW_CONFIG_CHANGE",
            "OVERLAY_DETECTED",
            "PIP_MODE_DETECTED" -> Severity.CRITICAL

            "USER_LEFT_APP",
            "MULTIPLE_FACES_DETECTED",
            "NO_FACE_DETECTED_LONG" -> Severity.HIGH

            "BACK_BUTTON_PRESSED",
            "LOOKING_AWAY",
            "USER_FORCED_EXIT",
            "WINDOW_FOCUS_LOST" -> Severity.MEDIUM

            else -> Severity.LOW
        }
    }

    /**
     * معالجة مخالفة حرجة
     */
    private fun handleCriticalViolation() {
        try {
            pauseExam()
            pauseMonitoring()
            _shouldAutoSubmit.value = true
            Log.e(TAG, "Critical violation - Auto submit triggered")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling critical violation", e)
        }
    }

    /**
     * معالجة مخالفة عالية
     */
    private fun handleHighViolation() {
        try {
            val lastViolation = _violations.value.lastOrNull()

            when (lastViolation?.type) {
                "NO_FACE_DETECTED_LONG" -> {
                    noFaceViolationCount++
                    Log.w(TAG, "No face violation count: $noFaceViolationCount")

                    when {
                        // المرة 1-2: تحذير فقط
                        noFaceViolationCount <= maxNoFaceWarnings -> {
                            _shouldShowWarning.value = true
                            Log.w(TAG, "Showing no-face warning")
                        }

                        // المرة 3-4: إيقاف مؤقت
                        noFaceViolationCount <= maxNoFaceBeforeTerminate -> {
                            pauseExam()
                            pauseMonitoring()
                            _shouldShowWarning.value = true
                            Log.w(TAG, "Exam paused - no face detected")
                        }

                        // المرة 5+: إنهاء
                        else -> {
                            _shouldAutoSubmit.value = true
                            Log.e(TAG, "Auto-submit triggered - too many no-face violations")
                        }
                    }
                }

                "MULTIPLE_FACES_DETECTED" -> {
                    // أكثر من وجه → إنهاء فوري
                    pauseExam()
                    _shouldAutoSubmit.value = true
                    Log.e(TAG, "Auto-submit triggered - multiple faces")
                }

                "USER_LEFT_APP" -> {
                    // تسجيل فقط - سيتم المعالجة في onAppResumed
                    Log.w(TAG, "User left app")
                }

                else -> {
                    Log.w(TAG, "High violation: ${lastViolation?.type}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling high violation", e)
        }
    }

    /**
     * معالجة مخالفة متوسطة
     */
    private fun handleMediumViolation() {
        // تسجيل فقط
        Log.w(TAG, "Medium violation detected")
    }

    /**
     * معالجة مخالفة منخفضة
     */
    private fun handleLowViolation() {
        // تسجيل فقط
        Log.i(TAG, "Low violation detected")
    }

    /**
     * إيقاف الاختبار مؤقتاً
     */
    fun pauseExam() {
        _isPaused.value = true
        Log.d(TAG, "Exam paused")
    }

    /**
     * استئناف الاختبار
     */
    fun resumeExam() {
        _isPaused.value = false
        resumeMonitoring()
        Log.d(TAG, "Exam resumed")
    }

    /**
     * إعادة تعيين عداد مخالفات عدم ظهور الوجه
     * يتم استدعاؤها عندما يظهر الوجه مرة أخرى بعد فترة طويلة من الغياب
     */
    fun resetNoFaceViolations() {
        if (noFaceViolationCount > 0) {
            Log.d(TAG, "Resetting no-face violations (was: $noFaceViolationCount)")
            noFaceViolationCount = 0
        }
    }

    /**
     * عند إيقاف التطبيق
     */
    fun onAppPaused() {
        appPausedTime = System.currentTimeMillis()
        pauseMonitoring()
        Log.d(TAG, "App paused at: $appPausedTime")
    }

    /**
     * عند استئناف التطبيق
     */
    fun onAppResumed() {
        if (appPausedTime > 0) {
            val duration = System.currentTimeMillis() - appPausedTime
            totalTimeOutOfApp += duration

            exitAttempts++
            Log.d(TAG, "App resumed after ${duration}ms, total attempts: $exitAttempts")

            logViolation("APP_RESUMED_AFTER_${duration}ms")

            // تحديد الإجراء حسب عدد المحاولات
            when {
                exitAttempts > maxExitAttempts -> {
                    // إنهاء تلقائي
                    _shouldAutoSubmit.value = true
                    Log.e(TAG, "Max exit attempts exceeded - Auto submit")
                }
                else -> {
                    // إظهار تحذير
                    _shouldShowWarning.value = true
                    // استئناف المراقبة
                    resumeMonitoring()
                }
            }

            appPausedTime = 0
        }
    }

    /**
     * إخفاء التحذير بعد قراءته
     */
    fun dismissWarning() {
        _shouldShowWarning.value = false
        Log.d(TAG, "Warning dismissed")
    }

    /**
     * الحصول على عدد المحاولات المتبقية
     */
    fun getRemainingAttempts(): Int {
        return (maxExitAttempts - exitAttempts).coerceAtLeast(0)
    }

    /**
     * إنشاء التقرير الأمني النهائي
     */
    fun generateReport(): ExamSecurityReport {
        val report = ExamSecurityReport(
            violations = _violations.value,
            totalExitAttempts = exitAttempts,
            totalTimeOutOfApp = totalTimeOutOfApp,
            securityScore = calculateSecurityScore(),
            timestamp = System.currentTimeMillis()
        )
        Log.d(TAG, "Security report generated: Score=${report.securityScore}, Violations=${report.violations.size}")
        return report
    }

    /**
     * حساب درجة الأمان (0-100)
     */
    private fun calculateSecurityScore(): Int {
        var score = 100

        _violations.value.forEach { violation ->
            score -= when (violation.severity) {
                Severity.CRITICAL -> 30
                Severity.HIGH -> 15
                Severity.MEDIUM -> 5
                Severity.LOW -> 2
            }
        }

        return score.coerceAtLeast(0).coerceAtMost(100)
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        try {
            stopMonitoring()
            overlayDetector = null
            cameraMonitor = null
            Log.d(TAG, "Security manager cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up security manager", e)
        }
    }
}

/**
 * مخالفة أمنية
 */
data class SecurityViolation(
    val type: String,
    val timestamp: Long,
    val severity: Severity,
    val details: String = ""
)

/**
 * شدة المخالفة
 */
enum class Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * التقرير الأمني
 */
data class ExamSecurityReport(
    val violations: List<SecurityViolation>,
    val totalExitAttempts: Int,
    val totalTimeOutOfApp: Long,
    val securityScore: Int,
    val timestamp: Long
)