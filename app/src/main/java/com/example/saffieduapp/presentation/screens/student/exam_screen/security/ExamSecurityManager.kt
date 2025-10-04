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

    // ✅ تدفقات منفصلة لكل نوع تحذير
    private val _showNoFaceWarning = MutableStateFlow(false)
    val showNoFaceWarning: StateFlow<Boolean> = _showNoFaceWarning.asStateFlow()

    private val _showExitWarning = MutableStateFlow(false)
    val showExitWarning: StateFlow<Boolean> = _showExitWarning.asStateFlow()

    private val _shouldAutoSubmit = MutableStateFlow(false)
    val shouldAutoSubmit: StateFlow<Boolean> = _shouldAutoSubmit.asStateFlow()

    private var appPausedTime: Long = 0
    private var totalTimeOutOfApp: Long = 0
    private var exitAttempts = 0
    private val maxExitAttempts = 2

    private var noFaceViolationCount = 0
    private val maxNoFaceWarnings = 2
    private val maxNoFaceBeforeTerminate = 5

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    private var overlayDetector: OverlayDetector? = null
    private var cameraMonitor: CameraMonitor? = null

    fun setCameraMonitor(monitor: CameraMonitor) {
        this.cameraMonitor = monitor
        Log.d(TAG, "Camera monitor set successfully")
    }

    fun enableSecurityFeatures() {
        try {
            setupExternalDisplayMonitoring()
            setupOverlayDetection()
            Log.d(TAG, "Security features enabled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling security features", e)
        }
    }

    fun startMonitoring() {
        try {
            overlayDetector?.startMonitoring()
            Log.d(TAG, "Monitoring started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting monitoring", e)
        }
    }

    fun stopMonitoring() {
        try {
            overlayDetector?.stopMonitoring()
            cameraMonitor?.stopMonitoring()
            Log.d(TAG, "Monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping monitoring", e)
        }
    }

    fun pauseMonitoring() {
        try {
            cameraMonitor?.pauseMonitoring()
            Log.d(TAG, "Monitoring paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing monitoring", e)
        }
    }

    fun resumeMonitoring() {
        try {
            cameraMonitor?.resumeMonitoring()
            Log.d(TAG, "Monitoring resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming monitoring", e)
        }
    }

    private fun setupOverlayDetection() {
        overlayDetector = OverlayDetector(activity) {
            logViolation("OVERLAY_DETECTED")
            handleCriticalViolation()
        }
    }

    fun onWindowFocusChanged(hasFocus: Boolean) {
        overlayDetector?.onWindowFocusChanged(hasFocus)
    }

    private fun setupExternalDisplayMonitoring() {
        try {
            displayManager.registerDisplayListener(
                object : DisplayManager.DisplayListener {
                    override fun onDisplayAdded(displayId: Int) {
                        logViolation("EXTERNAL_DISPLAY_CONNECTED")
                        handleCriticalViolation()
                    }
                    override fun onDisplayRemoved(displayId: Int) {}
                    override fun onDisplayChanged(displayId: Int) {}
                },
                null
            )

            if (displayManager.displays.size > 1) {
                logViolation("EXTERNAL_DISPLAY_ALREADY_CONNECTED")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up external display monitoring", e)
        }
    }

    fun logViolation(type: String) {
        try {
            Log.w(TAG, "🚨 Violation: $type")

            val violation = SecurityViolation(
                type = type,
                timestamp = System.currentTimeMillis(),
                severity = calculateSeverity(type)
            )

            _violations.value = _violations.value + violation
            Log.w(TAG, "Total violations: ${_violations.value.size}, Severity: ${violation.severity}")

            when (violation.severity) {
                Severity.CRITICAL -> handleCriticalViolation()
                Severity.HIGH -> handleHighViolation(type)
                Severity.MEDIUM -> handleMediumViolation()
                Severity.LOW -> handleLowViolation()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging violation", e)
        }
    }

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

    private fun handleHighViolation(type: String) {
        try {
            when (type) {
                "NO_FACE_DETECTED_LONG" -> {
                    noFaceViolationCount++
                    Log.w(TAG, "No face violation #$noFaceViolationCount")

                    when {
                        noFaceViolationCount <= maxNoFaceWarnings -> {
                            _showNoFaceWarning.value = true
                            Log.w(TAG, "Showing no-face warning")
                        }
                        noFaceViolationCount <= maxNoFaceBeforeTerminate -> {
                            pauseExam()
                            pauseMonitoring()
                            _showNoFaceWarning.value = true
                            Log.w(TAG, "Exam paused - no face")
                        }
                        else -> {
                            _shouldAutoSubmit.value = true
                            Log.e(TAG, "Auto-submit - too many no-face violations")
                        }
                    }
                }

                "MULTIPLE_FACES_DETECTED" -> {
                    pauseExam()
                    _shouldAutoSubmit.value = true
                    Log.e(TAG, "Auto-submit - multiple faces")
                }

                "USER_LEFT_APP" -> {
                    Log.w(TAG, "User left app - will handle in onAppResumed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling high violation", e)
        }
    }

    private fun handleMediumViolation() {
        Log.w(TAG, "Medium violation detected")
    }

    private fun handleLowViolation() {
        Log.i(TAG, "Low violation detected")
    }

    fun pauseExam() {
        _isPaused.value = true
        Log.d(TAG, "Exam paused")
    }

    fun resumeExam() {
        _isPaused.value = false
        resumeMonitoring()
        Log.d(TAG, "Exam resumed")
    }

    fun onAppPaused() {
        appPausedTime = System.currentTimeMillis()
        pauseMonitoring()
        Log.d(TAG, "App paused")
    }

    fun onAppResumed() {
        if (appPausedTime > 0) {
            val duration = System.currentTimeMillis() - appPausedTime
            totalTimeOutOfApp += duration
            exitAttempts++

            Log.d(TAG, "App resumed - Exit attempt #$exitAttempts")
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

    fun dismissNoFaceWarning() {
        _showNoFaceWarning.value = false
        if (_isPaused.value) {
            resumeExam()
        }
        Log.d(TAG, "No-face warning dismissed")
    }

    fun dismissExitWarning() {
        _showExitWarning.value = false
        Log.d(TAG, "Exit warning dismissed")
    }

    fun getRemainingAttempts(): Int {
        return (maxExitAttempts - exitAttempts).coerceAtLeast(0)
    }

    fun getNoFaceViolationCount(): Int = noFaceViolationCount

    fun getRemainingNoFaceWarnings(): Int {
        return (maxNoFaceBeforeTerminate - noFaceViolationCount).coerceAtLeast(0)
    }

    fun generateReport(): ExamSecurityReport {
        val report = ExamSecurityReport(
            violations = _violations.value,
            totalExitAttempts = exitAttempts,
            totalTimeOutOfApp = totalTimeOutOfApp,
            securityScore = calculateSecurityScore(),
            timestamp = System.currentTimeMillis()
        )
        Log.d(TAG, "Report: Score=${report.securityScore}, Violations=${report.violations.size}")
        return report
    }

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

    fun cleanup() {
        try {
            stopMonitoring()
            overlayDetector = null
            cameraMonitor = null
            Log.d(TAG, "Cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up", e)
        }
    }
}

data class SecurityViolation(
    val type: String,
    val timestamp: Long,
    val severity: Severity,
    val details: String = ""
)

enum class Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class ExamSecurityReport(
    val violations: List<SecurityViolation>,
    val totalExitAttempts: Int,
    val totalTimeOutOfApp: Long,
    val securityScore: Int,
    val timestamp: Long
)