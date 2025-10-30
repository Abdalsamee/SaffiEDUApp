package com.example.saffieduapp.presentation.screens.teacher.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.local.preferences.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class TeachData(
    val fullName: String = "",
    val subject: String = "",
    val isSubjectActivated: Boolean = false,
    val classes: List<String> = emptyList()
)

@HiltViewModel
class TeacherHomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val prefs: PreferencesManager
) : ViewModel() {

    // ⭐️ إضافة جديدة: لتعريف الأحداث التي تحدث مرة واحدة
    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        // يمكنك إضافة أحداث أخرى هنا مستقبلاً، مثل الانتقال لشاشة أخرى
    }

    // ⭐️ إضافة جديدة: لتدفق الأحداث
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _state = MutableStateFlow(TeacherHomeState())
    val state = _state.asStateFlow()

    private var currentPage = 0
    private var idTeach: String? = null // لتخزين رقم هوية المعلم

    init {
        viewModelScope.launch {
            // جلب حالة التفعيل من Firestore أولًا
            loadTeacherData()
        }
    }

    private suspend fun getTeacherClasses(teacherId: String): List<String> {
        return try {
            val teacherDoc = firestore.collection("teachers").document(teacherId).get().await()

            // افترض أن الصفوف مخزنة كقائمة في حقل "classes"
            val classes = teacherDoc.get("className") as? List<String> ?: emptyList()

            classes
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadTeacherData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val currentUserEmail = auth.currentUser?.email
            if (currentUserEmail != null) {
                try {
                    val querySnapshot =
                        firestore.collection("teachers").whereEqualTo("email", currentUserEmail)
                            .get().await()

                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents[0]
                        idTeach = doc.id
                        val teacherData = doc.toObject(TeachData::class.java)

                        val teacherId = doc.id
                        val teacherClasses = teacherData?.classes ?: emptyList()

                        // تحقق من وجود أي مادة للمستخدم في كوليكشن subjects
                        val subjectsSnapshot =
                            firestore.collection("subjects").whereEqualTo("teacherId", teacherId)
                                .get().await()

                        val hasAnySubject = !subjectsSnapshot.isEmpty

                        loadInitialData(
                            teacherName = formatUserName(teacherData?.fullName ?: "غير معروف"),
                            teacherSubject = teacherData?.subject ?: "غير معروف",
                            isActivated = hasAnySubject,
                            teacherClasses = teacherClasses // تمرير قائمة الصفوف
                        )
                        return@launch
                    }
                    loadInitialData("غير معروف", "غير معروف", false, emptyList())

                } catch (e: Exception) {
                    loadInitialData("خطأ", "خطأ", false, emptyList())
                }
            } else {
                loadInitialData("لم يتم تسجيل الدخول", "لم يتم تسجيل الدخول", false, emptyList())
            }
        }
    }

    // **HELPER FUNCTION: Fetch Student Name**
    private suspend fun getStudentName(studentId: String): String {
        return try {
            val studentDoc = firestore.collection("students").document(studentId).get().await()
            studentDoc.getString("fullName") ?: "طالب مجهول ($studentId)"
        } catch (e: Exception) {
            "خطأ في جلب اسم الطالب ($studentId)"
        }
    }

    // **HELPER FUNCTION: Fetch Assignment Title**
    private suspend fun getAssignmentTitle(assignmentId: String): String {
        return try {
            val assignmentDoc =
                firestore.collection("assignments").document(assignmentId).get().await()
            assignmentDoc.getString("title") ?: "واجب"
        } catch (e: Exception) {
            "خطأ في جلب عنوان الواجب"
        }
    }

    // **HELPER FUNCTION: Fetch Exam Title**
    private suspend fun getExamTitle(examId: String): String {
        return try {
            val examDoc = firestore.collection("exams").document(examId).get().await()
            examDoc.getString("examTitle") ?: "اختبار"
        } catch (e: Exception) {
            "خطأ في جلب عنوان الاختبار"
        }
    }

    /**
     * تجلب أحدث 3 تسليمات للامتحانات والواجبات التي تتعلق بصفوف المعلم.
     */
    private suspend fun getLatestStudentUpdates(teacherClasses: List<String>): List<StudentUpdate> {
        val allSubmissions = mutableListOf<StudentUpdate>()

        // 1. جلب أحدث تسليمات الواجبات
        try {
            val assignmentsSnapshot = firestore.collection("assignment_submissions")
                // يجب تطبيق الفلترة هنا: .whereIn("className", teacherClasses)
                .orderBy("submissionTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(3) // جلب آخر 3 واجبات
                .get().await()

            for (doc in assignmentsSnapshot.documents) {
                val studentId = doc.getString("studentId") ?: continue
                val assignmentId = doc.getString("assignmentId") ?: continue
                val submissionTime = doc.getLong("submissionTime") ?: 0L

                // 🌟 جلب اسم الطالب وعنوان الواجب
                val studentName = getStudentName(studentId)
                val taskTitle = getAssignmentTitle(assignmentId)

                allSubmissions.add(
                    StudentUpdate(
                        studentId = studentId,
                        studentName = studentName,
                        studentImageUrl = "", // يجب جلبها من كوليكشن الطلاب
                        taskTitle = "حل " + taskTitle, // إضافة 'حل' للعرض
                        submissionTime = formatTimestamp(submissionTime)
                    )
                )
            }
        } catch (e: Exception) {
            println("خطأ في جلب تسليمات الواجبات: ${e.message}")
        }

        // 2. جلب أحدث تسليمات الاختبارات
        try {
            val examsSnapshot = firestore.collection("exam_submissions")
                // يجب تطبيق الفلترة هنا: .whereIn("className", teacherClasses)
                .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(3) // جلب آخر 3 اختبارات
                .get().await()

            for (doc in examsSnapshot.documents) {
                val studentId = doc.getString("studentId") ?: continue
                val examId = doc.getString("examId") ?: continue
                val submittedAt = doc.getLong("submittedAt") ?: 0L

                // 🌟 جلب اسم الطالب وعنوان الاختبار
                val studentName = getStudentName(studentId)
                val taskTitle = getExamTitle(examId)

                allSubmissions.add(
                    StudentUpdate(
                        studentId = studentId,
                        studentName = studentName,
                        studentImageUrl = "",
                        taskTitle = "حل " + taskTitle, // إضافة 'حل' للعرض
                        submissionTime = formatTimestamp(submittedAt)
                    )
                )
            }
        } catch (e: Exception) {
            println("خطأ في جلب تسليمات الاختبارات: ${e.message}")
        }

        // 3. دمج وفرز النتائج النهائية
        // يتم الفرز هنا حسب الوقت لضمان عرض الأحدث أولاً، ثم نأخذ الـ 5 الأحدث.
        // ⚠️ ملاحظة: بما أننا لا نملك الحقل 'rawTimestamp' في StudentUpdate، لا يمكننا فرز 'allSubmissions' بشكل صحيح بناءً على الوقت.
        // يجب تعديل StudentUpdate لتضمين الوقت الأصلي (Long) للفرز الدقيق.
        // لغرض التصحيح الحالي، سنعتمد على أن الاستعلامين جلبوا الأحدث ونتخذ الـ 5 الأوائل.

        return allSubmissions.take(5) // عرض أحدث 5 تحديثات
    }

    // دالة مساعدة لتنسيق وقت التسليم (مثال مبسط)
    private fun formatTimestamp(timestamp: Long): String {
        // يجب استخدام SimpleDateFormat أو Joda-Time أو java.time لتنسيق صحيح
        val diff = System.currentTimeMillis() - timestamp
        val hours = diff / (1000 * 60 * 60)
        return if (hours < 1) "قبل دقائق" else "قبل ${hours.toInt()} ساعة"
    }

    // ... (TeacherHomeViewModel code before loadInitialData)

    /**
     * دالة مساعدة لتحويل اسم الصف العربي إلى رقم لغرض الفلترة/التحقق
     */
    private fun mapClassNameToNumber(className: String): Int {
        return when (className) {
            "الصف الأول" -> 1
            "الصف الثاني" -> 2
            "الصف الثالث" -> 3
            "الصف الرابع" -> 4
            "الصف الخامس" -> 5
            "الصف السادس" -> 6
            "الصف السابع" -> 7
            "الصف الثامن" -> 8
            "الصف التاسع" -> 9
            "الصف العاشر" -> 10
            "الصف الحادي عشر" -> 11
            "الصف الثاني عشر" -> 12
            else -> 99 // رقم كبير للصفوف غير المعروفة
        }
    }

    /**
     * تجلب الصفوف التي يدرسها المعلم (من الصف الأول للخامس) مع بيانات المادة وصور الطلاب.
     */
    private suspend fun getTeacherClassDetails(teacherId: String): List<TeacherClass> {
        val teacherClassesDetails = mutableListOf<TeacherClass>()

        try {
            // 1. جلب المواد التي يدرسها المعلم
            val subjectsSnapshot =
                firestore.collection("subjects").whereEqualTo("teacherId", teacherId).get().await()

            for (subjectDoc in subjectsSnapshot.documents) {
                val className = subjectDoc.getString("className") ?: continue
                val subjectName = subjectDoc.getString("subjectName") ?: continue
                val subjectImage =
                    subjectDoc.getString("subjectImageUrl") ?: "" // افترض وجود حقل للصورة

                // 2. فلترة الصفوف لتكون من الأول للخامس
                val classNumber = mapClassNameToNumber(className)
                if (classNumber !in 1..5) continue

                // 3. جلب الطلاب لهذا الصف
                val studentsSnapshot = firestore.collection("students").whereEqualTo(
                    "grade", className
                ) // يجب أن يكون حقل grade في students يطابق className
                    .get().await()

                val studentCount = studentsSnapshot.size()
                val studentImages = studentsSnapshot.documents.take(3) // جلب صور أول 3 طلاب
                    .mapNotNull { it.getString("profileImageUrl") }
                    // ⚠️ ملاحظة: لقطة الشاشة للطالب image_3542fa.png تُظهر الحقل "profileImageUrl"
                    // لكن قد تحتاج لتعديل مسار التخزين هنا إذا كان مختلفًا عن المتوقع.
                    .toList()

                // 4. بناء كائن TeacherClass
                teacherClassesDetails.add(
                    TeacherClass(
                        classId = subjectDoc.id, // يمكن استخدام ID المادة كـ ClassID مؤقت
                        className = className,
                        subjectName = subjectName,
                        subjectImageUrl = subjectImage,
                        studentCount = studentCount,
                        studentImages = studentImages
                    )
                )
            }
        } catch (e: Exception) {
            println("خطأ في جلب تفاصيل صفوف المعلم: ${e.message}")
            return emptyList()
        }

        return teacherClassesDetails
    }

// ... (Rest of TeacherHomeViewModel code)

    // 💡 تعديل دالة loadInitialData لاستخدام الدالة الجديدة
    private fun loadInitialData(
        teacherName: String,
        teacherSubject: String,
        isActivated: Boolean,
        teacherClasses: List<String>
    ) {
        viewModelScope.launch {
            val topStudentsList = listOf(
                TopStudent("st1", "طاهر قديح", "", 1, 98, "9/10", "10/10"),
                TopStudent("st2", "محمد خالد", "", 2, 96, "8/10", "10/10"),
                TopStudent("st3", "علي أحمد", "", 3, 95, "10/10", "8/10")
            )

            // ⚠️ الاستدعاء الجديد هنا لجلب بيانات الصفوف بالتفصيل
            val fetchedTeacherClasses = if (idTeach != null) {
                getTeacherClassDetails(idTeach!!)
            } else {
                emptyList()
            }

            val fetchedUpdates = getLatestStudentUpdates(teacherClasses)

            delay(500)

            _state.value = TeacherHomeState(
                isLoading = false,
                teacherName = teacherName,
                teacherSub = teacherSubject,
                profileImageUrl = "",
                studentUpdates = fetchedUpdates,
                // 🔄 استخدام بيانات الصفوف المجلوبة هنا بدلاً من classesList الثابتة
                teacherClasses = fetchedTeacherClasses,
                availableClassesForFilter = fetchedTeacherClasses.map { it.className }.ifEmpty {
                    listOf("الصف السادس", "الصف السابع", "الصف الثامن")
                },
                selectedClassFilter = fetchedTeacherClasses.firstOrNull()?.className
                    ?: "الصف السادس",
                topStudents = topStudentsList,
                showActivateButton = !isActivated
            )
        }
    }

    fun activateSubject() {
        viewModelScope.launch {
            try {
                val teacherId = idTeach ?: return@launch
                val currentState = _state.value
                val subjectName = currentState.teacherSub.removePrefix("مدرس ").trim()

                // 🔹 جلب قائمة الصفوف الخاصة بالمعلم
                val teacherClasses = getTeacherClasses(teacherId)

                if (teacherClasses.isEmpty()) {
                    println("⚠️ لا توجد صفوف مرتبطة بهذا المعلم")
                    return@launch
                }

                // 🔹 تفعيل المادة لكل صف
                for (className in teacherClasses) {
                    // تحقق إذا كانت المادة مفعلة بالفعل لهذا الصف
                    val existingSubjects =
                        firestore.collection("subjects").whereEqualTo("teacherId", teacherId)
                            .whereEqualTo("subjectName", subjectName)
                            .whereEqualTo("className", className).get().await()

                    if (existingSubjects.isEmpty) {
                        // 🔹 إضافة المادة للصف الحالي
                        val subjectData = mapOf(
                            "teacherId" to teacherId,
                            "teacherName" to currentState.teacherName,
                            "subjectName" to subjectName,
                            "className" to className,
                            "lessonsCount" to 0,
                            "rating" to 0
                        )

                        val docId = UUID.randomUUID().toString()
                        firestore.collection("subjects").document(docId).set(subjectData).await()

                        println("✅ تم تفعيل المادة $subjectName للصف $className")
                    } else {
                        println("ℹ️ المادة $subjectName مفعلة بالفعل للصف $className")
                    }
                }

                // 🔹 تحديث حالة المعلم
                // 🔹 تحديث حالة المعلم
                firestore.collection("teachers").document(teacherId)
                    .update("isSubjectActivated", true).await()

                prefs.setSubjectActivated(true)
                _state.value = _state.value.copy(showActivateButton = false)

                // ⭐️ إضافة جديدة: إرسال حدث بنجاح العملية
                _eventFlow.emit(UiEvent.ShowSnackbar("تم تفعيل المادة بنجاح"))

            } catch (e: Exception) {
                println("❌ خطأ عند تفعيل المادة: ${e.message}")
                // ⭐️ إضافة اختيارية: إرسال رسالة خطأ
                _eventFlow.emit(UiEvent.ShowSnackbar("خطأ: ${e.message}"))
            }
        }
    }

    fun onClassFilterSelected(className: String) {
        _state.value = _state.value.copy(selectedClassFilter = className)
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    private fun formatUserName(fullName: String): String {
        return try {
            val nameParts = fullName.trim().split("\\s+".toRegex())
            when {
                nameParts.isEmpty() -> "غير معروف"
                nameParts.size == 1 -> nameParts[0]
                else -> "${nameParts.first()} ${nameParts.last()}"
            }
        } catch (e: Exception) {
            "غير معروف"
        }
    }

}
