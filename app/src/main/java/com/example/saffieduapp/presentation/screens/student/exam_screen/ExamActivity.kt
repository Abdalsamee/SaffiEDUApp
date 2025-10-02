package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.os.Build
import android.os.Bundle
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

        // الحصول على examId
        examId = intent.getStringExtra("EXAM_ID") ?: ""

        // ✅ تفعيل الحماية الأمنية (مع تمرير Activity)
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

                // ✅ اعتراض زر الرجوع
                BackHandler {
                    securityManager.logViolation("BACK_BUTTON_PRESSED")
                    showExitDialog = true
                }

                // إنهاء تلقائي عند الوصول للحد الأقصى
                LaunchedEffect(shouldAutoSubmit) {
                    if (shouldAutoSubmit) {
                        // ✅ رسالة مختلفة حسب نوع المخالفة
                        val lastViolation = securityManager.violations.value.lastOrNull()

                        // إذا كانت مخالفة Critical، نعرض Dialog خاص
                        if (lastViolation?.severity == com.example.saffieduapp.presentation.screens.student.exam_screen.security.Severity.CRITICAL) {
                            overlayViolationType = lastViolation.type
                            showOverlayDialog = true
                        } else {
                            val message = when (lastViolation?.type) {
                                "OVERLAY_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف نافذة منبثقة فوق شاشة الاختبار"
                                "MULTI_WINDOW_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف استخدام وضع النوافذ المتعددة"
                                "EXTERNAL_DISPLAY_CONNECTED" -> "تم إنهاء الاختبار: تم اكتشاف شاشة خارجية"
                                else -> "تم إنهاء الاختبار تلقائياً بسبب تجاوز محاولات الخروج"
                            }

                            Toast.makeText(
                                this@ExamActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                            finishExam()
                        }
                    }
                }

                // مراقبة Lifecycle
                LaunchedEffect(Unit) {
                    securityManager.startMonitoring()
                }

                ExamScreen(
                    onNavigateUp = {
                        securityManager.logViolation("NAVIGATE_UP_PRESSED")
                        showExitDialog = true
                    },
                    onExamComplete = {
                        finishExam()
                    }
                )

                // Dialog تحذير الخروج (زر الرجوع)
                if (showExitDialog) {
                    ExamExitWarningDialog(
                        onDismiss = { showExitDialog = false },
                        onConfirmExit = {
                            securityManager.logViolation("USER_FORCED_EXIT")
                            finishExam()
                        }
                    )
                }

                // Dialog تحذير العودة (بعد الخروج بزر Home)
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
            }
        }
    }

    /**
     * إعداد الشاشة الآمنة
     */
    private fun setupSecureScreen() {
        window.apply {
            // 1. منع Screenshot و Screen Recording
            setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )

            // 2. Keep Screen On
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // 3. Full Screen Mode
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        // 4. إخفاء من Recent Apps (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
    }

    /**
     * ✅ منع Picture-in-Picture Mode
     */
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {

        /**
         * ✅ منع Picture-in-Picture Mode
         */
        override fun onPictureInPictureModeChanged(
            isInPictureInPictureMode: Boolean,
            newConfig: android.content.res.Configuration
        ) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

            if (isInPictureInPictureMode) {
                // تسجيل المخالفة وإنهاء الاختبار فوراً
                securityManager.logViolation("PIP_MODE_DETECTED")

                Toast.makeText(
                    this,
                    "تم إنهاء الاختبار: لا يسمح بوضع Picture-in-Picture",
                    Toast.LENGTH_LONG
                ).show()

                finishExam()
            }
        }

        /**
         * مراقبة Multi-Window Mode
         */
        override fun onMultiWindowModeChanged(
            isInMultiWindowMode: Boolean,
            newConfig: android.content.res.Configuration
        ) {
            super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)

            if (isInMultiWindowMode) {
                // تسجيل المخالفة
                securityManager.logViolation("MULTI_WINDOW_DETECTED")

                // إيقاف الاختبار مؤقتاً
                securityManager.pauseExam()
            } else {
                // استئناف الاختبار
                securityManager.resumeExam()
            }
        }

        /**
         * ✅ مراقبة Window Focus - للكشف عن Overlays
         */
        override fun onWindowFocusChanged(hasFocus: Boolean) {
            super.onWindowFocusChanged(hasFocus)

            if (!hasFocus) {
                // قد يكون Overlay أو Dialog أو Notification
                // الـ OverlayDetector سيحدد إذا كان مخالفة حقيقية
            }
        }

        /**
         * مراقبة خروج المستخدم من التطبيق
         */
        override fun onUserLeaveHint() {
            super.onUserLeaveHint()

            // المستخدم ضغط Home أو Recent Apps
            securityManager.logViolation("USER_LEFT_APP")
        }

        override fun onPause() {
            super.onPause()
            // تسجيل وقت الخروج
            securityManager.onAppPaused()
        }

        override fun onResume() {
            super.onResume()
            // تسجيل وقت العودة
            securityManager.onAppResumed()
        }

        override fun onDestroy() {
            super.onDestroy()
            // إيقاف المراقبة
            securityManager.stopMonitoring()
        }

        /**
         * إنهاء الاختبار بشكل آمن
         */
        private fun finishExam() {
            // إنشاء التقرير النهائي
            val report = securityManager.generateReport()

            // TODO: إرسال التقرير للسيرفر

            finish()
        }
    }