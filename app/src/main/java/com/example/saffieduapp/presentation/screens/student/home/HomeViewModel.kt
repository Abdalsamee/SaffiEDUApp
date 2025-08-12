package com.example.saffieduapp.presentation.screens.student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.model.FeaturedLesson
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.domain.model.UrgentTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// نموذج بيانات المستخدم كما هو مخزن في Firestore
data class UserData(
    val fullName: String = "",
    val grade: String = ""
)

// ViewModel الخاص بشاشة Home للطالب
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // حالة الشاشة التي يتم تحديثها باستمرار باستخدام StateFlow
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    // عند إنشاء الـ ViewModel، يتم تحميل بيانات المستخدم والبيانات الأولية الأخرى
    init {
        loadUserData()
        loadInitialData()
    }

    /**
     * تحميل بيانات المستخدم من Firestore باستخدام البريد الإلكتروني للمستخدم الحالي
     */
    private fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val currentUserEmail = auth.currentUser?.email
            if (currentUserEmail != null) {
                try {
                    val querySnapshot = firestore.collection("users")
                        .whereEqualTo("email", currentUserEmail)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val userData = querySnapshot.documents[0].toObject(UserData::class.java)
                        if (userData != null) {
                            val nameParts = userData.fullName.trim().split("\\s+".toRegex())
                            val firstName = nameParts.firstOrNull() ?: ""
                            val lastName = if (nameParts.size > 1) nameParts.last() else ""

                            val displayName = if (lastName.isNotEmpty()) {
                                "$firstName $lastName"
                            } else {
                                firstName
                            }

                            _state.value = _state.value.copy(
                                studentName = displayName,
                                studentGrade = userData.grade,
                                isLoading = false
                            )
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                studentName = "غير معروف",
                                studentGrade = "غير معروف"
                            )
                        }
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            studentName = "غير معروف",
                            studentGrade = "غير معروف"
                        )
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        studentName = "خطأ في التحميل",
                        studentGrade = "خطأ في التحميل"
                    )
                }
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    studentName = "لم يتم تسجيل الدخول",
                    studentGrade = "لم يتم تسجيل الدخول"
                )
            }
        }
    }


    /**
     * تحديث البيانات عند سحب الشاشة (refresh)
     * هنا يتم محاكاة تأخير لعرض تأثير التحميل ثم تحديث البيانات
     */
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            delay(1500)  // محاكاة تأخير التحميل
            fetchAndProcessData()
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    /**
     * تحميل البيانات الأولية عند بدء الشاشة (تطبيق تحميل وهمي مع تأخير)
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(1000) // محاكاة تأخير التحميل
            fetchAndProcessData()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    /**
     * جلب ومعالجة البيانات الوهمية (Dummy Data)
     * - المهام العاجلة
     * - المواد الدراسية المسجلة
     * - الدروس المميزة
     */
    private fun fetchAndProcessData() {
        // قائمة المهام العاجلة (عينة بيانات)
        val urgentTasksList = listOf(
            UrgentTask("1", "اختبار نصفي", "التربية الإسلامية", "24/8/2025", "11 صباحاً", ""),
            UrgentTask("2", "المهمة رقم 1", "اللغة الانجليزية", "24/8/2025", "12 مساءً", "")
        )

        // قائمة المواد الدراسية (عينة بيانات)
        val subjectsList = listOf(
            Subject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, "", 12),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, "", 20),
            Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 15)
        )

        // قائمة الدروس المميزة (عينة بيانات)
        val lessonsList = listOf(
            FeaturedLesson("l1", "Romeo story", "English", "15 دقيقة", 30, ""),
            FeaturedLesson("l2", "درس الكسور", "رياضيات", "15 دقيقة", 80, "")
        )

        // تحديث حالة الشاشة مع البيانات الجديدة
        _state.value = _state.value.copy(
            profileImageUrl = "https://instagram.fgza2-5.fna.fbcdn.net/v/t51.2885-19/519497419_17974326737899750_3401532740011521622_n.jpg?_nc_cat=110",
            urgentTasks = urgentTasksList,
            enrolledSubjects = subjectsList,
            featuredLessons = lessonsList
        )
    }

    /**
     * تحديث حالة البحث عند تغيير نص البحث
     */
    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }
}
