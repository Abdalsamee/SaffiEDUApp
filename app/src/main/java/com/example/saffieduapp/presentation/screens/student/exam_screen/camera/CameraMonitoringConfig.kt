package com.example.saffieduapp.presentation.screens.student.exam_screen.camera

/**
 * ⚙️ إعدادات نظام المراقبة بالكاميرا
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/CameraMonitoringConfig.kt
 *
 * 🎯 الهدف:
 * مركز التحكم في جميع الإعدادات - تغيير أي رقم من هنا فقط!
 */
object CameraMonitoringConfig {

    // ═══════════════════════════════════════════
    // 📹 إعدادات الكاميرا الخلفية
    // ═══════════════════════════════════════════

    object BackCamera {
        // ⏰ التوقيت والتكرار
        const val RECORDING_ENABLED = true
        const val MIN_EXAM_DURATION_FOR_RECORDING = 20 * 1000L // 15 دقيقة
        const val RECORDING_DURATION = 10_000L // 10 ثواني
        const val EARLIEST_RECORDING_PERCENT = 0.15f // 15%
        const val LATEST_RECORDING_PERCENT = 0.85f // 85%

        // 🎥 مواصفات الفيديو
        const val TARGET_WIDTH = 854
        const val TARGET_HEIGHT = 480
        const val TARGET_BITRATE = 2_000_000 // 2 Mbps
        const val TARGET_FPS = 24
        const val VIDEO_CODEC = "video/avc" // H.264
        const val AUDIO_CODEC = "audio/mp4a-latm" // AAC
        const val AUDIO_BITRATE = 128_000 // 128 kbps
        const val AUDIO_SAMPLE_RATE = 44100

        // 💾 حدود الحجم
        const val TARGET_FILE_SIZE = 1_500_000L // 1.5 MB
        const val MAX_FILE_SIZE = 2_621_440L // 2.5 MB
        const val MIN_FILE_SIZE = 800_000L // 0.8 MB

        // 🗜️ الضغط
        const val COMPRESSION_ENABLED = true
        const val COMPRESSION_QUALITY = 0.7f // 70%

        // 🖼️ واجهة المستخدم
        const val UI_TRANSITION_DURATION = 300L // 0.3 ثانية
        const val SUCCESS_MESSAGE_DURATION = 1000L // 1 ثانية
    }

    // ═══════════════════════════════════════════
    // 📸 إعدادات الكاميرا الأمامية
    // ═══════════════════════════════════════════

    object FrontCamera {
        // 🔢 الحدود القصوى
        const val MAX_SNAPSHOTS = 10
        const val BASE_COOLDOWN = 30_000L // 30 ثانية

        // ⏱️ أولويات Cooldown
        const val COOLDOWN_CRITICAL = 0L // فوري - P0
        const val COOLDOWN_HIGH = 30_000L // 30 ثانية - P1
        const val COOLDOWN_NORMAL = 300_000L // 5 دقائق - P2

        // 🎯 عتبات الكشف
        const val NO_FACE_THRESHOLD = 5_000L // 5 ثواني
        const val LOOKING_AWAY_THRESHOLD = 10_000L // 10 ثواني
        const val FACE_TOO_FAR_THRESHOLD = 60_000L // دقيقة واحدة
        const val FACE_TOO_CLOSE_THRESHOLD = 60_000L // دقيقة واحدة

        // 🔄 الفحص الدوري
        const val PERIODIC_CHECK_INTERVAL = 300_000L // 5 دقائق

        // ⚠️ معالجة الانتهاكات
        const val MAX_MULTIPLE_FACES_WARNINGS = 2
        const val MAX_NO_FACE_WARNINGS = 5
        const val LOOKING_AWAY_WARNING_COUNT = 5

        // 📷 مواصفات الصورة
        const val SNAPSHOT_WIDTH = 1280
        const val SNAPSHOT_HEIGHT = 720
        const val SNAPSHOT_QUALITY = 85 // جودة JPEG
        const val TARGET_SNAPSHOT_SIZE = 500_000L // 500 KB
    }

    // ═══════════════════════════════════════════
    // 📤 إعدادات الرفع
    // ═══════════════════════════════════════════

    object Upload {
        const val UPLOAD_ON_COMPLETE = true
        const val RETRY_COUNT = 3
        val RETRY_DELAYS = longArrayOf(120_000L, 300_000L, 600_000L) // 2, 5, 10 دقائق
        const val REQUIRE_WIFI = false
        const val MIN_BATTERY_LEVEL = 10
        const val UPLOAD_TIMEOUT = 60_000L // دقيقة واحدة
    }

    // ═══════════════════════════════════════════
    // 🔐 إعدادات التشفير والتخزين
    // ═══════════════════════════════════════════

    object Security {
        const val ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"
        const val KEY_SIZE = 256
        const val IV_SIZE = 12
        const val TAG_SIZE = 128
    }

    // ═══════════════════════════════════════════
    // 🐛 إعدادات المطور
    // ═══════════════════════════════════════════

    object Debug {
        const val DETAILED_LOGGING = true // ⚠️ غيّره لـ false في الإنتاج
        const val CRASH_REPORTING = true
        const val ANALYTICS_ENABLED = true
        const val TEST_MODE = false
    }

    // ═══════════════════════════════════════════
    // 📊 دوال مساعدة للحسابات
    // ═══════════════════════════════════════════

    /**
     * حساب الوقت المناسب للتسجيل العشوائي
     *
     * مثال: اختبار 60 دقيقة
     * - أقل وقت = 9 دقائق (15%)
     * - أكبر وقت = 51 دقيقة (85%)
     * - النتيجة = رقم عشوائي بينهم
     */
    fun calculateRandomRecordingTime(examDurationMs: Long): Long {
        if (examDurationMs < BackCamera.MIN_EXAM_DURATION_FOR_RECORDING) {
            return -1 // الاختبار قصير جداً
        }

        val minTime = (examDurationMs * BackCamera.EARLIEST_RECORDING_PERCENT).toLong()
        val maxTime = (examDurationMs * BackCamera.LATEST_RECORDING_PERCENT).toLong()

        return (minTime..maxTime).random()
    }

    /**
     * حساب الحجم المتوقع للفيديو
     *
     * الصيغة:
     * حجم الفيديو = (Bitrate × المدة) / 8
     * + حجم الصوت
     * × نسبة الضغط
     */
    fun calculateExpectedVideoSize(durationMs: Long): Long {
        val videoBits = (BackCamera.TARGET_BITRATE * durationMs) / 1000
        val audioBits = (BackCamera.AUDIO_BITRATE * durationMs) / 1000
        val totalBytes = (videoBits + audioBits) / 8

        return (totalBytes * BackCamera.COMPRESSION_QUALITY).toLong()
    }

    /**
     * التحقق من توفر مساحة كافية
     *
     * مثال: جلسة واحدة تحتاج:
     * - فيديو: 2.5 MB
     * - 10 صور: 5 MB
     * - المجموع: 7.5 MB
     * + 20% هامش أمان = 9 MB
     */
    fun hasEnoughStorage(availableBytes: Long, sessionsCount: Int = 1): Boolean {
        val estimatedSize = (BackCamera.MAX_FILE_SIZE +
                (FrontCamera.TARGET_SNAPSHOT_SIZE * FrontCamera.MAX_SNAPSHOTS)) * sessionsCount
        return availableBytes > estimatedSize * 1.2 // 20% هامش أمان
    }
}