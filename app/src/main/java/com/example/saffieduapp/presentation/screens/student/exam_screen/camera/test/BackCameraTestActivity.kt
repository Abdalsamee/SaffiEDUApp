package com.example.saffieduapp.presentation.screens.student.exam_screen.camera.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.back.*
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import kotlinx.coroutines.launch

/**
 * 🧪 Activity لاختبار الكاميرا الخلفية بالكامل
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/test/BackCameraTestActivity.kt
 *
 * 🎯 الهدف:
 * اختبار نظام الكاميرا الخلفية الكامل:
 * - المجدول العشوائي
 * - المسجل
 * - الواجهة
 * - التشفير
 */


@Composable
fun BackCameraTestScreen() {
    val viewModel: BackCameraViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // الحالات
    val systemState by viewModel.systemState.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()

    // عرض شاشة المسح
    var showScanScreen by remember { mutableStateOf(false) }
    var previewView by remember { mutableStateOf(viewModel.getPreviewView()) }

    // Logs
    var logs by remember { mutableStateOf(listOf<String>()) }

    fun addLog(message: String) {
        logs = logs + "[${System.currentTimeMillis() % 100000}] $message"
    }

    // مراقبة الأحداث
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BackCameraEvent.PauseExam -> {
                    addLog("⏸️ تم إيقاف الاختبار مؤقتاً")
                }

                is BackCameraEvent.ShowScanScreen -> {
                    addLog("📹 عرض شاشة المسح")
                    showScanScreen = true
                }

                is BackCameraEvent.ResumeExam -> {
                    addLog("▶️ العودة للاختبار")
                    showScanScreen = false
                }

                is BackCameraEvent.Error -> {
                    addLog("❌ خطأ: ${event.message}")
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // إذا كانت شاشة المسح ظاهرة
    if (showScanScreen) {
        RoomScanScreen(
            previewView = previewView,
            recordingState = recordingState,
            onComplete = {
                showScanScreen = false
                addLog("✅ تم الانتهاء من المسح")
            }
        )
        return
    }

    // الشاشة الرئيسية
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
                text = "🧪 اختبار الكاميرا الخلفية",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "اختبار نظام التسجيل العشوائي بالكامل",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // بطاقة الحالة
            StatusCard(
                systemState = systemState,
                recordingState = recordingState
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════
            // السيناريو 1: اختبار سريع (30 ثانية)
            // ═══════════════════════════════════════════

            TestScenarioCard(
                title = "🚀 اختبار سريع",
                description = "اختبار وهمي مدته 30 ثانية\nالتسجيل سيحدث في الثانية 10"
            ) {
                Button(
                    onClick = {
                        logs = emptyList()
                        addLog("🚀 بدء الاختبار السريع...")
                        addLog("⏱️ مدة الاختبار: 30 ثانية")

                        // تهيئة بمدة 30 ثانية
                        viewModel.initialize(
                            sessionId = "test_${System.currentTimeMillis()}",
                            examId = "exam_test",
                            studentId = "student_test",
                            examDurationMs = 30_000L // 30 ثانية
                        )

                        addLog("✅ تم التهيئة - المجدول يعمل الآن")
                        addLog("⏰ سيحدث التسجيل في أي لحظة...")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = systemState is BackCameraSystemState.Idle
                ) {
                    Text("بدء الاختبار السريع (30s)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════
            // السيناريو 2: اختبار متوسط (60 ثانية)
            // ═══════════════════════════════════════════

            TestScenarioCard(
                title = "⏱️ اختبار متوسط",
                description = "اختبار وهمي مدته 60 ثانية\nالتسجيل سيحدث بين 9-51 ثانية"
            ) {
                Button(
                    onClick = {
                        logs = emptyList()
                        addLog("⏱️ بدء الاختبار المتوسط...")
                        addLog("⏱️ مدة الاختبار: 60 ثانية")

                        viewModel.initialize(
                            sessionId = "test_${System.currentTimeMillis()}",
                            examId = "exam_test",
                            studentId = "student_test",
                            examDurationMs = 60_000L // 60 ثانية
                        )

                        addLog("✅ تم التهيئة - المجدول يعمل الآن")
                        addLog("⏰ التسجيل سيحدث بين 9-51 ثانية")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = systemState is BackCameraSystemState.Idle
                ) {
                    Text("بدء الاختبار المتوسط (60s)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════
            // السيناريو 3: اختبار كامل (5 دقائق)
            // ═══════════════════════════════════════════

            TestScenarioCard(
                title = "🎯 اختبار كامل",
                description = "اختبار وهمي مدته 5 دقائق\nتجربة واقعية للنظام"
            ) {
                Button(
                    onClick = {
                        logs = emptyList()
                        addLog("🎯 بدء الاختبار الكامل...")
                        addLog("⏱️ مدة الاختبار: 5 دقائق")

                        viewModel.initialize(
                            sessionId = "test_${System.currentTimeMillis()}",
                            examId = "exam_test",
                            studentId = "student_test",
                            examDurationMs = 5 * 60_000L // 5 دقائق
                        )

                        addLog("✅ تم التهيئة - المجدول يعمل الآن")
                        addLog("⏰ التسجيل سيحدث خلال الـ 5 دقائق...")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = systemState is BackCameraSystemState.Idle
                ) {
                    Text("بدء الاختبار الكامل (5min)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════
            // زر بدء التسجيل (يدوي)
            // ═══════════════════════════════════════════

            if (systemState is BackCameraSystemState.ReadyToRecord) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⏰ حان وقت التسجيل!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                addLog("📹 تحضير الكاميرا...")
                                scope.launch {
                                    viewModel.prepareCamera(
                                        lifecycleOwner = context as ComponentActivity
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تحضير الكاميرا")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // زر بدء التسجيل بعد تحضير الكاميرا
            if (systemState is BackCameraSystemState.CameraReady) {
                Button(
                    onClick = {
                        addLog("🎬 بدء التسجيل...")
                        viewModel.startRecording()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("⏺️ بدء التسجيل الآن")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ═══════════════════════════════════════════
            // Logs
            // ═══════════════════════════════════════════

            if (logs.isNotEmpty()) {
                Text(
                    text = "📝 Logs:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
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
}

@Composable
fun StatusCard(
    systemState: BackCameraSystemState,
    recordingState: BackCameraRecorder.RecordingState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📊 الحالة الحالية",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // حالة النظام
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "النظام:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = when (systemState) {
                        is BackCameraSystemState.Idle -> "خامل"
                        is BackCameraSystemState.Scheduled -> "مجدول ⏰"
                        is BackCameraSystemState.ReadyToRecord -> "جاهز للتسجيل"
                        is BackCameraSystemState.CameraReady -> "الكاميرا جاهزة 📹"
                        is BackCameraSystemState.Recording -> "يسجل ⏺️"
                        is BackCameraSystemState.Completed -> "مكتمل ✅"
                        is BackCameraSystemState.Error -> "خطأ ❌"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // حالة التسجيل
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "التسجيل:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = when (recordingState) {
                        is BackCameraRecorder.RecordingState.Idle -> "خامل"
                        is BackCameraRecorder.RecordingState.Initializing -> "يهيئ..."
                        is BackCameraRecorder.RecordingState.Ready -> "جاهز"
                        is BackCameraRecorder.RecordingState.Recording -> "${recordingState.elapsedSeconds}s"
                        is BackCameraRecorder.RecordingState.Processing -> "يعالج... 🔄"
                        is BackCameraRecorder.RecordingState.Completed -> "مكتمل ✅"
                        is BackCameraRecorder.RecordingState.Error -> "خطأ"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // معلومات إضافية
            if (systemState is BackCameraSystemState.Completed) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "✅ تم التسجيل بنجاح!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "الحجم: ${systemState.recording.fileSize / 1024} KB",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun TestScenarioCard(
    title: String,
    description: String,
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}