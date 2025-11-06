package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class TeacherStudentExamViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(TeacherStudentExamState(isLoading = true))
    val state: StateFlow<TeacherStudentExamState> = _state

    private val db = FirebaseFirestore.getInstance() // ⬅️ إضافة Firestore
    private val gson = Gson() // ⬅️ إنشاء مثيل Gson

    // الأسماء متطابقة مع navArgument في ملف التنقل
    private val examId: String = checkNotNull(savedStateHandle["examId"])
    private val studentId: String = checkNotNull(savedStateHandle["studentId"])

    init {
        loadExamData()
    }

    /**
     * 🔹 تحميل بيانات الطالب والاختبار من Firestore
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadExamData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // 1. جلب مستند الاختبار (Exam) للحصول على المدة القصوى
                val examDoc = db.collection("exams").document(examId).get().await()
                val examDurationMinutes = examDoc.getString("examTime")?.toIntOrNull() ?: 0

                // 2. جلب مستند التسليم (Submission)
                val submissionQuery =
                    db.collection("exam_submissions").whereEqualTo("examId", examId)
                        .whereEqualTo("studentId", studentId).get().await()

                val submissionDoc = submissionQuery.documents.firstOrNull()
                    ?: throw Exception("لم يتم العثور على تسليم الاختبار.")

                // 3. جلب مستند الطالب (Student)
                val studentDoc = db.collection("students").document(studentId).get().await()

                // 4. جلب تقرير المراقبة (Monitoring Report)
                val reportQuery =
                    db.collection("exam_monitoring_reports").whereEqualTo("examId", examId)
                        .whereEqualTo("studentId", studentId).get().await()

                val reportDoc = reportQuery.documents.firstOrNull()


                // 5. معالجة البيانات

                // 5.1. بيانات التسليم والدرجات
                val earnedScore = submissionDoc.getLong("score")?.toInt() ?: 0
                val totalScore = submissionDoc.getLong("maxScore")?.toInt() ?: 0
                // المدة الفعلية التي قضاها الطالب (بالدقائق)
                val timeSpentMinutes =
                    (submissionDoc.getLong("totalDurationSeconds")?.div(60))?.toInt() ?: 0
                val status =
                    if (submissionDoc.getBoolean("isCompleted") == true) "مكتملة" else "غير مكتملة"


                // 5.2. بيانات التقرير (المراقبة)
                // ⬅️ تم تصحيح منطق الاستخراج هنا
                val (cheatingLogs, imageUrls, videoUrl) = extractMonitoringData(reportDoc)

                // 5.3. بيانات الطالب
                val studentName = studentDoc.getString("fullName") ?: "اسم غير معروف"
                val studentImageUrl = studentDoc.getString("profileImageUrl")


                // 6. تحديث الحالة
                _state.value = TeacherStudentExamState(
                    isLoading = false,
                    studentName = studentName,
                    studentImageUrl = studentImageUrl,
                    earnedScore = earnedScore,
                    totalScore = totalScore,
                    answerStatus = status,
                    totalTimeMinutes = examDurationMinutes,
                    examStatus = ExamStatus.COMPLETED,
                    cheatingLogs = cheatingLogs,
                    imageUrls = imageUrls,
                    videoUrl = videoUrl
                )

            } catch (e: Exception) {
                println("Error loading exam data: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "فشل في تحميل بيانات الاختبار"
                    )
                }
            }
        }
    }

    /**
     * 🔹 تحديث درجة الطالب أثناء الكتابة
     * يتم التحقق من إدخال أرقام صحيحة فقط
     */
    fun onScoreChange(newScore: String) {
        val numericValue = newScore.toIntOrNull() ?: 0
        _state.update { it.copy(earnedScore = numericValue) }
    }

    fun onSaveExamEvaluation() {
        viewModelScope.launch {
            // ... (منطق الحفظ لم يتغير وهو صحيح)
            // 1. تحديد مرجع المستند في مجموعة "exam_submissions"
            val submissionQuery = db.collection("exam_submissions").whereEqualTo("examId", examId)
                .whereEqualTo("studentId", studentId).get().await()

            val submissionDocSnapshot = submissionQuery.documents.firstOrNull()

            if (submissionDocSnapshot == null) {
                _state.update { it.copy(errorMessage = "لم يتم العثور على مستند التسليم للحفظ.") }
                return@launch
            }

            val submissionDocRef = submissionDocSnapshot.reference

            // 2. فحص حالة التعديل
            val isAlreadyEdited = submissionDocSnapshot.getBoolean("scoreEditedByTeacher") == true

            if (isAlreadyEdited) {
                println("⚠️ تم تعديل العلامة مسبقًا. لا يمكن التعديل مرة أخرى.")
                _state.update { it.copy(errorMessage = "تم تعديل العلامة مسبقًا ولا يمكن التعديل مرة أخرى.") }
                return@launch
            }

            // 3. إنشاء البيانات المراد تحديثها
            val updates = hashMapOf<String, Any>(
                "score" to _state.value.earnedScore, // العلامة الجديدة من الـ State
                "scoreEditedByTeacher" to true,       // تعيين الحقل لمنع التعديلات المستقبلية
                "lastEditedByTeacherAt" to System.currentTimeMillis() / 1000 // (اختياري) طابع زمني
            )

            // 4. حفظ التحديث
            try {
                submissionDocRef.update(updates).await()
                println("✅ تم حفظ تقييم الطالب بنجاح وتم تعيين حالة التعديل.")
                _state.update { it.copy(errorMessage = null) } // مسح أي خطأ سابق
            } catch (e: Exception) {
                println("❌ فشل في حفظ التقييم: ${e.message}")
                _state.update {
                    it.copy(
                        errorMessage = "فشل في حفظ التقييم: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 🔹 فتح شاشة عرض إجابات الطالب
     */
    fun onViewAnswersClick() {
        println("📄 عرض إجابات الطالب: ${_state.value.studentName}")
        // TODO: navController.navigate(Routes.TEACHER_STUDENT_EXAM_ANSWERS)
    }

    /**
     * 🔹 عند النقر على صورة مراقبة
     */
    fun onImageClick(url: String) {
        println("🖼️ عرض الصورة: $url")
        // TODO: فتح Dialog أو شاشة لعرض الصورة بالحجم الكامل
    }

    /**
     * 🔹 عند النقر على الفيديو
     */
    fun onVideoClick() {
        println("🎥 تشغيل الفيديو من الرابط: ${_state.value.videoUrl}")
        // TODO: تشغيل الفيديو باستخدام ExoPlayer أو External Viewer
    }

    /**
     * دالة مساعدة لاستخراج سجلات الغش والوسائط من مستند التقرير.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun extractMonitoringData(reportDoc: com.google.firebase.firestore.DocumentSnapshot?): Triple<List<String>, List<String>, String?> {

        // 1. استخراج سجلات الأحداث (Security Events) من حقل reportJson
        val reportJsonString = reportDoc?.getString("reportJson")
        val securityEvents = if (reportJsonString != null) {
            try {
                // نقوم بتحليل السلسلة النصية reportJson إلى خريطة Map
                val reportMap = gson.fromJson<Map<String, Any>>(
                    reportJsonString, object : TypeToken<Map<String, Any>>() {}.type
                )
                // نستخرج مصفوفة "securityEvents"
                reportMap["securityEvents"] as? List<Map<String, Any>> ?: emptyList()
            } catch (e: Exception) {
                println("Error parsing reportJson: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }

        val formattedLogs = securityEvents.mapNotNull { event ->
            val type = event["type"] as? String
            // طابع الوقت في JSON المرفق هو بالثواني/الملي ثانية (قد تحتاج لتعديله حسب نظامك)
            // بناءً على بياناتك المرفقة، يبدو أنه بالمللي ثانية، لذا نستخدم Instant.ofEpochMilli
            val timestampMilli = (event["timestamp"] as? Number)?.toLong()

            if (type != null && timestampMilli != null) {
                // تحويل UNIX timestamp (مللي ثانية) إلى تنسيق وقت
                val time = Instant.ofEpochMilli(timestampMilli).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("hh:mm a", Locale("ar")))

                // تحويل أنواع الأحداث إلى نصوص عربية مناسبة للعرض
                val logText = when (type) {
                    "EXAM_STARTED" -> "بدأ الاختبار"
                    "EXAM_PAUSED" -> "أوقف الاختبار"
                    "EXAM_RESUMED" -> "استأنف الاختبار"
                    "EXAM_SUBMITTED" -> "سلم الاختبار"
                    "SNAPSHOT_CAPTURED" -> "تم التقاط صورة"
                    "NO_FACE_DETECTED" -> "لم يتم الكشف عن وجه (تنبيه)"
                    "MULTIPLE_FACES" -> "تم الكشف عن وجوه متعددة (تنبيه)"
                    "ROOM_SCAN_COMPLETED" -> "اكتمل مسح الغرفة (فيديو)"
                    else -> type
                }
                return@mapNotNull "$time → $logText"
            }
            null
        }

        // 2. استخراج روابط الوسائط (Media URLs)
        // 💡 الحقل "media" هو مصفوفة (List) وليس خريطة (Map)
        val mediaList = reportDoc?.get("media") as? List<Map<String, Any>> ?: emptyList()

        // 2.1. استخراج عناوين URL للصور (يجب أن يكون نوعها "image")
        val imageUrls = mediaList.filter { it["type"] == "image" }.mapNotNull {
            it["downloadUrl"] as? String // ⬅️ المفتاح الصحيح هو "downloadUrl"
        }

        // 2.2. استخراج عنوان URL للفيديو (يجب أن يكون نوعها "video")
        val videoUrl = mediaList.firstOrNull { it["type"] == "video" }?.let {
            it["downloadUrl"] as? String // ⬅️ المفتاح الصحيح هو "downloadUrl"
        }

        return Triple(formattedLogs, imageUrls, videoUrl)
    }

}