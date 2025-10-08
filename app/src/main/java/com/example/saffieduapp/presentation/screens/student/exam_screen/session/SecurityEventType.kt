package com.example.saffieduapp.presentation.screens.student.exam_screen.session

/**
 * أنواع الأحداث الأمنية في جلسة الاختبار
 */
enum class SecurityEventType {
    EXAM_STARTED,
    EXAM_PAUSED,
    EXAM_RESUMED,
    EXAM_SUBMITTED,
    EXAM_TERMINATED,
    SNAPSHOT_CAPTURED,
    ROOM_SCAN_COMPLETED,
    VIOLATION_DETECTED
}