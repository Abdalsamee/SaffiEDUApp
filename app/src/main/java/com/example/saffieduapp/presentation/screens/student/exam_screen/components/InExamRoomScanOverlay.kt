package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.InExamScanUiState

@Composable
fun InExamRoomScanOverlay(state: InExamScanUiState) {
    // Fullscreen scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "جارٍ مسح المحيط بالكاميرا الخلفية",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "من فضلك حرّك الهاتف ببطء لليمين واليسار، وارفعه لأعلى ثم لأسفل لمسح كامل الغرفة.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // الوقت
                Text(
                    text = "المدة: ${formatDuration(state.durationMs)}",
                    color = Color(0xFF60A5FA),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                // إجمالي التغطية
                Text(
                    text = "نسبة التغطية الإجمالية",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = state.coverage.totalPercent.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = Color(0xFF60A5FA),
                    trackColor = Color.White.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${(state.coverage.totalPercent * 100f).toInt()}%",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(12.dp))

                // أفقي/رأسي
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "الأفقي",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = state.coverage.yawCoveragePercent.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFF34D399),
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${(state.coverage.yawCoveragePercent * 100f).toInt()}%",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "الرأسي",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = state.coverage.pitchCoveragePercent.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFFF59E0B),
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${(state.coverage.pitchCoveragePercent * 100f).toInt()}%",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (state.coverage.pitchComplete)
                        "تم إظهار الأعلى/الأسفل ✅ استمر حتى يكتمل الشريط"
                    else
                        "⬆️ ارفع الهاتف قليلًا للأعلى ثم ⬇️ أخفضه لأسفل لإكمال المسح الرأسي",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val s = (ms / 1000).toInt()
    return String.format("%02d:%02d", s / 60, s % 60)
}
