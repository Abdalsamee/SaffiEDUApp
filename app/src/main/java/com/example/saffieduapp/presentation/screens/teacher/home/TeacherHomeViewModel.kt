package com.example.saffieduapp.presentation.screens.teacher.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.local.preferences.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val _state = MutableStateFlow(TeacherHomeState())
    val state = _state.asStateFlow()

    private val allUpdates = listOf(
        StudentUpdate("1", "محمد محمود", "", "حل واجب الرياضيات", "قبل ساعة"),
        StudentUpdate("2", "علي أحمد", "", "حل اختبار الفيزياء", "قبل ساعتين"),
        StudentUpdate("3", "سارة خالد", "", "سلمت مشروع الكيمياء", "قبل 3 ساعات"),
        StudentUpdate("4", "فاطمة علي", "", "حل واجب الأحياء", "قبل 5 ساعات"),
        StudentUpdate("5", "أحمد ياسر", "", "أجاب على سؤال النقاش", "قبل 6 ساعات"),
        StudentUpdate("6", "خالد وليد", "", "أنهى درس الكسور", "قبل 8 ساعات")
    )

    private val classesList = listOf(
        TeacherClass("c1", "الصف الأول", "رياضيات", "", 30, listOf("", "", "")),
        TeacherClass("c2", "الصف الثاني", "رياضيات", "", 24, listOf("", "", "")),
        TeacherClass("c3", "الصف الثالث", "", "", 15, listOf("", "", "")),
        TeacherClass("c4", "الصف الرابع", "", "", 12, listOf("", "", "")),
        TeacherClass("c5", "الصف الخامس", "", "", 20, listOf("", "", ""))
    )

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
            val teacherDoc = firestore.collection("teachers")
                .document(teacherId)
                .get()
                .await()

            // افترض أن الصفوف مخزنة كقائمة في حقل "classes"
            val classes = teacherDoc.get("className") as? List<String>
                ?: emptyList()

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
                    val querySnapshot = firestore.collection("teachers")
                        .whereEqualTo("email", currentUserEmail)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents[0]
                        idTeach = doc.id
                        val teacherData = doc.toObject(TeachData::class.java)

                        val teacherId = doc.id
                        val teacherClasses = teacherData?.classes ?: emptyList()

                        // تحقق من وجود أي مادة للمستخدم في كوليكشن subjects
                        val subjectsSnapshot = firestore.collection("subjects")
                            .whereEqualTo("teacherId", teacherId)
                            .get()
                            .await()

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

            delay(500)

            _state.value = TeacherHomeState(
                isLoading = false,
                teacherName = teacherName,
                teacherSub = teacherSubject,
                profileImageUrl = "",
                studentUpdates = allUpdates.take(3),
                teacherClasses = classesList,
                availableClassesForFilter = teacherClasses.ifEmpty { // استخدام الصفوف الفعلية
                    listOf("الصف السادس", "الصف السابع", "الصف الثامن")
                },
                selectedClassFilter = teacherClasses.firstOrNull() ?: "الصف السادس",
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
                    val existingSubjects = firestore.collection("subjects")
                        .whereEqualTo("teacherId", teacherId)
                        .whereEqualTo("subjectName", subjectName)
                        .whereEqualTo("className", className)
                        .get()
                        .await()

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
                        firestore.collection("subjects")
                            .document(docId)
                            .set(subjectData)
                            .await()

                        println("✅ تم تفعيل المادة $subjectName للصف $className")
                    } else {
                        println("ℹ️ المادة $subjectName مفعلة بالفعل للصف $className")
                    }
                }

                // 🔹 تحديث حالة المعلم
                firestore.collection("teachers")
                    .document(teacherId)
                    .update("isSubjectActivated", true)
                    .await()

                prefs.setSubjectActivated(true)
                _state.value = _state.value.copy(showActivateButton = false)

            } catch (e: Exception) {
                println("❌ خطأ عند تفعيل المادة: ${e.message}")
            }
        }
    }

    fun onClassFilterSelected(className: String) {
        _state.value = _state.value.copy(selectedClassFilter = className)
    }

    fun loadNextUpdates() {
        if (_state.value.isLoading || currentPage * 3 >= allUpdates.size) return
        viewModelScope.launch {
            val startIndex = currentPage * 3
            val endIndex = (startIndex + 3).coerceAtMost(allUpdates.size)
            val newUpdates = allUpdates.subList(startIndex, endIndex)

            _state.value = _state.value.copy(
                studentUpdates = _state.value.studentUpdates + newUpdates
            )
            currentPage++
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    private fun formatUserName(fullName: String): String {
        return try {
            val nameParts = fullName.trim().split("\\s+".toRegex())
            when {
                nameParts.isEmpty() -> "أ. غير معروف"
                nameParts.size == 1 -> "أ. ${nameParts[0]}"
                else -> "أ. ${nameParts.first()} ${nameParts.last()}"
            }
        } catch (e: Exception) {
            "أ. غير معروف"
        }
    }
}
