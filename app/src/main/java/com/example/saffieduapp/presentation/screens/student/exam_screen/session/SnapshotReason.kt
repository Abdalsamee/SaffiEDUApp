package com.example.saffieduapp.presentation.screens.student.exam_screen.session

/**
 * أسباب التقاط الصور
 */
enum class SnapshotReason {
    NO_FACE_DETECTED,    // لم يتم اكتشاف وجه
    MULTIPLE_FACES,      // أكثر من وجه
    LOOKING_AWAY,        // ينظر بعيداً
    MANUAL_CAPTURE,      // التقاط يدوي
    PERIODIC_CHECK       // فحص دوري
}