package com.example.saffieduapp.presentation.screens.student.exam_screen.camera.front

import android.util.Log
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.CameraMonitoringConfig
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 🎯 نظام الأولويات الذكي لالتقاط الصور
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/front/SnapshotPrioritySystem.kt
 *
 * 🎯 الهدف:
 * تقييم متى وكيف نلتقط صور من الكاميرا الأمامية
 *
 * 🚦 الأولويات الثلاثة:
 * 🔴 P0 (Critical): فوري - 0 ثانية (وجوه متعددة، لا يوجد وجه)
 * 🟡 P1 (High): خلال 10 ثواني - 30 ثانية cooldown (ينظر بعيداً)
 * 🟢 P2 (Normal): عند الفرصة - 5 دقائق cooldown (فحص دوري)
 *
 * 📊 الاستراتيجية التكيفية:
 * - صور 0-5: جميع الأولويات نشطة
 * - صور 6-8: P0 + P1 فقط
 * - صور 9-10: P0 فقط (Critical)
 */
class SnapshotPrioritySystem {
    private val TAG = "PrioritySystem"

    // حالة النظام
    private val _state = MutableStateFlow(PrioritySystemState())
    val state: StateFlow<PrioritySystemState> = _state.asStateFlow()

    // عدادات الانتهاكات
    private var multipleFacesCount = 0
    private var noFaceCount = 0
    private var lookingAwayCount = 0

    // آخر وقت التقاط لكل نوع
    private val lastCaptureTime = mutableMapOf<SnapshotReason, Long>()

    // ═══════════════════════════════════════════
    // 📊 حالة النظام
    // ═══════════════════════════════════════════

    data class PrioritySystemState(
        val snapshotsTaken: Int = 0,
        val snapshotsRemaining: Int = CameraMonitoringConfig.FrontCamera.MAX_SNAPSHOTS,
        val currentActivePriorities: Set<SnapshotPriority> = setOf(
            SnapshotPriority.CRITICAL,
            SnapshotPriority.HIGH,
            SnapshotPriority.NORMAL
        ),
        val canCapture: Boolean = true,
        val violationCounts: Map<ViolationType, Int> = emptyMap(),
        val shouldAutoSubmit: Boolean = false
    )

    // ═══════════════════════════════════════════
    // ⚖️ تقييم الأولوية
    // ═══════════════════════════════════════════

    /**
     * تقييم ما إذا كان يجب التقاط صورة
     *
     * يتحقق من:
     * 1. الحد الأقصى للصور (10)
     * 2. الأولوية نشطة
     * 3. Cooldown انتهى
     * 4. نوع الانتهاك
     *
     * @param reason سبب الالتقاط
     * @return قرار الالتقاط (موافق أو مرفوض)
     */
    fun evaluateCapture(reason: SnapshotReason): CaptureDecision {
        val currentState = _state.value

        // 1️⃣ التحقق من الحد الأقصى
        if (currentState.snapshotsTaken >= CameraMonitoringConfig.FrontCamera.MAX_SNAPSHOTS) {
            return CaptureDecision.Rejected(
                reason = "تم الوصول للحد الأقصى من الصور (10)",
                shouldLog = true
            )
        }

        // 2️⃣ تحديد الأولوية
        val priority = determinePriority(reason)

        // 3️⃣ التحقق من أن الأولوية نشطة
        if (priority !in currentState.currentActivePriorities) {
            return CaptureDecision.Rejected(
                reason = "الأولوية ${priority.arabicName} غير نشطة (عدد الصور: ${currentState.snapshotsTaken})",
                shouldLog = false
            )
        }

        // 4️⃣ التحقق من cooldown
        if (!checkCooldown(reason, priority)) {
            val remaining = getRemainingCooldown(reason, priority)
            return CaptureDecision.Rejected(
                reason = "فترة الانتظار لم تنته (متبقي: ${remaining / 1000}s)",
                shouldLog = false
            )
        }

        // 5️⃣ تحديد نوع الانتهاك
        val violationType = mapReasonToViolation(reason)

        // 6️⃣ تقييم الإجراء المطلوب
        val violationAction = evaluateViolationAction(violationType, priority)

        // 7️⃣ تحديد التحذير
        val shouldShowWarning = shouldShowWarning(violationType, priority)

        // ✅ موافقة على الالتقاط
        return CaptureDecision.Approved(
            priority = priority,
            violationType = violationType,
            action = violationAction,
            shouldShowWarning = shouldShowWarning
        )
    }

    /**
     * تحديد الأولوية بناءً على السبب
     *
     * القواعد:
     * - وجوه متعددة / لا وجه → P0 (فوري)
     * - ينظر بعيداً / مسافة خاطئة → P1 (30 ثانية)
     * - فحص دوري → P2 (5 دقائق)
     */
    private fun determinePriority(reason: SnapshotReason): SnapshotPriority {
        return when (reason) {
            // 🔴 P0 - Critical: فوري
            SnapshotReason.MULTIPLE_FACES,
            SnapshotReason.NO_FACE -> SnapshotPriority.CRITICAL

            // 🟡 P1 - High: خلال 10 ثواني
            SnapshotReason.LOOKING_AWAY,
            SnapshotReason.FACE_TOO_FAR,
            SnapshotReason.FACE_TOO_CLOSE -> SnapshotPriority.HIGH

            // 🟢 P2 - Normal: عند الفرصة
            SnapshotReason.PERIODIC_CHECK,
            SnapshotReason.RANDOM_VERIFICATION -> SnapshotPriority.NORMAL
        }
    }

    /**
     * التحقق من cooldown
     *
     * @return true إذا انتهت فترة الانتظار
     */
    private fun checkCooldown(reason: SnapshotReason, priority: SnapshotPriority): Boolean {
        val lastTime = lastCaptureTime[reason] ?: 0
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastTime

        val requiredCooldown = when (priority) {
            SnapshotPriority.CRITICAL -> CameraMonitoringConfig.FrontCamera.COOLDOWN_CRITICAL // 0
            SnapshotPriority.HIGH -> CameraMonitoringConfig.FrontCamera.COOLDOWN_HIGH // 30s
            SnapshotPriority.NORMAL -> CameraMonitoringConfig.FrontCamera.COOLDOWN_NORMAL // 5min
        }

        return elapsed >= requiredCooldown
    }

    /**
     * الحصول على الوقت المتبقي في cooldown
     */
    private fun getRemainingCooldown(reason: SnapshotReason, priority: SnapshotPriority): Long {
        val lastTime = lastCaptureTime[reason] ?: 0
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastTime

        val requiredCooldown = when (priority) {
            SnapshotPriority.CRITICAL -> 0L
            SnapshotPriority.HIGH -> CameraMonitoringConfig.FrontCamera.COOLDOWN_HIGH
            SnapshotPriority.NORMAL -> CameraMonitoringConfig.FrontCamera.COOLDOWN_NORMAL
        }

        return (requiredCooldown - elapsed).coerceAtLeast(0)
    }

    /**
     * ربط السبب بنوع الانتهاك
     */
    private fun mapReasonToViolation(reason: SnapshotReason): ViolationType? {
        return when (reason) {
            SnapshotReason.MULTIPLE_FACES -> ViolationType.MULTIPLE_FACES
            SnapshotReason.NO_FACE -> ViolationType.NO_FACE_DETECTED
            SnapshotReason.LOOKING_AWAY -> ViolationType.LOOKING_AWAY
            SnapshotReason.FACE_TOO_FAR,
            SnapshotReason.FACE_TOO_CLOSE -> ViolationType.FACE_DISTANCE
            else -> null // الفحص الدوري ليس انتهاكاً
        }
    }

    /**
     * تقييم الإجراء المطلوب للانتهاك
     *
     * القواعد:
     * - وجوه متعددة:
     *   - 1: التقاط
     *   - 2: تحذير
     *   - 3+: تسليم تلقائي ❌
     *
     * - لا يوجد وجه:
     *   - 1-2: التقاط
     *   - 3-4: تحذير
     *   - 5+: تسليم تلقائي ❌
     *
     * - ينظر بعيداً:
     *   - 1-4: تسجيل فقط
     *   - 5+: تحذير
     */
    private fun evaluateViolationAction(
        violationType: ViolationType?,
        priority: SnapshotPriority
    ): ViolationAction {
        if (violationType == null) {
            return ViolationAction.SNAPSHOT_CAPTURED
        }

        return when (violationType) {
            ViolationType.MULTIPLE_FACES -> {
                multipleFacesCount++
                when {
                    multipleFacesCount >= 3 -> ViolationAction.AUTO_SUBMITTED
                    multipleFacesCount >= 2 -> ViolationAction.WARNING_SHOWN
                    else -> ViolationAction.SNAPSHOT_CAPTURED
                }
            }

            ViolationType.NO_FACE_DETECTED -> {
                noFaceCount++
                when {
                    noFaceCount >= CameraMonitoringConfig.FrontCamera.MAX_NO_FACE_WARNINGS ->
                        ViolationAction.AUTO_SUBMITTED
                    noFaceCount >= 3 -> ViolationAction.WARNING_SHOWN
                    else -> ViolationAction.SNAPSHOT_CAPTURED
                }
            }

            ViolationType.LOOKING_AWAY -> {
                lookingAwayCount++
                when {
                    lookingAwayCount >= CameraMonitoringConfig.FrontCamera.LOOKING_AWAY_WARNING_COUNT ->
                        ViolationAction.WARNING_SHOWN
                    else -> ViolationAction.LOGGED
                }
            }

            ViolationType.FACE_DISTANCE -> ViolationAction.LOGGED
        }
    }

    /**
     * تحديد ما إذا كان يجب إظهار تحذير
     */
    private fun shouldShowWarning(violationType: ViolationType?, priority: SnapshotPriority): Boolean {
        if (violationType == null) return false
        if (priority != SnapshotPriority.CRITICAL) return false

        return when (violationType) {
            ViolationType.MULTIPLE_FACES -> multipleFacesCount >= 1
            ViolationType.NO_FACE_DETECTED -> noFaceCount >= 3
            else -> false
        }
    }

    // ═══════════════════════════════════════════
    // 📝 تسجيل الالتقاط
    // ═══════════════════════════════════════════

    /**
     * تسجيل أنه تم التقاط صورة
     *
     * يقوم بـ:
     * 1. تحديث وقت الالتقاط الأخير
     * 2. زيادة العداد
     * 3. تحديث الأولويات النشطة
     * 4. التحقق من شروط التسليم التلقائي
     */
    fun recordCapture(reason: SnapshotReason, violationType: ViolationType?) {
        val currentState = _state.value

        // 1️⃣ تحديث وقت الالتقاط الأخير
        lastCaptureTime[reason] = System.currentTimeMillis()

        // 2️⃣ تحديث العداد
        val newCount = currentState.snapshotsTaken + 1
        val remaining = CameraMonitoringConfig.FrontCamera.MAX_SNAPSHOTS - newCount

        // 3️⃣ تحديث عدادات الانتهاكات
        val newViolationCounts = currentState.violationCounts.toMutableMap()
        if (violationType != null) {
            newViolationCounts[violationType] = (newViolationCounts[violationType] ?: 0) + 1
        }

        // 4️⃣ تحديث الأولويات النشطة بناءً على العدد
        val activePriorities = determineActivePriorities(newCount)

        // 5️⃣ التحقق من التسليم التلقائي
        val shouldAutoSubmit = checkAutoSubmitConditions(newViolationCounts)

        // 6️⃣ تحديث الحالة
        _state.value = PrioritySystemState(
            snapshotsTaken = newCount,
            snapshotsRemaining = remaining,
            currentActivePriorities = activePriorities,
            canCapture = remaining > 0,
            violationCounts = newViolationCounts,
            shouldAutoSubmit = shouldAutoSubmit
        )

        // 7️⃣ Log
        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "📸 Snapshot recorded!")
            Log.d(TAG, "   Reason: ${reason.arabicName}")
            Log.d(TAG, "   Count: $newCount / ${CameraMonitoringConfig.FrontCamera.MAX_SNAPSHOTS}")
            Log.d(TAG, "   Active priorities: ${activePriorities.map { it.arabicName }}")
            if (shouldAutoSubmit) {
                Log.w(TAG, "⚠️ AUTO SUBMIT CONDITIONS MET!")
            }
        }
    }

    /**
     * تحديد الأولويات النشطة بناءً على عدد الصور
     *
     * الاستراتيجية التكيفية:
     * - 0-5 صور: جميع الأولويات (P0 + P1 + P2)
     * - 6-8 صور: P0 + P1 فقط
     * - 9-10 صور: P0 فقط (Critical)
     */
    private fun determineActivePriorities(count: Int): Set<SnapshotPriority> {
        return when {
            count < 6 -> setOf(
                SnapshotPriority.CRITICAL,
                SnapshotPriority.HIGH,
                SnapshotPriority.NORMAL
            )
            count < 9 -> setOf(
                SnapshotPriority.CRITICAL,
                SnapshotPriority.HIGH
            )
            else -> setOf(SnapshotPriority.CRITICAL)
        }
    }

    /**
     * التحقق من شروط التسليم التلقائي
     *
     * يحدث التسليم التلقائي إذا:
     * - وجوه متعددة ≥ 3 مرات
     * - أو لا يوجد وجه ≥ 5 مرات
     */
    private fun checkAutoSubmitConditions(violations: Map<ViolationType, Int>): Boolean {
        val multipleFaces = violations[ViolationType.MULTIPLE_FACES] ?: 0
        val noFace = violations[ViolationType.NO_FACE_DETECTED] ?: 0

        return multipleFaces >= 3 || noFace >= CameraMonitoringConfig.FrontCamera.MAX_NO_FACE_WARNINGS
    }

    // ═══════════════════════════════════════════
    // 🔄 إعادة التعيين
    // ═══════════════════════════════════════════

    /**
     * إعادة تعيين كل شيء
     */
    fun reset() {
        multipleFacesCount = 0
        noFaceCount = 0
        lookingAwayCount = 0
        lastCaptureTime.clear()

        _state.value = PrioritySystemState()

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "🔄 Priority system reset")
        }
    }

    /**
     * الحصول على معلومات النظام
     */
    fun getSystemInfo(): String {
        val state = _state.value
        return buildString {
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🎯 Priority System Info:")
            appendLine("   Snapshots: ${state.snapshotsTaken} / ${CameraMonitoringConfig.FrontCamera.MAX_SNAPSHOTS}")
            appendLine("   Remaining: ${state.snapshotsRemaining}")
            appendLine("   Active Priorities: ${state.currentActivePriorities.map { it.arabicName }}")
            appendLine("   Can Capture: ${state.canCapture}")
            appendLine("   Violations:")
            state.violationCounts.forEach { (type, count) ->
                appendLine("     - ${type.arabicName}: $count")
            }
            appendLine("   Auto Submit: ${state.shouldAutoSubmit}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }
}

// ═══════════════════════════════════════════
// 📊 قرار الالتقاط
// ═══════════════════════════════════════════

/**
 * نتيجة تقييم الالتقاط
 */
sealed class CaptureDecision {
    /**
     * موافق على الالتقاط ✅
     */
    data class Approved(
        val priority: SnapshotPriority,
        val violationType: ViolationType?,
        val action: ViolationAction,
        val shouldShowWarning: Boolean
    ) : CaptureDecision()

    /**
     * مرفوض ❌
     */
    data class Rejected(
        val reason: String,
        val shouldLog: Boolean
    ) : CaptureDecision()
}