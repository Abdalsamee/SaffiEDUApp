package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.CoverageState
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.InExamScanUiState
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Overlay مُحسّن للمسح المفاجئ:
 * - أسهم كبيرة مع نبض عند الحاجة
 * - "نغزات" بصرية إذا ما في حركة (بعد ~2 ثانية)
 * - عناصر أقرب وأكثر وضوحًا
 */
@Composable
fun InExamRoomScanOverlay(
    state: InExamScanUiState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = state.visible) {
        // خلفية داكنة بتعتيم بسيط
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xE61A1C24)),
            contentAlignment = Center
        ) {
            // منطق الحركة/النبض
            val (needRight, needLeft, needDown) = remember(state.coverage) {
                // ترتيب الأولوية: يمين → يسار → أسفل
                val rightDone = state.coverage.yawCoveragePercent >= 0.33f
                val leftDone  = state.coverage.yawCoveragePercent >= 0.66f
                val downDone  = state.coverage.pitchDownReached
                Triple(!rightDone, !leftDone, !downDone)
            }

            // مراقبة "الثبات" (عدم تقدم التغطية) -> نغزات
            var lastProgress by remember { mutableFloatStateOf(totalProgress(state.coverage)) }
            var lastChangeAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
            var isStale by remember { mutableStateOf(false) }

            LaunchedEffect(state.coverage) {
                val now = System.currentTimeMillis()
                val p = totalProgress(state.coverage)
                if (p - lastProgress > 0.01f) {
                    lastProgress = p
                    lastChangeAt = now
                    isStale = false
                } else {
                    // لو مر أكثر من 2000ms بدون تحسّن → اعتبره جمود
                    isStale = (now - lastChangeAt) > 2000
                }
            }

            // عدّاد تسجيل صغير "REC"
            RecPill(elapsedMs = state.durationMs, modifier = Modifier.align(TopEnd).padding(16.dp))

            // الهاتف "إطار" + الأسهم
            PhoneWithArrows(
                needRight = needRight,
                needLeft = needLeft,
                needDown = needDown,
                staleNudge = isStale
            )

            // تعليمات سريعة في الوسط
            QuickHint(
                needRight = needRight,
                needLeft = needLeft,
                needDown = needDown,
                staleNudge = isStale,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 180.dp)
            )

            // شارات الإنجاز أسفل
            BottomChips(
                coverage = state.coverage,
                modifier = Modifier.align(BottomCenter)
            )
        }
    }
}

/* ===================== قطع واجهة ===================== */

@Composable
private fun RecPill(elapsedMs: Long, modifier: Modifier = Modifier) {
    val seconds = (elapsedMs / 1000L).toInt()
    val min = seconds / 60
    val sec = seconds % 60

    val pulse = rememberInfiniteTransition(label = "rec")
        .animateFloat(
            initialValue = 0.7f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(700, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ), label = "recAnim"
        )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xCC0D0F14))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF5252))
                .scale(pulse.value)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "REC  %02d:%02d".format(min, sec),
            color = Color(0xFFE6E9F2),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * إطار هاتف بسيط + أسهم كبيرة على الجوانب
 */
@Composable
private fun BoxScope.PhoneWithArrows(
    needRight: Boolean,
    needLeft: Boolean,
    needDown: Boolean,
    staleNudge: Boolean
) {
    // إطار الهاتف (نسبة 9:19)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.65f)
            .aspectRatio(9f / 19f)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0D0F14))
            .align(Center)
    )

    // سهم يمين
    DirectionArrow(
        icon = Icons.Rounded.ArrowForward,
        active = needRight,
        stale = staleNudge && needRight,
        modifier = Modifier
            .align(CenterEnd)
            .offset(x = (-10).dp)
    )
    // سهم يسار
    DirectionArrow(
        icon = Icons.Rounded.ArrowBack,
        active = needLeft,
        stale = staleNudge && !needRight && needLeft, // لو خلّص يمين ولسّا ما تحرك يسار
        modifier = Modifier
            .align(CenterStart)
            .offset(x = 10.dp)
    )
    // سهم أسفل
    DirectionArrow(
        icon = Icons.Rounded.ArrowDownward,
        active = needDown,
        stale = staleNudge && !needRight && !needLeft && needDown, // آخر أولوية
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = (-90).dp)
    )
}

/**
 * سهم اتجاهي مع نبض/وميض عند الحاجة
 */
@Composable
private fun DirectionArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    stale: Boolean,
    modifier: Modifier = Modifier
) {
    val baseColor = if (active) Color(0xFF7EABFF) else Color(0xFF3A4256)
    val pulseScale = rememberInfiniteTransition(label = "arrowPulse")
        .animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                tween(if (stale) 400 else 900, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ), label = "arrowPulseAnim"
        )
    val pulseAlpha = rememberInfiniteTransition(label = "arrowAlpha")
        .animateFloat(
            initialValue = if (stale) 0.55f else 0.35f,
            targetValue = if (stale) 1f else 0.7f,
            animationSpec = infiniteRepeatable(
                tween(if (stale) 400 else 900, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ), label = "arrowAlphaAnim"
        )

    Box(
        modifier = modifier
            .size(74.dp)
            .scale(if (active) pulseScale.value else 1f)
            .alpha(if (active) pulseAlpha.value else 0.35f)
            .clip(RoundedCornerShape(18.dp))
            .background(baseColor.copy(alpha = 0.18f)),
        contentAlignment = Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (stale) Color(0xFFFF5F5F) else Color(0xFFBFD4FF),
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * تلميح سريع في الوسط يوضح المطلوب الآن
 */
@Composable
private fun QuickHint(
    needRight: Boolean,
    needLeft: Boolean,
    needDown: Boolean,
    staleNudge: Boolean,
    modifier: Modifier = Modifier
) {
    val text = when {
        needRight -> "حرّك الهاتف نحو اليمين"
        needLeft  -> "الآن نحو اليسار"
        needDown  -> "أمِل الهاتف إلى الأسفل"
        else      -> "ممتاز! استمر…"
    }
    val accent = when {
        staleNudge && (needRight || needLeft || needDown) -> Color(0xFFFF5F5F)
        else -> Color(0xFFB2C7FF)
    }
    val scale by animateFloatAsState(
        targetValue = if (staleNudge && (needRight || needLeft || needDown)) 1.06f else 1f,
        animationSpec = tween(250),
        label = "hintScale"
    )

    Card(
        modifier = modifier.scale(scale),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC141823)),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                color = Color(0xFFE7ECF8),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "تأكد من مسح يمين/يسار وأسفل الغرفة.",
                color = accent,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * شارات إنجاز أسفل (يمين/يسار/أسفل) بتصميم مضغوط
 */
@Composable
private fun BottomChips(
    coverage: CoverageState,
    modifier: Modifier = Modifier
) {
    val rightDone = coverage.yawCoveragePercent >= 0.33f
    val leftDone  = coverage.yawCoveragePercent >= 0.66f
    val downDone  = coverage.pitchDownReached

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(18.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Chip(text = "يمين", done = rightDone)
        Spacer(Modifier.width(8.dp))
        Chip(text = "يسار", done = leftDone)
        Spacer(Modifier.width(8.dp))
        Chip(text = "أسفل", done = downDone)
    }
}

@Composable
private fun Chip(text: String, done: Boolean) {
    val bg = if (done) Color(0x2538D873) else Color(0x203A4256)
    val fg = if (done) Color(0xFF7DE28F) else Color(0xFFB8C1D1)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (done) Color(0xFF39D98A) else Color(0xFF7A869A))
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/* ===================== مساعدات ===================== */

private fun totalProgress(c: CoverageState): Float {
    // وزن أفقي أقوى، وأسفل كشرط ثنائي
    val yawW = c.yawCoveragePercent.coerceIn(0f, 1f) * 0.7f
    val downW = if (c.pitchDownReached) 0.3f else 0f
    return (yawW + downW).coerceIn(0f, 1f)
}

/* ===================== معاينات ===================== */

@Composable
private fun PreviewScaffold(content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0F111A)) { content() }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Overlay • بداية المسح",
    showBackground = true,
    backgroundColor = 0xFF0F111A,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun Preview_InExamRoomScanOverlay_Initial() {
    PreviewScaffold {
        InExamRoomScanOverlay(
            state = InExamScanUiState(
                visible = true,
                durationMs = 5_000L,
                coverage = CoverageState(
                    yawCoveragePercent = 0.0f,
                    pitchCoveragePercent = 0.0f,
                    pitchUpReached = false,
                    pitchDownReached = false
                )
            )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Overlay • يمين + أسفل مكتملة",
    showBackground = true,
    backgroundColor = 0xFF0F111A,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun Preview_InExamRoomScanOverlay_RightAndDownDone() {
    PreviewScaffold {
        InExamRoomScanOverlay(
            state = InExamScanUiState(
                visible = true,
                durationMs = 14_500L,
                coverage = CoverageState(
                    yawCoveragePercent = 0.45f,
                    pitchCoveragePercent = 0.5f,
                    pitchUpReached = false,
                    pitchDownReached = true
                )
            )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Overlay • مكتمل تقريباً (يسار متبقّي قليل)",
    showBackground = true,
    backgroundColor = 0xFF0F111A,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun Preview_InExamRoomScanOverlay_AlmostDone() {
    PreviewScaffold {
        InExamRoomScanOverlay(
            state = InExamScanUiState(
                visible = true,
                durationMs = 27_000L,
                coverage = CoverageState(
                    yawCoveragePercent = 0.68f,
                    pitchCoveragePercent = 1.0f,
                    pitchUpReached = true,
                    pitchDownReached = true
                )
            )
        )
    }
}
