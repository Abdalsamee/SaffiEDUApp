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
import javax.inject.Inject

data class TeachData(
    val fullName: String = "",
    val subject: String = "",
    val isSubjectActivated: Boolean = false
)

@HiltViewModel
class TeacherHomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(TeacherHomeState())
    val state = _state.asStateFlow()

    // قائمة كاملة بالبيانات (كأنها في قاعدة البيانات)
    private val allUpdates = listOf(
        StudentUpdate("1", "محمد محمود", "", "حل واجب الرياضيات", "قبل ساعة"),
        StudentUpdate("2", "علي أحمد", "", "حل اختبار الفيزياء", "قبل ساعتين"),
        StudentUpdate("3", "سارة خالد", "", "سلمت مشروع الكيمياء", "قبل 3 ساعات"),
        StudentUpdate("4", "فاطمة علي", "", "حل واجب الأحياء", "قبل 5 ساعات"),
        StudentUpdate("5", "أحمد ياسر", "", "أجاب على سؤال النقاش", "قبل 6 ساعات"),
        StudentUpdate("6", "خالد وليد", "", "أنهى درس الكسور", "قبل 8 ساعات")
    )

    private val classesList = listOf(
        TeacherClass("c1","الصف الأول","رياضيات","",30,listOf("", "", "")),
        TeacherClass("c2","الصف الثاني","رياضيات","",24,listOf("", "", "")),
        TeacherClass("c3","الصف الثالث","","",15,listOf("", "", "")),
        TeacherClass("c4","الصف الرابع","","",12,listOf("", "", "")),
        TeacherClass("c5","الصف الخامس","","",20,listOf("", "", "")),
    )

    private var currentPage = 0
    private val pageSize = 3

    private var idTeach: String? = null // لتخزين رقم هوية المعلم (document id)

    init {
        // قراءة حالة تفعيل المادة من DataStore
        viewModelScope.launch {
            prefs.isSubjectActivated.collect { isActivated ->
                // تحديث الحالة في UI (الزر)
                _state.value = _state.value.copy(showActivateButton = !isActivated)
            }
        }
        // تحميل بيانات المدرس + باقي البيانات
        loadTeacherData()
    }

    /**
     * تحميل بيانات المدرس من Firestore
     */
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

                        if (teacherData != null) {
                            loadInitialData(
                                teacherName = teacherData.fullName,
                                teacherSubject = teacherData.subject,
                                isActivated = teacherData.isSubjectActivated
                            )
                            return@launch
                        }
                    }
                    loadInitialData("غير معروف", "غير معروف", false)

                } catch (e: Exception) {
                    loadInitialData("خطأ", "خطأ", false)
                }
            } else {
                loadInitialData("لم يتم تسجيل الدخول", "لم يتم تسجيل الدخول", false)
            }
        }
    }

    /**
     * تحميل البيانات الثابتة (الهيدر، الطلاب، الصفوف...)
     */
    private fun loadInitialData(teacherName: String, teacherSubject: String, isActivated: Boolean) {
        viewModelScope.launch {
            val activatedFromPrefs = prefs.isSubjectActivated.first() // اقرأ القيمة من DataStore
            val topStudentsList = listOf(
                TopStudent("st1", "طاهر قديح", "", 1, 98, "9/10", "10/10"),
                TopStudent("st2", "محمد خالد", "", 2, 96, "8/10", "10/10"),
                TopStudent("st3", "علي أحمد", "", 3, 95, "10/10", "8/10")
            )

            delay(500) // محاكاة تحميل البيانات

            _state.value = TeacherHomeState(
                isLoading = false,
                teacherName = teacherName,
                teacherSub = teacherSubject,
                profileImageUrl = "",
                studentUpdates = allUpdates.take(pageSize),
                teacherClasses = classesList,
                availableClassesForFilter = listOf("الصف السادس","الصف السابع","الصف الثامن","الصف الثاني عشر","الصف الحادي عشر"),
                selectedClassFilter = "الصف السادس",
                topStudents = topStudentsList,
                showActivateButton = !activatedFromPrefs // ✅ استخدم DataStore كمصدر
            )
        }
    }

    // ⬇️ تفعيل المادة
    fun activateSubject(selectedClass: String = "") {
        viewModelScope.launch {
            try {
                val teacherId = idTeach ?: return@launch
                val currentState = _state.value

                val subjectData = mapOf(
                    "teacherId" to teacherId,
                    "teacherName" to currentState.teacherName,
                    "subjectName" to currentState.teacherSub.removePrefix("مدرس ").trim(),
                    "className" to selectedClass,
                    "lessonsCount" to 0,
                    "rating" to 0
                )

                firestore.collection("subjects")
                    .document("${teacherId}_${currentState.teacherSub}")
                    .set(subjectData)
                    .await()

                firestore.collection("teachers")
                    .document(teacherId)
                    .update("isSubjectActivated", true)
                    .await()

                // ✅ تخزين الحالة في DataStore
                prefs.setSubjectActivated(true)

                // إخفاء الزر في الواجهة
                _state.value = _state.value.copy(showActivateButton = false)
                println("✅ تم تفعيل المادة وتخزين البيانات بنجاح")
            } catch (e: Exception) {
                println("❌ خطأ عند تفعيل المادة: ${e.message}")
            }
        }
    }


    fun onClassFilterSelected(className: String) {
        _state.value = _state.value.copy(selectedClassFilter = className)
        println("Selected class: $className")
    }

    fun loadNextUpdates() {
        if (_state.value.isLoading || currentPage * pageSize >= allUpdates.size) return

        viewModelScope.launch {
            val startIndex = currentPage * pageSize
            val endIndex = (startIndex + pageSize).coerceAtMost(allUpdates.size)
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
}
