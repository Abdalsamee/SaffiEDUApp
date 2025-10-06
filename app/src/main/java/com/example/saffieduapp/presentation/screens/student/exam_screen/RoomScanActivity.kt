package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.BackCameraVideoRecorder
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.RecordingState
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import kotlinx.coroutines.launch

/**
 * شاشة مسح الغرفة قبل بدء الاختبار
 */
class RoomScanActivity : ComponentActivity() {

    private val TAG = "RoomScanActivity"

    private lateinit var sessionManager: ExamSessionManager
    private lateinit var videoRecorder: BackCameraVideoRecorder

    private var examId: String = ""
    private var sessionId: String = ""

    // طلب صلاحيات الكاميرا والمايكروفون
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true

        if (cameraGranted && audioGranted) {
            startRoomScan()
        } else {
            Toast.makeText(
                this,
                "يجب منح صلاحيات الكاميرا والمايكروفون",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // الحصول على البيانات
        examId = intent.getStringExtra("EXAM_ID") ?: ""
        val studentId = intent.getStringExtra("STUDENT_ID") ?: ""

        if (examId.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "خطأ في البيانات", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // تهيئة المديرين
        sessionManager = ExamSessionManager(this, examId, studentId)
        videoRecorder = BackCameraVideoRecorder(this, sessionManager)

        // بدء الجلسة
        val session = sessionManager.startSession()
        sessionId = session.sessionId

        setContent {
            SaffiEDUAppTheme {
                RoomScanScreen(
                    videoRecorder = videoRecorder,
                    onScanComplete = { videoFile ->
                        proceedToExam()
                    },
                    onSkip = {
                        // السماح بالتخطي (اختياري)
                        proceedToExam()
                    }
                )
            }
        }

        // فحص الصلاحيات
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        val audioPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )

        if (cameraPermission == PackageManager.PERMISSION_GRANTED &&
            audioPermission == PackageManager.PERMISSION_GRANTED
        ) {
            // الصلاحيات ممنوحة
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    private fun startRoomScan() {
        lifecycleScope.launch {
            val result = videoRecorder.startRoomScan(this@RoomScanActivity, sessionId)

            if (result.isFailure) {
                Toast.makeText(
                    this@RoomScanActivity,
                    "فشل بدء التسجيل: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun proceedToExam() {
        // الانتقال إلى شاشة الاختبار
        val intent = Intent(this, ExamActivity::class.java)
        intent.putExtra("EXAM_ID", examId)
        intent.putExtra("SESSION_ID", sessionId)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoRecorder.cleanup()
    }
}

/**
 * شاشة Compose لمسح الغرفة
 */
@Composable
fun RoomScanScreen(
    videoRecorder: BackCameraVideoRecorder,
    onScanComplete: (java.io.File) -> Unit,
    onSkip: () -> Unit
) {
    val recordingState by videoRecorder.recordingState.collectAsState()
    val duration by videoRecorder.recordingDuration.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // الأيقونة
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "Camera",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF6C63FF)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // العنوان
            Text(
                text = "مسح الغرفة",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // الوصف
            Text(
                text = when (recordingState) {
                    RecordingState.IDLE -> "استخدم الكاميرا الخلفية لمسح الغرفة من جميع الجوانب"
                    RecordingState.RECORDING -> "جارٍ التسجيل... قم بتحريك الهاتف لمسح الغرفة"
                    RecordingState.STOPPED -> "تم إيقاف التسجيل"
                    is RecordingState.COMPLETED -> "تم المسح بنجاح!"
                    is RecordingState.ERROR -> "حدث خطأ في التسجيل"
                },
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // المدة
            if (recordingState == RecordingState.RECORDING) {
                Text(
                    text = formatDuration(duration),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C63FF)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "من 30 ثانية",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // مؤشر التقدم
                LinearProgressIndicator(
                    progress = (duration / 30000f).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp),
                    color = Color(0xFF6C63FF),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // التعليمات
            if (recordingState == RecordingState.RECORDING) {
                InstructionCard(
                    instructions = listOf(
                        "احمل الهاتف بثبات",
                        "قم بتحريكه ببطء لمسح الغرفة",
                        "تأكد من إظهار جميع الزوايا",
                        "سيتوقف التسجيل تلقائياً بعد 30 ثانية"
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // معالجة حالة الاكتمال
            when (val state = recordingState) {
                is RecordingState.COMPLETED -> {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1000)
                        onScanComplete(state.videoFile)
                    }
                }
                is RecordingState.ERROR -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onSkip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        )
                    ) {
                        Text("المتابعة بدون مسح")
                    }
                }
                else -> {
                    // زر التخطي (اختياري)
                    TextButton(onClick = onSkip) {
                        Text(
                            "تخطي مسح الغرفة",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstructionCard(instructions: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "التعليمات",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            instructions.forEach { instruction ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "•",
                        color = Color(0xFF6C63FF),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = instruction,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    return String.format("%02d:%02d", seconds / 60, seconds % 60)
}