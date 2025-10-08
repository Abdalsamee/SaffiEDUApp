package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.BackCameraVideoRecorder
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.RecordingState
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.ScanGuidanceController
import kotlinx.coroutines.delay

@Composable
fun BackScanOverlay(
    recorder: BackCameraVideoRecorder,
    maxDurationMs: Long = 10_000L
) {
    val recordingState by recorder.recordingState.collectAsState()
    val duration by recorder.recordingDuration.collectAsState()

    if (recordingState != RecordingState.RECORDING) return

    val ctx = LocalContext.current
    val guidance = remember { ScanGuidanceController(ctx) }
    val hint by guidance.hint.collectAsState()

    // ابدأ/أوقف الحساسات مع بدء/انتهاء التسجيل
    DisposableEffect(recordingState) {
        if (recordingState == RecordingState.RECORDING) {
            guidance.start()
        }
        onDispose { guidance.stop() }
    }

    // نبضة شفافية لطيفة على الأيقونة
    var pulse by remember { mutableStateOf(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            pulse = 0.4f; delay(400)
            pulse = 1f;   delay(400)
        }
    }
    val iconAlpha by animateFloatAsState(targetValue = pulse, label = "pulse")

    // واجهة نصف شفافة تغطي الشاشة
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.92f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "مسح سريع للمحيط (10 ثوانٍ)",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = (duration / maxDurationMs.toFloat()).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                )

                Spacer(Modifier.height(16.dp))

                // أيقونة الاتجاه
                val icon = when (hint.direction) {
                    ScanGuidanceController.Direction.LEFT  -> Icons.Default.ArrowBack
                    ScanGuidanceController.Direction.RIGHT -> Icons.Default.ArrowForward
                    ScanGuidanceController.Direction.UP    -> Icons.Default.ArrowUpward
                    ScanGuidanceController.Direction.DOWN  -> Icons.Default.ArrowDownward
                    else -> Icons.Default.ArrowForward
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "arrow",
                    tint = Color(0xFF6C63FF),
                    modifier = Modifier
                        .size(72.dp)
                        .alpha(iconAlpha)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = hint.message,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "حرّك الهاتف ببطء لمسح كل الزوايا. سيُغلق تلقائيًا.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
