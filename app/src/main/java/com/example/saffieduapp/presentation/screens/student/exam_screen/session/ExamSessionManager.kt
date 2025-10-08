package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID
import javax.crypto.SecretKey

/**
 * مدير جلسة الاختبار - يدير البيانات والوسائط
 */
class ExamSessionManager(
    private val context: Context,
    private val examId: String,
    private val studentId: String
) {
    private val TAG = "ExamSessionManager"

    private val gson = Gson()

    // مفتاح التشفير للجلسة
    private val encryptionKey: SecretKey = EncryptionHelper.generateKey()

    // MediaStorage
    private val mediaStorage = MediaStorage(context, encryptionKey)

    // الجلسة الحالية
    private var currentSession: ExamSession? = null

    private val _sessionState = MutableStateFlow<ExamSession?>(null)
    val sessionState: StateFlow<ExamSession?> = _sessionState.asStateFlow()

    /**
     * بدء جلسة جديدة
     */
    fun startSession(): ExamSession {
        Log.d(TAG, "Starting new exam session")

        val session = ExamSession(
            sessionId = UUID.randomUUID().toString(),
            examId = examId,
            studentId = studentId,
            startTime = System.currentTimeMillis(),
            status = SessionStatus.ACTIVE
        )

        currentSession = session
        _sessionState.value = session

        saveSession()
        logSecurityEvent(SecurityEventType.EXAM_STARTED, "Exam session started")

        Log.d(TAG, "✅ Session started: ${session.sessionId}")
        return session
    }

    /**
     * إنهاء الجلسة
     */
    fun endSession() {
        currentSession?.let { session ->
            session.endTime = System.currentTimeMillis()
            session.status = SessionStatus.COMPLETED

            logSecurityEvent(SecurityEventType.EXAM_SUBMITTED, "Exam submitted")
            saveSession()

            Log.d(TAG, "✅ Session ended: ${session.sessionId}, Duration: ${session.getDuration()}ms")
        }
    }

    /**
     * إيقاف مؤقت
     */
    fun pauseSession() {
        currentSession?.let { session ->
            session.status = SessionStatus.PAUSED
            logSecurityEvent(SecurityEventType.EXAM_PAUSED, "Exam paused")
            saveSession()
        }
    }

    /**
     * استئناف
     */
    fun resumeSession() {
        currentSession?.let { session ->
            session.status = SessionStatus.ACTIVE
            logSecurityEvent(SecurityEventType.EXAM_RESUMED, "Exam resumed")
            saveSession()
        }
    }

    /**
     * إنهاء قسري
     */
    fun terminateSession(reason: String) {
        currentSession?.let { session ->
            session.endTime = System.currentTimeMillis()
            session.status = SessionStatus.TERMINATED

            logSecurityEvent(SecurityEventType.EXAM_TERMINATED, "Exam terminated: $reason")
            saveSession()

            Log.w(TAG, "⚠️ Session terminated: $reason")
        }
    }

    /**
     * حفظ snapshot من الكاميرا الأمامية
     */
    fun saveSnapshot(
        imageData: ImageData,
        reason: SnapshotReason
    ): Boolean {
        val session = currentSession ?: run {
            Log.e(TAG, "No active session")
            return false
        }

        if (session.snapshots.size >= ExamSession.MAX_SNAPSHOTS) {
            Log.w(TAG, "⚠️ Max snapshots reached (${ExamSession.MAX_SNAPSHOTS})")
            return false
        }

        return try {
            val snapshot = mediaStorage.saveSnapshot(imageData, session.sessionId, reason)

            if (snapshot != null) {
                session.addSnapshot(snapshot)

                logSecurityEvent(
                    SecurityEventType.SNAPSHOT_CAPTURED,
                    "Snapshot captured: ${reason.name}, Total: ${session.snapshots.size}"
                )

                saveSession()
                _sessionState.value = session

                Log.d(TAG, "✅ Snapshot saved: ${snapshot.id}, Reason: ${reason.name}")
                return true
            }

            false

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving snapshot", e)
            false
        }
    }

    /**
     * حفظ فيديو الكاميرا الخلفية
     */
    fun saveBackCameraVideo(videoFile: File): Boolean {
        val session = currentSession ?: run {
            Log.e(TAG, "No active session")
            return false
        }

        return try {
            if (!videoFile.exists()) {
                Log.e(TAG, "Video file does not exist: ${videoFile.absolutePath}")
                return false
            }

            val savedVideoPath = mediaStorage.saveBackCameraVideo(videoFile, session.sessionId)

            if (savedVideoPath != null) {
                session.backCameraVideo = BackCameraVideo(
                    path = savedVideoPath,
                    timestamp = System.currentTimeMillis(),
                    duration = 30_000L,
                    fileSize = videoFile.length()
                )

                logSecurityEvent(
                    SecurityEventType.ROOM_SCAN_COMPLETED,
                    "Back camera video recorded: ${videoFile.name}"
                )

                saveSession()
                _sessionState.value = session

                Log.d(TAG, "✅ Back camera video saved: $savedVideoPath")
                return true
            }

            Log.e(TAG, "Failed to save video")
            false

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving back camera video", e)
            false
        }
    }

    /**
     * تسجيل مخالفة
     */
    fun logViolation(
        type: String,
        description: String,
        snapshotId: String? = null
    ) {
        currentSession?.let { session ->
            val violation = ViolationRecord(
                type = type,
                timestamp = System.currentTimeMillis(),
                description = description,
                snapshotId = snapshotId
            )

            session.violations.add(violation)
            saveSession()
            _sessionState.value = session

            Log.w(TAG, "⚠️ Violation logged: $type - $description")
        }
    }

    /**
     * تسجيل حدث أمني
     */
    fun logSecurityEvent(type: SecurityEventType, details: String) {
        currentSession?.let { session ->
            val event = SecurityEvent(
                type = type,
                timestamp = System.currentTimeMillis(),
                details = details
            )

            session.securityEvents.add(event)
            saveSession()
        }
    }

    /**
     * حفظ الجلسة
     */
    private fun saveSession() {
        currentSession?.let { session ->
            try {
                val sessionFile = getSessionFile(session.sessionId)
                val json = gson.toJson(session)
                val encryptedJson = EncryptionHelper.encryptString(json, encryptionKey)

                if (encryptedJson != null) {
                    sessionFile.writeText(encryptedJson)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to save session", e)
            }
        }
    }

    /**
     * تحميل جلسة
     */
    fun loadSession(sessionId: String): ExamSession? {
        return try {
            val sessionFile = getSessionFile(sessionId)

            if (!sessionFile.exists()) {
                Log.w(TAG, "Session file not found: $sessionId")
                return null
            }

            val encryptedJson = sessionFile.readText()
            val json = EncryptionHelper.decryptString(encryptedJson, encryptionKey)

            if (json != null) {
                val session = gson.fromJson(json, ExamSession::class.java)
                currentSession = session
                _sessionState.value = session
                Log.d(TAG, "✅ Session loaded: $sessionId")
                session
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to load session", e)
            null
        }
    }

    /**
     * حذف جلسة
     */
    fun deleteSession(sessionId: String) {
        try {
            val sessionFile = getSessionFile(sessionId)
            if (sessionFile.exists()) {
                sessionFile.delete()
            }

            mediaStorage.deleteSessionFiles(sessionId)

            Log.d(TAG, "✅ Session deleted: $sessionId")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete session", e)
        }
    }

    /**
     * الحصول على جميع الجلسات
     */
    fun getAllSessions(): List<ExamSession> {
        return try {
            val sessionsDir = getSessionsDir()
            val sessions = mutableListOf<ExamSession>()

            sessionsDir.listFiles()?.forEach { file ->
                if (file.extension == "json") {
                    val sessionId = file.nameWithoutExtension
                    loadSession(sessionId)?.let { sessions.add(it) }
                }
            }

            sessions

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all sessions", e)
            emptyList()
        }
    }

    fun getCurrentSession(): ExamSession? = currentSession

    fun canCaptureMoreSnapshots(): Boolean {
        val session = currentSession ?: return false
        return session.snapshots.size < ExamSession.MAX_SNAPSHOTS
    }

    fun getRemainingSnapshotsCount(): Int {
        val session = currentSession ?: return 0
        return ExamSession.MAX_SNAPSHOTS - session.snapshots.size
    }

    fun getEncryptionKeyString(): String {
        return EncryptionHelper.keyToString(encryptionKey)
    }

    private fun getSessionFile(sessionId: String): File {
        return File(getSessionsDir(), "$sessionId.json")
    }

    private fun getSessionsDir(): File {
        val dir = File(context.filesDir, "exam_sessions")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getSessionStats(): SessionStats? {
        val session = currentSession ?: return null

        return SessionStats(
            sessionId = session.sessionId,
            duration = session.getDuration(),
            snapshotsCount = session.snapshots.size,
            violationsCount = session.violations.size,
            eventsCount = session.securityEvents.size,
            hasBackVideo = session.backCameraVideo != null,
            status = session.status
        )
    }

    fun cleanup() {
        currentSession = null
        _sessionState.value = null
    }
}

/**
 * إحصائيات الجلسة
 */
data class SessionStats(
    val sessionId: String,
    val duration: Long,
    val snapshotsCount: Int,
    val violationsCount: Int,
    val eventsCount: Int,
    val hasBackVideo: Boolean,
    val status: SessionStatus
)