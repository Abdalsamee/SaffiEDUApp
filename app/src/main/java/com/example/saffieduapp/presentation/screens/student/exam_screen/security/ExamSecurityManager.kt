package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * مدير الأمان المركزي للاختبار
 */
class ExamSecurityManager(private val context: Context) {

    private val _violations = MutableStateFlow<List<SecurityViolation>>(emptyList())
    val violations = _violations.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    private var appPausedTime: Long = 0
    private var totalTimeOutOfApp: Long = 0
    private var exitAttempts = 0

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    /**
     * تفعيل جميع ميزات الحماية
     */
    fun enableSecurityFeatures() {
        setupExternalDisplayMonitoring()
    }

    /**
     * بدء المراقبة
     */
    fun startMonitoring() {
        // TODO: بدء Face Detection
        // TODO: جدولة اللقطات العشوائية
    }

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() {
        // TODO: إيقاف الكاميرا
        // TODO: إلغاء الجداول
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
            "MULTI_WINDOW_DETECTED" -> Severity.CRITICAL

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
        // TODO: إظهار Dialog تحذيري
        // TODO: إرسال تنبيه فوري للمعلم
    }

    /**
     * معالجة مخالفة عالية
     */
    private fun handleHighViolation() {
        exitAttempts++

        if (exitAttempts >= 3) {
            // إنهاء تلقائي بعد 3 محاولات
            // TODO: submitExamAutomatically()
        }
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

            logViolation("APP_RESUMED_AFTER_${duration}ms")
            appPausedTime = 0
        }
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