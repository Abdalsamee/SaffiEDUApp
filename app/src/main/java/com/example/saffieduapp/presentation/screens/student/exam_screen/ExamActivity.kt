package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
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

        // تفعيل الحماية الأمنية
        securityManager = ExamSecurityManager(this)
        securityManager.enableSecurityFeatures()

        // إعداد الشاشة
        setupSecureScreen()

        // اعتراض زر الرجوع
        setupBackPressHandler()

        setContent {
            SaffiEDUAppTheme {
                var showExitDialog by remember { mutableStateOf(false) }

                // مراقبة Lifecycle
                LaunchedEffect(Unit) {
                    securityManager.startMonitoring()
                }

                ExamScreen(
                    onNavigateUp = {
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
     * اعتراض زر الرجوع
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // تسجيل محاولة الخروج
                    securityManager.logViolation("BACK_BUTTON_PRESSED")

                    // إظهار Dialog تحذيري
                    // سيتم التعامل معه في Compose
                }
            }
        )
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

            // TODO: إظهار Dialog تحذيري
        } else {
            // استئناف الاختبار
            securityManager.resumeExam()
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