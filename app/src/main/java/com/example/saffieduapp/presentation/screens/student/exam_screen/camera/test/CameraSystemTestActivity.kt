package com.example.saffieduapp.presentation.screens.student.exam_screen.camera.test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.CameraMonitoringConfig
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.back.BackCameraScheduler
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.front.CaptureDecision
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.front.SnapshotPrioritySystem
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.models.*
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.storage.EncryptedMediaStorage
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.UUID

/**
 * 🧪 Activity لاختبار نظام المراقبة بالكاميرا
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/test/CameraSystemTestActivity.kt
 *
 * 🎯 الهدف:
 * اختبار جميع المكونات التي بنيناها
 */
class CameraSystemTestActivity : ComponentActivity() {

    private val TAG = "CameraTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SaffiEDUAppTheme {
                CameraSystemTestScreen()
            }
        }
    }
}

@Composable
fun CameraSystemTestScreen() {
    var logs by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()

    // إنشاء المكونات
    val context = androidx.compose.ui.platform.LocalContext.current
    val storage = remember { EncryptedMediaStorage(context, "test_session_${System.currentTimeMillis()}") }
    val prioritySystem = remember { SnapshotPrioritySystem() }

    var scheduler: BackCameraScheduler? by remember { mutableStateOf(null) }
    var schedulerInfo by remember { mutableStateOf("غير مجدول") }

    // دالة لإضافة log
    fun addLog(message: String) {
        Log.d("CameraTest", message)
        logs = logs + "✅ $message"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // العنوان
            Text(
                text = "🧪 اختبار نظام المراقبة",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "اختبر جميع المكونات التي بنيناها",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════════
            // اختبار 1: الإعدادات
            // ═══════════════════════════════════════════

            TestSection(title = "1️⃣ اختبار الإعدادات") {
                Button(
                    onClick = {
                        logs = emptyList()
                        addLog("📖 قراءة الإعدادات...")

                        addLog("الكاميرا الخلفية:")
                        addLog("  - مدة التسجيل: ${CameraMonitoringConfig.BackCamera.RECORDING_DURATION / 1000}s")
                        addLog("  - الدقة: ${CameraMonitoringConfig.BackCamera.TARGET_WIDTH}x${CameraMonitoringConfig.BackCamera.TARGET_HEIGHT}")
                        addLog("  - الحد الأدنى للاختبار: ${CameraMonitoringConfig.BackCamera.MIN_EXAM_DURATION_FOR_RECORDING / 60000}min")

                        addLog("الكاميرا الأمامية:")
                        addLog("  - الحد الأقصى للصور: ${CameraMonitoringConfig.FrontCamera.MAX_SNAPSHOTS}")
                        addLog("  - Cooldown عادي: ${CameraMonitoringConfig.FrontCamera.COOLDOWN_NORMAL / 1000}s")

                        // اختبار الحسابات
                        val examDuration = 60 * 60 * 1000L // 60 دقيقة
                        val randomTime = CameraMonitoringConfig.calculateRandomRecordingTime(examDuration)
                        addLog("وقت تسجيل عشوائي لاختبار 60 دقيقة: ${randomTime / 60000}min ${(randomTime % 60000) / 1000}s")

                        val expectedSize = CameraMonitoringConfig.calculateExpectedVideoSize(10_000)
                        addLog("الحجم المتوقع لفيديو 10s: ${expectedSize / 1024} KB")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("اختبار الإعدادات")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════
            // اختبار 2: التخزين المشفر
            // ═══════════════════════════════════════════

            TestSection(title = "2️⃣ اختبار التخزين المشفر") {
                Button(
                    onClick = {
                        logs = emptyList()
                        addLog("🔐 اختبار التخزين المشفر...")

                        try {
                            // الحصول على معلومات الجلسة
                            val info = storage.getSessionInfo()
                            addLog("معرف الجلسة: ${info.sessionId}")
                            addLog("المسار: ${info.sessionPath}")
                            addLog("المساحة المتاحة: ${info.availableSpace / 1024 / 1024} MB")

                            // اختبار حفظ metadata
                            val testData = """{"test": "data", "timestamp": ${System.currentTimeMillis()}}"""
                            val result = storage.saveMetadata("test", testData)
                            if (result.isSuccess) {
                                addLog("✅ تم حفظ metadata بنجاح")
                            }

                            // اختبار قراءة metadata
                            val readResult = storage.readMetadata("test")
                            if (readResult.isSuccess) {
                                addLog("✅ تم قراءة metadata بنجاح")
                            }

                            // اختبار log
                            storage.appendLog("Test log message")
                            addLog("✅ تم كتابة log بنجاح")

                            // اختبار التشفير (محاكاة)
                            addLog("محاكاة تشفير ملف...")
                            val tempFile = File(context.cacheDir, "test.txt")
                            tempFile.writeText("Test content for encryption")

                            val encrypted = storage.encryptVideo(tempFile)
                            if (encrypted.isSuccess) {
                                addLog("✅ تم التشفير بنجاح: ${encrypted.getOrNull()?.name}")
                            }

                            tempFile.delete()

                        } catch (e: Exception) {
                            addLog("❌ خطأ: ${e.message}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("اختبار التخزين")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════
            // اختبار 3: المجدول العشوائي
            // ═══════════════════════════════════════════

            TestSection(title = "3️⃣ اختبار المجدول العشوائي") {
                Column {
                    Button(
                        onClick = {
                            logs = emptyList()
                            addLog("⏰ اختبار المجدول العشوائي...")

                            // إنشاء مجدول لاختبار 60 دقيقة
                            val examDuration = 60 * 60 * 1000L
                            scheduler = BackCameraScheduler(examDuration) {
                                addLog("🎬 تم الوصول لوقت التسجيل!")
                                addLog("يجب بدء التسجيل الآن...")
                            }

                            scheduler?.start()

                            // الحصول على معلومات الجدولة
                            scope.launch {
                                delay(500) // انتظار قصير للتأكد من التهيئة
                                val info = scheduler?.getScheduleInfo()
                                info?.let {
                                    schedulerInfo = buildString {
                                        appendLine("الوقت المجدول: ${it.scheduledTimeFromStart}")
                                        appendLine("الوقت المتبقي: ${it.remainingTime}")
                                        appendLine("في انتظار: ${it.isWaiting}")
                                        appendLine("تم التفعيل: ${it.isTriggered}")
                                    }
                                    addLog("📊 معلومات الجدولة:")
                                    addLog("  - الوقت المجدول: ${it.scheduledTimeFromStart}")
                                    addLog("  - الوقت المتبقي: ${it.remainingTime}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("بدء المجدول")
                    }

                    if (schedulerInfo != "غير مجدول") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = schedulerInfo,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scheduler?.stop()
                            addLog("🛑 تم إيقاف المجدول")
                            schedulerInfo = "غير مجدول"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("إيقاف المجدول")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════
            // اختبار 4: نظام الأولويات
            // ═══════════════════════════════════════════

            TestSection(title = "4️⃣ اختبار نظام الأولويات") {
                Column {
                    Button(
                        onClick = {
                            logs = emptyList()
                            addLog("🎯 اختبار نظام الأولويات...")

                            // إعادة تعيين
                            prioritySystem.reset()
                            addLog("✅ تم إعادة تعيين النظام")

                            // اختبار P0 - Critical (وجوه متعددة)
                            addLog("\n🔴 اختبار P0 - Critical:")
                            val decision1 = prioritySystem.evaluateCapture(SnapshotReason.MULTIPLE_FACES)
                            when (decision1) {
                                is CaptureDecision.Approved -> {
                                    addLog("✅ موافق على الالتقاط")
                                    addLog("  - الأولوية: ${decision1.priority.arabicName}")
                                    addLog("  - الإجراء: ${decision1.action}")
                                    prioritySystem.recordCapture(SnapshotReason.MULTIPLE_FACES, decision1.violationType)
                                }
                                is CaptureDecision.Rejected -> {
                                    addLog("❌ مرفوض: ${decision1.reason}")
                                }
                            }

                            // اختبار P1 - High (ينظر بعيداً)
                            addLog("\n🟡 اختبار P1 - High:")
                            val decision2 = prioritySystem.evaluateCapture(SnapshotReason.LOOKING_AWAY)
                            when (decision2) {
                                is CaptureDecision.Approved -> {
                                    addLog("✅ موافق على الالتقاط")
                                    addLog("  - الأولوية: ${decision2.priority.arabicName}")
                                    prioritySystem.recordCapture(SnapshotReason.LOOKING_AWAY, decision2.violationType)
                                }
                                is CaptureDecision.Rejected -> {
                                    addLog("❌ مرفوض: ${decision2.reason}")
                                }
                            }

                            // اختبار P2 - Normal (فحص دوري)
                            addLog("\n🟢 اختبار P2 - Normal:")
                            val decision3 = prioritySystem.evaluateCapture(SnapshotReason.PERIODIC_CHECK)
                            when (decision3) {
                                is CaptureDecision.Approved -> {
                                    addLog("✅ موافق على الالتقاط")
                                    addLog("  - الأولوية: ${decision3.priority.arabicName}")
                                    prioritySystem.recordCapture(SnapshotReason.PERIODIC_CHECK, decision3.violationType)
                                }
                                is CaptureDecision.Rejected -> {
                                    addLog("❌ مرفوض: ${decision3.reason}")
                                }
                            }

                            // طباعة الحالة
                            addLog("\n📊 الحالة الحالية:")
                            val state = prioritySystem.state.value
                            addLog("  - الصور: ${state.snapshotsTaken} / ${CameraMonitoringConfig.FrontCamera.MAX_SNAPSHOTS}")
                            addLog("  - المتبقي: ${state.snapshotsRemaining}")
                            addLog("  - الأولويات النشطة: ${state.currentActivePriorities.map { it.arabicName }}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("اختبار الأولويات")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // محاكاة التقاط 10 صور
                    Button(
                        onClick = {
                            logs = emptyList()
                            addLog("🎬 محاكاة التقاط 10 صور...")

                            prioritySystem.reset()

                            scope.launch {
                                repeat(10) { i ->
                                    delay(500)

                                    val reasons = listOf(
                                        SnapshotReason.MULTIPLE_FACES,
                                        SnapshotReason.NO_FACE,
                                        SnapshotReason.LOOKING_AWAY,
                                        SnapshotReason.PERIODIC_CHECK
                                    )
                                    val randomReason = reasons.random()

                                    val decision = prioritySystem.evaluateCapture(randomReason)
                                    when (decision) {
                                        is CaptureDecision.Approved -> {
                                            prioritySystem.recordCapture(randomReason, decision.violationType)
                                            addLog("${i + 1}. ✅ ${randomReason.arabicName} - ${decision.priority.arabicName}")
                                        }
                                        is CaptureDecision.Rejected -> {
                                            addLog("${i + 1}. ❌ ${randomReason.arabicName} - ${decision.reason}")
                                        }
                                    }
                                }

                                val state = prioritySystem.state.value
                                addLog("\n📊 النتيجة النهائية:")
                                addLog("  - الصور الملتقطة: ${state.snapshotsTaken}")
                                addLog("  - الأولويات النشطة: ${state.currentActivePriorities.map { it.arabicName }}")
                                if (state.shouldAutoSubmit) {
                                    addLog("⚠️ شروط التسليم التلقائي تحققت!")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("محاكاة 10 صور")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════════
            // عرض الـ Logs
            // ═══════════════════════════════════════════

            if (logs.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "📝 Logs:",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        logs.forEach { log ->
                            Text(
                                text = log,
                                fontSize = 11.sp,
                                color = Color(0xFF00FF00),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // تنظيف عند الخروج
    DisposableEffect(Unit) {
        onDispose {
            scheduler?.cleanup()
        }
    }
}

@Composable
fun TestSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}