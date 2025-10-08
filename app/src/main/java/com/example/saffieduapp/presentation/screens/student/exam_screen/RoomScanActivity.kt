package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.RoomScanCoverageTracker
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import kotlinx.coroutines.launch

class RoomScanActivity : ComponentActivity() {

    private lateinit var sessionManager: ExamSessionManager
    private lateinit var videoRecorder: BackCameraVideoRecorder
    private lateinit var coverageTracker: RoomScanCoverageTracker

    private var examId: String = ""
    private var sessionId: String = ""

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true

        if (cameraGranted && audioGranted) {
            startRoomScan()
        } else {
            Toast.makeText(this, "يجب منح صلاحيات الكاميرا والمايكروفون", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        examId = intent.getStringExtra("EXAM_ID") ?: ""
        val studentId = intent.getStringExtra("STUDENT_ID") ?: ""

        if (examId.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "خطأ في البيانات", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        sessionManager = ExamSessionManager(this, examId, studentId)
        videoRecorder = BackCameraVideoRecorder(this, sessionManager)
        coverageTracker = RoomScanCoverageTracker(this)

        val session = sessionManager.startSession()
        sessionId = session.sessionId

        setContent {
            SaffiEDUAppTheme {
                RoomScanScreen(
                    videoRecorder = videoRecorder,
                    coverage = coverageTracker.state.collectAsState().value,
                    onScanComplete = { proceedToExam() },
                    onSkip = { proceedToExam() }
                )
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val camOk = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val audOk = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        if (camOk && audOk) {
            // جاهزين
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun startRoomScan() {
        lifecycleScope.launch {
            val result = videoRecorder.startRoomScan(
                lifecycleOwner = this@RoomScanActivity,
                sessionId = sessionId,
                coverageTracker = coverageTracker,
                softDurationMs = 10_000L, // 10 ثواني "هدف"
                hardCapMs = 30_000L       // حد أقصى 30 ثانية
            )
            if (result.isFailure) {
                Toast.makeText(this@RoomScanActivity, "فشل بدء التسجيل", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun proceedToExam() {
        val intent = Intent(this, ExamActivity::class.java)
        intent.putExtra("EXAM_ID", examId)
        intent.putExtra("SESSION_ID", sessionId)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        coverageTracker.stop()
        videoRecorder.cleanup()
    }
}

@Composable
fun RoomScanScreen(
    videoRecorder: BackCameraVideoRecorder,
    coverage: com.example.saffieduapp.presentation.screens.student.exam_screen.security.CoverageState,
    onScanComplete: (java.io.File) -> Unit,
    onSkip: () -> Unit
) {
    val recordingState by videoRecorder.recordingState.collectAsState()
    val duration by videoRecorder.recordingDuration.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A2E))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF6C63FF), modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(24.dp))
            Text("مسح الغرفة", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(12.dp))

            val desc = when (recordingState) {
                RecordingState.IDLE -> "استعد لمسح الغرفة بالكاميرا الخلفية"
                RecordingState.RECORDING -> "جارٍ التسجيل... لف الهاتف ببطء لمسح كامل الغرفة"
                RecordingState.STOPPED -> "تم إيقاف التسجيل"
                is RecordingState.COMPLETED -> "تم المسح بنجاح!"
                is RecordingState.ERROR -> "حدث خطأ أثناء التسجيل"
            }
            Text(desc, color = Color.White.copy(alpha = 0.75f), textAlign = TextAlign.Center)

            Spacer(Modifier.height(24.dp))

            if (recordingState == RecordingState.RECORDING) {
                Text(
                    text = "التغطية: ${(coverage.totalPercent * 100f).toInt()}%",
                    color = Color(0xFF6C63FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = coverage.totalPercent.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(0.9f).height(8.dp),
                    color = Color(0xFF6C63FF),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "الأفقي: ${(coverage.yawCoveragePercent * 100f).toInt()}% • الرأسي: ${(coverage.pitchCoveragePercent * 100f).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (coverage.pitchComplete) "تمت حركة لأعلى/أسفل ✅" else "ارفع الهاتف ثم اخفضه لتغطية الأعلى/الأسفل ⬆️⬇️",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(16.dp))
            if (recordingState == RecordingState.RECORDING) {
                Text(
                    text = formatDuration(duration),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C63FF)
                )
                Text("سيتوقف تلقائيًا عند اكتمال التغطية أو بعد 30 ثانية كحد أقصى", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.weight(1f))

            when (val state = recordingState) {
                is RecordingState.COMPLETED -> {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(700)
                        onScanComplete(state.videoFile)
                    }
                }
                is RecordingState.ERROR -> {
                    Text(state.message, color = Color.Red, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onSkip, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))) {
                        Text("المتابعة")
                    }
                }
                else -> {
                    TextButton(onClick = onSkip) {
                        Text("تخطي (غير مُفضّل)", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    return String.format("%02d:%02d", seconds / 60, seconds % 60)
}
