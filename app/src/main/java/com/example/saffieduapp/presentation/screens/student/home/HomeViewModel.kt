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

// موديل بيانات المستخدم كما هو مخزن في Firestore
data class UserData(
    val fullName: String = "",
    val grade: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        // عند بدء ViewModel يتم جلب بيانات المستخدم والداتا الأولية
        loadUserData()
        loadInitialData()
    }

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
                            _state.value = _state.value.copy(
                                studentName = userData.fullName,
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


    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            delay(1500)  // محاكاة تأخير
            fetchAndProcessData()
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(1000) // محاكاة تحميل أولي
            fetchAndProcessData()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private fun fetchAndProcessData() {
        // بيانات مؤقتة (Dummy Data)
        val urgentTasksList = listOf(
            UrgentTask("1", "اختبار نصفي", "التربية الإسلامية", "24/8/2025", "11 صباحاً", ""),
            UrgentTask("2", "المهمة رقم 1", "اللغة الانجليزية", "24/8/2025", "12 مساءً", "")
        )

        val subjectsList = listOf(
            Subject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, "", 12),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, "", 20),
            Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 15)
        )

        val lessonsList = listOf(
            FeaturedLesson("l1", "Romeo story", "English", "15 دقيقة", 30, ""),
            FeaturedLesson("l2", "درس الكسور", "رياضيات", "15 دقيقة", 80, "")
        )

        // تحديث الحالة مع البيانات
        _state.value = _state.value.copy(
            profileImageUrl = "https://instagram.fgza2-5.fna.fbcdn.net/v/t51.2885-19/519497419_17974326737899750_3401532740011521622_n.jpg?_nc_cat=110",
            urgentTasks = urgentTasksList,
            enrolledSubjects = subjectsList,
            featuredLessons = lessonsList
        )
    }

    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }
}
