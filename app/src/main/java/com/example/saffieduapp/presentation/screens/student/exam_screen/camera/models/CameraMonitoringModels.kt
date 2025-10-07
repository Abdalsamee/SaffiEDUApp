package com.example.saffieduapp.presentation.screens.student.exam_screen.camera.models

import java.util.Date

/**
 * 📦 نماذج البيانات لنظام المراقبة بالكاميرا
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/models/CameraMonitoringModels.kt
 *
 * 🎯 الهدف:
 * تنظيم جميع البيانات في "صناديق" واضحة ومرتبة
 */

// ═══════════════════════════════════════════
// 📹 تسجيل الكاميرا الخلفية
// ═══════════════════════════════════════════

/**
 * معلومات تسجيل الكاميرا الخلفية (مسح الغرفة)
 *
 * مثل "شهادة ميلاد" الفيديو - كل المعلومات عنه
 */
data class BackCameraRecording(
    val id: String,                        // معرف فريد للفيديو
    val sessionId: String,                 // معرف جلسة الاختبار
    val examId: String,                    // معرف الاختبار
    val studentId: String,                 // معرف الطالب
    val filePath: String,                  // مسار الملف الأصلي
    val encryptedFilePath: String,         // مسار الملف المشفر
    val fileSize: Long,                    // حجم الملف بالبايت
    val duration: Long,                    // مدة التسجيل بالميلي ثانية
    val recordedAt: Date,                  // متى تم التسجيل؟
    val scheduledAt: Long,                 // الوقت المجدول من بداية الاختبار
    val uploadStatus: UploadStatus = UploadStatus.PENDING,  // حالة الرفع
    val uploadedAt: Date? = null,          // متى تم الرفع؟
    val compressionRatio: Float = 1.0f,    // نسبة الضغط
    val metadata: VideoMetadata            // معلومات إضافية
)

/**
 * معلومات تقنية عن الفيديو
 */
data class VideoMetadata(
    val width: Int,                        // العرض
    val height: Int,                       // الارتفاع
    val bitrate: Int,                      // معدل البت
    val fps: Int,                          // عدد الإطارات في الثانية
    val codec: String,                     // نوع الترميز
    val hasAudio: Boolean,                 // هل يحتوي على صوت؟
    val deviceModel: String,               // موديل الهاتف
    val deviceOrientation: Int             // اتجاه الجهاز
)

// ═══════════════════════════════════════════
// 📸 التقاط الكاميرا الأمامية
// ═══════════════════════════════════════════

/**
 * معلومات التقاط صورة من الكاميرا الأمامية
 *
 * مثل "بطاقة هوية" للصورة
 */
data class FrontCameraSnapshot(
    val id: String,                        // معرف فريد للصورة
    val sessionId: String,                 // معرف جلسة الاختبار
    val examId: String,                    // معرف الاختبار
    val studentId: String,                 // معرف الطالب
    val filePath: String,                  // مسار الملف الأصلي
    val encryptedFilePath: String,         // مسار الملف المشفر
    val fileSize: Long,                    // حجم الملف بالبايت
    val capturedAt: Date,                  // متى تم الالتقاط؟
    val reason: SnapshotReason,            // لماذا تم الالتقاط؟
    val priority: SnapshotPriority,        // ما الأولوية؟
    val violationType: ViolationType? = null,  // نوع الانتهاك (إن وجد)
    val uploadStatus: UploadStatus = UploadStatus.PENDING,
    val uploadedAt: Date? = null,
    val metadata: SnapshotMetadata         // معلومات إضافية
)

/**
 * معلومات تقنية عن الصورة
 */
data class SnapshotMetadata(
    val width: Int,                        // العرض
    val height: Int,                       // الارتفاع
    val quality: Int,                      // الجودة
    val faceDetected: Boolean,             // هل تم كشف وجه؟
    val faceCount: Int,                    // عدد الوجوه
    val faceConfidence: Float? = null,     // مستوى الثقة في الكشف
    val lookingAway: Boolean = false,      // هل ينظر بعيداً؟
    val deviceOrientation: Int             // اتجاه الجهاز
)

// ═══════════════════════════════════════════
// 🏷️ التعدادات (Enums)
// ═══════════════════════════════════════════

/**
 * سبب التقاط الصورة
 */
enum class SnapshotReason(val arabicName: String) {
    MULTIPLE_FACES("وجوه متعددة"),              // 👥 أكثر من وجه
    NO_FACE("عدم وجود وجه"),                   // ❌ لا يوجد وجه
    LOOKING_AWAY("النظر بعيداً"),              // 👀 ينظر بعيداً
    FACE_TOO_FAR("الوجه بعيد جداً"),           // 📏 بعيد عن الكاميرا
    FACE_TOO_CLOSE("الوجه قريب جداً"),         // 📏 قريب جداً
    PERIODIC_CHECK("فحص دوري"),               // ⏰ فحص روتيني
    RANDOM_VERIFICATION("تحقق عشوائي")        // 🎲 عشوائي
}

/**
 * أولوية الالتقاط
 */
enum class SnapshotPriority(val level: Int, val arabicName: String) {
    CRITICAL(0, "حرج"),      // 🔴 P0 - فوري (0 ثانية)
    HIGH(1, "عالي"),         // 🟡 P1 - خلال 10 ثواني (30 ثانية cooldown)
    NORMAL(2, "عادي")        // 🟢 P2 - عند الفرصة (5 دقائق cooldown)
}

/**
 * نوع الانتهاك
 */
enum class ViolationType(val arabicName: String, val severity: Int) {
    MULTIPLE_FACES("وجوه متعددة", 3),          // الأخطر
    NO_FACE_DETECTED("عدم كشف وجه", 2),        // خطير
    LOOKING_AWAY("النظر بعيداً", 1),          // متوسط
    FACE_DISTANCE("مسافة غير مناسبة", 1)      // متوسط
}

/**
 * حالة الرفع
 */
enum class UploadStatus {
    PENDING,        // ⏳ في الانتظار
    IN_PROGRESS,    // 🔄 جاري الرفع
    SUCCESS,        // ✅ تم بنجاح
    FAILED,         // ❌ فشل
    RETRYING        // 🔁 إعادة المحاولة
}

// ═══════════════════════════════════════════
// 📊 جلسة المراقبة
// ═══════════════════════════════════════════

/**
 * جلسة المراقبة الكاملة
 *
 * مثل "ملف الطالب" - كل شيء عن الاختبار
 */
data class MonitoringSession(
    val sessionId: String,                                  // معرف الجلسة
    val examId: String,                                     // معرف الاختبار
    val studentId: String,                                  // معرف الطالب
    val startedAt: Date,                                    // وقت البداية
    val examDuration: Long,                                 // مدة الاختبار
    val backCameraRecording: BackCameraRecording? = null,   // تسجيل الكاميرا الخلفية
    val frontCameraSnapshots: MutableList<FrontCameraSnapshot> = mutableListOf(),  // صور الكاميرا الأمامية
    val violations: MutableList<ViolationEvent> = mutableListOf(),  // الانتهاكات
    val metrics: SessionMetrics = SessionMetrics()          // المقاييس
)

/**
 * حدث انتهاك
 */
data class ViolationEvent(
    val id: String,                        // معرف فريد
    val timestamp: Date,                   // متى حدث؟
    val type: ViolationType,               // نوع الانتهاك
    val priority: SnapshotPriority,        // الأولوية
    val description: String,               // وصف
    val actionTaken: ViolationAction,      // الإجراء المتخذ
    val snapshotId: String? = null         // معرف الصورة (إن وجد)
)

/**
 * الإجراء المتخذ عند الانتهاك
 */
enum class ViolationAction {
    LOGGED,              // 📝 تم التسجيل فقط
    WARNING_SHOWN,       // ⚠️ تم عرض تحذير
    SNAPSHOT_CAPTURED,   // 📸 تم التقاط صورة
    EXAM_PAUSED,         // ⏸️ تم إيقاف الاختبار مؤقتاً
    AUTO_SUBMITTED       // 🔒 تم التسليم التلقائي
}

// ═══════════════════════════════════════════
// 📈 إحصائيات الجلسة
// ═══════════════════════════════════════════

/**
 * مقاييس الأداء والإحصائيات
 *
 * مثل "تقرير نهائي" عن الجلسة
 */
data class SessionMetrics(
    // 📹 معلومات الكاميرا الخلفية
    var backCameraVideoRecorded: Boolean = false,
    var backCameraVideoSize: Long = 0,
    var backCameraRecordingTime: Long = 0,
    var videoUploadStatus: UploadStatus = UploadStatus.PENDING,
    var videoUploadDuration: Long = 0,

    // 📸 معلومات الكاميرا الأمامية
    var snapshotsCaptured: Int = 0,
    var snapshotReasons: MutableMap<SnapshotReason, Int> = mutableMapOf(),
    var snapshotsUploadStatus: MutableMap<String, UploadStatus> = mutableMapOf(),

    // ⚠️ معلومات الانتهاكات
    var violationsLogged: Int = 0,
    var violationsByType: MutableMap<ViolationType, Int> = mutableMapOf(),
    var criticalViolations: Int = 0,

    // ⏱️ معلومات التوقيت
    var examDuration: Long = 0,
    var recordingPauseDuration: Long = 0,
    var totalInterruptions: Int = 0,

    // ⚡ مقاييس الأداء
    var cameraInitTime: Long = 0,
    var recordingStartDelay: Long = 0,
    var videoCompressionTime: Long = 0,
    var uploadSpeed: Float = 0f,
    var batteryUsage: Float = 0f,
    var storageUsed: Long = 0,

    // ⭐ مقاييس الجودة
    var videoActualSize: Long = 0,
    var videoQualityScore: Int = 0,
    var uploadSuccessRate: Float = 0f,
    var userCompletionRate: Float = 0f
)

// ═══════════════════════════════════════════
// 🎬 حالة نظام المراقبة
// ═══════════════════════════════════════════

/**
 * الحالة الحالية لنظام المراقبة
 */
data class MonitoringState(
    val isBackCameraRecording: Boolean = false,     // هل الكاميرا الخلفية تسجل؟
    val isFrontCameraMonitoring: Boolean = false,   // هل الكاميرا الأمامية تراقب؟
    val snapshotsRemaining: Int = 10,               // عدد الصور المتبقية
    val currentPriority: SnapshotPriority = SnapshotPriority.NORMAL,
    val lastSnapshotTime: Long = 0,                 // آخر وقت التقاط
    val canCaptureSnapshot: Boolean = true,         // هل يمكن الالتقاط؟
    val violationCount: Map<ViolationType, Int> = emptyMap(),
    val warnings: List<String> = emptyList()
)

// ═══════════════════════════════════════════
// 📡 أحداث المراقبة
// ═══════════════════════════════════════════

/**
 * الأحداث التي تحدث أثناء المراقبة
 */
sealed class MonitoringEvent {
    // 📹 أحداث الكاميرا الخلفية
    data class BackCameraScheduled(val scheduledTime: Long) : MonitoringEvent()
    data object BackCameraStarting : MonitoringEvent()
    data class BackCameraRecording(val progress: Float) : MonitoringEvent()
    data class BackCameraCompleted(val recording: com.example.saffieduapp.presentation.screens.student.exam_screen.camera.models.BackCameraRecording) : MonitoringEvent()
    data class BackCameraError(val error: String) : MonitoringEvent()

    // 📸 أحداث الكاميرا الأمامية
    data class SnapshotCaptured(val snapshot: FrontCameraSnapshot) : MonitoringEvent()
    data class ViolationDetected(val violation: ViolationEvent) : MonitoringEvent()
    data class SnapshotLimitReached(val count: Int) : MonitoringEvent()

    // 📤 أحداث الرفع
    data class UploadStarted(val fileId: String, val type: String) : MonitoringEvent()
    data class UploadProgress(val fileId: String, val progress: Float) : MonitoringEvent()
    data class UploadCompleted(val fileId: String) : MonitoringEvent()
    data class UploadFailed(val fileId: String, val error: String) : MonitoringEvent()

    // 🎯 أحداث عامة
    data class SessionStarted(val session: MonitoringSession) : MonitoringEvent()
    data class SessionEnded(val session: MonitoringSession) : MonitoringEvent()
}