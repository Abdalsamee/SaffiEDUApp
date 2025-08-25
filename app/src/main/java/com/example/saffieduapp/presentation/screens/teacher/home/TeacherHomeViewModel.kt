package com.example.saffieduapp.presentation.screens.teacher.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherHomeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(TeacherHomeState())
    val state = _state.asStateFlow()

    // قائمة كاملة بالبيانات (كأنها في قاعدة البيانات)
    private val allUpdates = listOf(
        StudentUpdate("1", "محمد محمود", "", "  حل واجب الرياضيات", "قبل ساعة"),
        StudentUpdate("2", "علي أحمد", "", " حل اختبار الفيزياء", "قبل ساعتين"),
        StudentUpdate("3", "سارة خالد", "", " سلمت مشروع الكيمياء", "قبل 3 ساعات"),
        StudentUpdate("4", "فاطمة علي", "", "حل واجب الأحياء", "قبل 5 ساعات"),
        StudentUpdate("5", "أحمد ياسر", "", "أجاب على سؤال النقاش", "قبل 6 ساعات"),
        StudentUpdate("6", "خالد وليد", "", "أنهى درس الكسور", "قبل 8 ساعات")
    )
    val classesList = listOf(
        TeacherClass("c1","الصف الأول","رياضيات","",30,listOf("", "", "")),
        TeacherClass("c2","الصف الثاني","رياضيات","",24,listOf("", "", "")),
        TeacherClass("c3","الصف الثالث","","",15,listOf("", "", "")),
        TeacherClass("c4","الصف الرابع","","",12,listOf("", "", "")),
        TeacherClass("c5","الصف الخامس","","",20,listOf("", "", "")),

    )


    private var currentPage = 0
    private val pageSize = 3

    init {
        // استدعاء دالة التحميل الأولي
        loadInitialData()
    }

    /**
     * دالة لتحميل البيانات الثابتة (الهيدر) والدفعة الأولى من البيانات المتغيرة.
     */
    private fun loadInitialData() {

        val topStudentsList = listOf(
            TopStudent("st1", "طاهر قديح", "", 1, 98, "9/10", "10/10"),
            TopStudent("st2", "محمد خالد", "", 2, 96, "8/10", "10/10"),
            TopStudent("st3", "علي أحمد", "", 3, 95, "10/10", "8/10")
        )
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(1500) // محاكاة تحميل أولي من الشبكة

            // جلب أول دفعة من البيانات
            val firstPage = allUpdates.take(pageSize)
            currentPage = 1 // الآن نحن جاهزون لجلب الصفحة رقم 1 في المرة القادمة


            val filterClasses = listOf("الصف السادس", "الصف السابع", "الصف الثامن", "الصف الثاني عشر", "الصف الحادي عشر")
            val initialSelectedClass = filterClasses.first() // اختيار أول صف كافتراضي

            // تحديث الحالة مرة واحدة فقط بكل البيانات
            _state.value = TeacherHomeState(
                isLoading = false,
                teacherName = "أ. عبدالسميع النجار",
                teacherRole =  "رياضيات",
                profileImageUrl = "", // Your image URL
                studentUpdates = firstPage,
                teacherClasses = classesList,
                availableClassesForFilter = filterClasses,
                selectedClassFilter = initialSelectedClass,
                topStudents = topStudentsList
                // TODO: Load top students for the initial selected class

            )
        }
    }
    fun onClassFilterSelected(className: String) {
        _state.value = _state.value.copy(selectedClassFilter = className)
        // TODO: Add logic here to fetch top students for the newly selected class
        println("Selected class: $className")
    }

    /**
     * هذه الدالة الآن مسؤولة فقط عن جلب "دفعة" جديدة وإضافتها للقائمة الحالية.
     */
    fun loadNextUpdates() {
        // منع الجلب إذا كنا في منتصف عملية تحميل أخرى أو وصلنا للنهاية
        if (_state.value.isLoading || currentPage * pageSize >= allUpdates.size) return

        viewModelScope.launch {
            // لا نعرض مؤشر التحميل الرئيسي، فهذا يحدث في الخلفية

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