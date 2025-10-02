package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.ExamReturnWarningDialog
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.ExamExitWarningDialog
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.OverlayDetectedDialog
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.MultiWindowBlockedDialog
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.ExamSecurityManager
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity منفصلة للاختبار مع حماية كاملة
 */
@AndroidEntryPoint
class ExamActivity : ComponentActivity() {

    private lateinit var securityManager: ExamSecurityManager
    private var examId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // الحصول على examId
            examId = intent.getStringExtra("EXAM_ID") ?: ""

            // ✅ فحص Multi-Window قبل بدء الاختبار
            if (isInMultiWindowMode) {
                Log.w("ExamActivity", "Multi-window detected at onCreate")
                showMultiWindowBlockedDialog()
                return
            }

            // تفعيل الحماية الأمنية
            securityManager = ExamSecurityManager(this, this)
            securityManager.enableSecurityFeatures()

            // إعداد الشاشة
            setupSecureScreen()

            setContent {
                SaffiEDUAppTheme {
                    var showExitDialog by remember { mutableStateOf(false) }
                    var showOverlayDialog by remember { mutableStateOf(false) }
                    var overlayViolationType by remember { mutableStateOf("") }

                    val shouldShowWarning by securityManager.shouldShowWarning.collectAsState()
                    val shouldAutoSubmit by securityManager.shouldAutoSubmit.collectAsState()

                    // اعتراض زر الرجوع
                    BackHandler {
                        securityManager.logViolation("BACK_BUTTON_PRESSED")
                        showExitDialog = true
                    }

                    // إنهاء تلقائي عند الوصول للحد الأقصى
                    LaunchedEffect(shouldAutoSubmit) {
                        if (shouldAutoSubmit) {
                            val lastViolation = securityManager.violations.value.lastOrNull()

                            if (lastViolation?.severity == com.example.saffieduapp.presentation.screens.student.exam_screen.security.Severity.CRITICAL) {
                                overlayViolationType = lastViolation.type
                                showOverlayDialog = true
                            } else {
                                val message = when (lastViolation?.type) {
                                    "OVERLAY_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف نافذة منبثقة"
                                    "MULTI_WINDOW_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف وضع النوافذ المتعددة"
                                    "EXTERNAL_DISPLAY_CONNECTED" -> "تم إنهاء الاختبار: تم اكتشاف شاشة خارجية"
                                    else -> "تم إنهاء الاختبار تلقائياً بسبب تجاوز محاولات الخروج"
                                }

                                Toast.makeText(this@ExamActivity, message, Toast.LENGTH_LONG).show()
                                finishExam()
                            }
                        }
                    }

                    // مراقبة Lifecycle
                    LaunchedEffect(Unit) {
                        securityManager.startMonitoring()
                    }

                    // شاشة الاختبار الأصلية
                    ExamScreen(
                        onNavigateUp = {
                            securityManager.logViolation("NAVIGATE_UP_PRESSED")
                            showExitDialog = true
                        },
                        onExamComplete = {
                            finishExam()
                        }
                    )

                    // Dialog تحذير الخروج
                    if (showExitDialog) {
                        ExamExitWarningDialog(
                            onDismiss = { showExitDialog = false },
                            onConfirmExit = {
                                securityManager.logViolation("USER_FORCED_EXIT")
                                finishExam()
                            }
                        )
                    }

                    // Dialog تحذير العودة
                    if (shouldShowWarning) {
                        val exitCount = remember(shouldShowWarning) {
                            securityManager.violations.value.count {
                                it.type.startsWith("APP_RESUMED")
                            }
                        }

                        ExamReturnWarningDialog(
                            exitAttempts = exitCount,
                            remainingAttempts = securityManager.getRemainingAttempts(),
                            onContinue = {
                                securityManager.dismissWarning()
                            }
                        )
                    }

                    // Dialog تحذير Overlay
                    if (showOverlayDialog) {
                        OverlayDetectedDialog(
                            violationType = overlayViolationType,
                            onDismiss = {
                                showOverlayDialog = false
                                finishExam()
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ExamActivity", "Error in onCreate", e)
            Toast.makeText(this, "خطأ في بدء الاختبار", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupSecureScreen() {
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (isInPictureInPictureMode) {
            if (::securityManager.isInitialized) {
                securityManager.logViolation("PIP_MODE_DETECTED")
            }
            Toast.makeText(this, "تم إنهاء الاختبار: لا يسمح بوضع Picture-in-Picture", Toast.LENGTH_LONG).show()

            if (::securityManager.isInitialized) {
                finishExam()
            } else {
                finish()
            }
        }
    }

    override fun onMultiWindowModeChanged(
        isInMultiWindowMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)

        if (isInMultiWindowMode) {
            if (::securityManager.isInitialized) {
                securityManager.logViolation("MULTI_WINDOW_DETECTED")
            }

            Toast.makeText(this, "⚠️ تم إنهاء الاختبار: تم اكتشاف وضع تقسيم الشاشة", Toast.LENGTH_LONG).show()

            if (::securityManager.isInitialized) {
                finishExam()
            } else {
                finish()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)

        if (isInMultiWindowMode && ::securityManager.isInitialized) {
            securityManager.logViolation("MULTI_WINDOW_CONFIG_CHANGE")
            finishExam()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (::securityManager.isInitialized) {
            securityManager.onWindowFocusChanged(hasFocus)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (::securityManager.isInitialized) {
            securityManager.logViolation("USER_LEFT_APP")
        }
    }

    override fun onPause() {
        super.onPause()
        if (::securityManager.isInitialized) {
            securityManager.onAppPaused()
        }
    }

    override fun onResume() {
        super.onResume()

        if (isInMultiWindowMode) {
            if (::securityManager.isInitialized) {
                securityManager.logViolation("MULTI_WINDOW_ON_RESUME")
            }
            Toast.makeText(this, "⚠️ تم اكتشاف وضع تقسيم الشاشة", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (::securityManager.isInitialized) {
            securityManager.onAppResumed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::securityManager.isInitialized) {
            securityManager.stopMonitoring()
        }
    }

    private fun finishExam() {
        try {
            if (::securityManager.isInitialized) {
                val report = securityManager.generateReport()
                Log.d("ExamActivity", "Security Report: $report")
                // TODO: إرسال التقرير للسيرفر
            }
        } catch (e: Exception) {
            Log.e("ExamActivity", "Error generating report", e)
        } finally {
            finish()
        }
    }

    private fun showMultiWindowBlockedDialog() {
        setContent {
            SaffiEDUAppTheme {
                MultiWindowBlockedDialog(
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }
}