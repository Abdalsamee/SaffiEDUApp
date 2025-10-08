package com.example.saffieduapp.presentation.screens.student.exam_screen.camera.back

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.compose.ui.draw.scale
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.CameraMonitoringConfig

/**
 * 🖼️ شاشة مسح الغرفة
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/back/RoomScanScreen.kt
 *
 * 🎯 الهدف:
 * عرض واجهة مسح الغرفة للطالب
 *
 * 📊 المواصفات:
 * - شاشة كاملة
 * - معاينة مباشرة من الكاميرا الخلفية
 * - تعليمات واضحة بالعربية
 * - عداد تنازلي (10 → 0)
 * - شريط تقدم
 * - رسالة نجاح وعودة تلقائية
 */

@Composable
fun RoomScanScreen(
    previewView: PreviewView,
    recordingState: BackCameraRecorder.RecordingState,
    onComplete: () -> Unit,
    onStartRecording: () -> Unit = {} // إضافة callback لبدء التسجيل
) {
    // بدء التسجيل تلقائياً عند ظهور الشاشة
    LaunchedEffect(Unit) {
        // انتظار قصير للتهيئة
        kotlinx.coroutines.delay(500)
        if (recordingState is BackCameraRecorder.RecordingState.Ready) {
            onStartRecording()
        }
    }

    // مراقبة حالة التسجيل
    LaunchedEffect(recordingState) {
        if (recordingState is BackCameraRecorder.RecordingState.Completed) {
            // الانتظار قليلاً لعرض رسالة النجاح
            kotlinx.coroutines.delay(CameraMonitoringConfig.BackCamera.SUCCESS_MESSAGE_DURATION)
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (recordingState) {
            is BackCameraRecorder.RecordingState.Idle,
            is BackCameraRecorder.RecordingState.Initializing -> {
                LoadingScreen()
            }

            is BackCameraRecorder.RecordingState.Ready,
            is BackCameraRecorder.RecordingState.Recording -> {
                RecordingScreen(
                    previewView = previewView,
                    elapsedSeconds = if (recordingState is BackCameraRecorder.RecordingState.Recording) {
                        recordingState.elapsedSeconds
                    } else 0
                )
            }

            is BackCameraRecorder.RecordingState.Processing -> {
                ProcessingScreen()
            }

            is BackCameraRecorder.RecordingState.Completed -> {
                SuccessScreen()
            }

            is BackCameraRecorder.RecordingState.Error -> {
                ErrorScreen(message = recordingState.message)
            }
        }
    }
}

// ═══════════════════════════════════════════
// شاشة التحميل
// ═══════════════════════════════════════════

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "جاري تهيئة الكاميرا...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ═══════════════════════════════════════════
// شاشة التسجيل
// ═══════════════════════════════════════════

@Composable
private fun RecordingScreen(
    previewView: PreviewView,
    elapsedSeconds: Int
) {
    val remainingSeconds = 10 - elapsedSeconds
    val progress = elapsedSeconds / 10f

    Box(modifier = Modifier.fillMaxSize()) {
        // 📹 معاينة الكاميرا
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // طبقة شفافة
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // المحتوى
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // العنوان والتعليمات
            TopSection()

            Spacer(modifier = Modifier.weight(1f))

            // العداد وشريط التقدم
            BottomSection(
                remainingSeconds = remainingSeconds,
                progress = progress
            )
        }
    }
}

@Composable
private fun TopSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // أيقونة
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // العنوان
            Text(
                text = "مسح الغرفة 📹",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // التعليمات
            Text(
                text = "يرجى تحريك الهاتف ببطء\nلمسح الغرفة من جميع الجوانب",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // أيقونات التوجيه
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DirectionIcon(Icons.Default.ArrowForward)
                DirectionIcon(Icons.Default.ArrowBack)
                DirectionIcon(Icons.Default.ArrowUpward)
                DirectionIcon(Icons.Default.ArrowDownward)
            }
        }
    }
}

@Composable
private fun DirectionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    // أنيميشن نبض
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color.White.copy(alpha = alpha),
        modifier = Modifier.size(32.dp)
    )
}

@Composable
private fun BottomSection(
    remainingSeconds: Int,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // شريط التقدم
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "التقدم",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$remainingSeconds / 10",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // شريط التقدم
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF4CAF50),
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // العداد التنازلي
            CountdownTimer(remainingSeconds)

            Spacer(modifier = Modifier.height(16.dp))

            // رسالة
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سيتم العودة تلقائياً بعد انتهاء التسجيل",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CountdownTimer(seconds: Int) {
    // أنيميشن نبض للعداد
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                color = when {
                    seconds > 7 -> Color(0xFF4CAF50) // أخضر
                    seconds > 3 -> Color(0xFFFFC107) // أصفر
                    else -> Color(0xFFF44336) // أحمر
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seconds.toString(),
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
        )
    }
}

// ═══════════════════════════════════════════
// شاشة المعالجة
// ═══════════════════════════════════════════

@Composable
private fun ProcessingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // أنيميشن دائري
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = Color(0xFF2196F3),
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "جاري المعالجة...",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تشفير وحفظ التسجيل",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// شاشة النجاح
// ═══════════════════════════════════════════

@Composable
private fun SuccessScreen() {
    // أنيميشن ظهور
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.scale(scale)
        ) {
            Column(
                modifier = Modifier
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // أيقونة صح
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "تم المسح بنجاح ✓",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "جاري العودة للاختبار...",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// شاشة الخطأ
// ═══════════════════════════════════════════

@Composable
private fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF44336).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // أيقونة خطأ
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "حدث خطأ",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}