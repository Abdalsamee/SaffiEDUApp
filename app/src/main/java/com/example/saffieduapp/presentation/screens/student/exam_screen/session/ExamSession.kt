package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import java.util.Date

/**
 * جلسة الاختبار - تحتوي على جميع بيانات الجلسة
 */
data class ExamSession(
    val sessionId: String,
    val examId: String,
    val studentId: String,
    val startTime: Long,
    var endTime: Long? = null,
    val snapshots: MutableList<MediaSnapshot> = mutableListOf(),
    val backCameraVideo: MediaVideo? = null,
    val violations: MutableList<ViolationRecord> = mutableListOf(),
    val securityEvents: MutableList<SecurityEvent> = mutableListOf(),
    var status: SessionStatus = SessionStatus.ACTIVE
) {
    fun getDuration(): Long {
        return (endTime ?: System.currentTimeMillis()) - startTime
    }

    fun addSnapshot(snapshot: MediaSnapshot) {
        if (snapshots.size < MAX_SNAPSHOTS) {
            snapshots.add(snapshot)
        }
    }

    fun getTotalViolations(): Int = violations.size

    companion object {
        const val MAX_SNAPSHOTS = 10
    }
}

/**
 * لقطة من الكاميرا
 */
data class MediaSnapshot(
    val id: String,
    val timestamp: Long,
    val encryptedFilePath: String,
    val reason: SnapshotReason,
    var uploadUrl: String? = null,
    var uploadStatus: UploadStatus = UploadStatus.PENDING
)

/**
 * فيديو الكاميرا الخلفية
 */
data class MediaVideo(
    val id: String,
    val timestamp: Long,
    val encryptedFilePath: String,
    val duration: Long, // بالميلي ثانية
    var uploadUrl: String? = null,
    var uploadStatus: UploadStatus = UploadStatus.PENDING
)

/**
 * سجل مخالفة
 */
data class ViolationRecord(
    val type: String,
    val timestamp: Long,
    val description: String,
    val snapshotId: String? = null
)

/**
 * حدث أمني
 */
data class SecurityEvent(
    val type: SecurityEventType,
    val timestamp: Long,
    val details: String
)

/**
 * سبب التقاط الصورة
 */
enum class SnapshotReason {
    NO_FACE_DETECTED,
    MULTIPLE_FACES,
    LOOKING_AWAY,
    MANUAL_CAPTURE,
    PERIODIC_CHECK
}

/**
 * حالة الرفع
 */
enum class UploadStatus {
    PENDING,
    UPLOADING,
    SUCCESS,
    FAILED,
    RETRYING
}

/**
 * حالة الجلسة
 */
enum class SessionStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    TERMINATED,
    UPLOADED
}

/**
 * نوع الحدث الأمني
 */
enum class SecurityEventType {
    EXAM_STARTED,
    EXAM_PAUSED,
    EXAM_RESUMED,
    EXAM_SUBMITTED,
    EXAM_TERMINATED,
    APP_BACKGROUNDED,
    APP_FOREGROUNDED,
    SCREENSHOT_ATTEMPT,
    OVERLAY_DETECTED,
    EXTERNAL_DISPLAY,
    MULTI_WINDOW,
    SNAPSHOT_CAPTURED,
    VIDEO_RECORDED,
    UPLOAD_STARTED,
    UPLOAD_COMPLETED
}