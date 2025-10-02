package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
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

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    // ✅ Overlay Detector
    private var overlayDetector: OverlayDetector? = null

    /**
     * تفعيل جميع ميزات الحماية
     */
    fun enableSecurityFeatures() {
        setupExternalDisplayMonitoring()
        setupOverlayDetection()
    }

    /**
     * بدء المراقبة
     */
    fun startMonitoring() {
        // بدء مراقبة Overlays
        overlayDetector?.startMonitoring()
    }

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() {
        overlayDetector?.stopMonitoring()
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
        displayManager.registerDisplayListener(
            object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {
                    logViolation("EXTERNAL_DISPLAY_CONNECTED")
                    handleCriticalViolation()
                }

                override fun onDisplayRemoved(displayId: Int) {
                    // تسجيل فقط
                }

                override fun onDisplayChanged(displayId: Int) {
                    // تسجيل فقط
                }
            },
            null
        )

        // فحص الشاشات الحالية
        if (displayManager.displays.size > 1) {
            logViolation("EXTERNAL_DISPLAY_ALREADY_CONNECTED")
        }
    }

    /**
     * تسجيل مخالفة أمنية
     */
    fun logViolation(type: String) {
        val violation = SecurityViolation(
            type = type,
            timestamp = System.currentTimeMillis(),
            severity = calculateSeverity(type)
        )

        _violations.value = _violations.value + violation

        // اتخاذ إجراء حسب الشدة
        when (violation.severity) {
            Severity.CRITICAL -> handleCriticalViolation()
            Severity.HIGH -> handleHighViolation()
            Severity.MEDIUM -> handleMediumViolation()
            Severity.LOW -> handleLowViolation()
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
            "PIP_MODE_DETECTED" -> Severity.CRITICAL // ✅ جميع هذه المخالفات خطيرة

            "USER_LEFT_APP",
            "MULTIPLE_FACES_DETECTED",
            "NO_FACE_DETECTED_LONG" -> Severity.HIGH

            "BACK_BUTTON_PRESSED",
            "LOOKING_AWAY",
            "USER_FORCED_EXIT" -> Severity.MEDIUM

            else -> Severity.LOW
        }
    }

    /**
     * معالجة مخالفة حرجة
     */
    private fun handleCriticalViolation() {
        pauseExam()
        // إنهاء فوري للاختبار في حالة Overlay
        _shouldAutoSubmit.value = true
    }

    /**
     * معالجة مخالفة عالية
     */
    private fun handleHighViolation() {
        // تسجيل فقط - العقوبة ستكون في onAppResumed
    }

    /**
     * معالجة مخالفة متوسطة
     */
    private fun handleMediumViolation() {
        // تسجيل فقط
    }

    /**
     * معالجة مخالفة منخفضة
     */
    private fun handleLowViolation() {
        // تسجيل فقط
    }

    /**
     * إيقاف الاختبار مؤقتاً
     */
    fun pauseExam() {
        _isPaused.value = true
    }

    /**
     * استئناف الاختبار
     */
    fun resumeExam() {
        _isPaused.value = false
    }

    /**
     * عند إيقاف التطبيق
     */
    fun onAppPaused() {
        appPausedTime = System.currentTimeMillis()
    }

    /**
     * عند استئناف التطبيق
     */
    fun onAppResumed() {
        if (appPausedTime > 0) {
            val duration = System.currentTimeMillis() - appPausedTime
            totalTimeOutOfApp += duration

            exitAttempts++

            logViolation("APP_RESUMED_AFTER_${duration}ms")

            // تحديد الإجراء حسب عدد المحاولات
            when {
                exitAttempts > maxExitAttempts -> {
                    // إنهاء تلقائي
                    _shouldAutoSubmit.value = true
                }
                else -> {
                    // إظهار تحذير
                    _shouldShowWarning.value = true
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
        return ExamSecurityReport(
            violations = _violations.value,
            totalExitAttempts = exitAttempts,
            totalTimeOutOfApp = totalTimeOutOfApp,
            securityScore = calculateSecurityScore(),
            timestamp = System.currentTimeMillis()
        )
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

        return score.coerceIn(0, 100)
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