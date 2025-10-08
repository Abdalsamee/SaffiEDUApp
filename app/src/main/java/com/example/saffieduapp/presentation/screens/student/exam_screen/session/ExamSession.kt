package com.example.saffieduapp.presentation.screens.student.exam_screen.session

/**
 * نموذج جلسة الاختبار - يحتوي على جميع بيانات الجلسة
 */
data class ExamSession(
    val sessionId: String,
    val examId: String,
    val studentId: String,
    val startTime: Long,
    var endTime: Long? = null,
    var status: SessionStatus,
    val snapshots: MutableList<SnapshotRecord> = mutableListOf(),
    val violations: MutableList<ViolationRecord> = mutableListOf(),
    val securityEvents: MutableList<SecurityEvent> = mutableListOf(),
    var backCameraVideo: BackCameraVideo? = null
) {
    companion object {
        const val MAX_SNAPSHOTS = 10
    }

    /**
     * حساب مدة الجلسة
     */
    fun getDuration(): Long {
        return (endTime ?: System.currentTimeMillis()) - startTime
    }

    /**
     * إضافة snapshot
     */
    fun addSnapshot(snapshot: SnapshotRecord) {
        if (snapshots.size < MAX_SNAPSHOTS) {
            snapshots.add(snapshot)
        }
    }
}

/**
 * سجل الـ Snapshot
 */
data class SnapshotRecord(
    val id: String,
    val timestamp: Long,
    val reason: SnapshotReason,
    val filePath: String,
    val fileSize: Long
)

/**
 * سجل المخالفة
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