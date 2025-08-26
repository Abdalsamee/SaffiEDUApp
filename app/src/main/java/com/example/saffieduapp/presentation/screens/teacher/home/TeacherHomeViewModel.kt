package com.example.saffieduapp.presentation.screens.teacher.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class TeachData(
    val fullName: String = "",
    val subject: String = ""
)

@HiltViewModel
class TeacherHomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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

    init {
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
                        val teacherData = querySnapshot.documents[0].toObject(TeachData::class.java)
                        if (teacherData != null) {
                            val nameParts = teacherData.fullName.trim().split("\\s+".toRegex())
                            val firstName = nameParts.firstOrNull() ?: ""
                            val lastName = if (nameParts.size > 1) nameParts.last() else ""
                            val displayName = if (lastName.isNotEmpty()) "أ. $firstName $lastName" else firstName

                            // بعد ما نجيب بيانات المدرس → نحمل باقي البيانات
                            loadInitialData(displayName, teacherData.subject)
                            return@launch
                        }
                    }

                    // في حالة ما في بيانات
                    loadInitialData("غير معروف", "غير معروف")

                } catch (e: Exception) {
                    loadInitialData("خطأ", "خطأ")
                }
            } else {
                loadInitialData("لم يتم تسجيل الدخول", "لم يتم تسجيل الدخول")
            }
        }
    }

    /**
     * تحميل البيانات الثابتة (الهيدر، الطلاب، الصفوف...)
     */
    private fun loadInitialData(teacherName: String, teacherSubject: String) {
        val topStudentsList = listOf(
            TopStudent("st1", "طاهر قديح", "", 1, 98, "9/10", "10/10"),
            TopStudent("st2", "محمد خالد", "", 2, 96, "8/10", "10/10"),
            TopStudent("st3", "علي أحمد", "", 3, 95, "10/10", "8/10")
        )

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(1500) // محاكاة تحميل من الشبكة

            val firstPage = allUpdates.take(pageSize)
            currentPage = 1

            val filterClasses = listOf("الصف السادس", "الصف السابع", "الصف الثامن", "الصف الثاني عشر", "الصف الحادي عشر")
            val initialSelectedClass = filterClasses.first()

            _state.value = TeacherHomeState(
                isLoading = false,
                teacherName = teacherName,
                teacherRole = "مدرس $teacherSubject",
                profileImageUrl = "",
                studentUpdates = firstPage,
                teacherClasses = classesList,
                availableClassesForFilter = filterClasses,
                selectedClassFilter = initialSelectedClass,
                topStudents = topStudentsList
            )
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
