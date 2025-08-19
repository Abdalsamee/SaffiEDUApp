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

    // الحالة الداخلية للشاشة باستخدام MutableStateFlow (قابلة للتعديل داخلياً)
    private val _state = MutableStateFlow(HomeState())
    // الحالة العامة للعرض (غير قابلة للتعديل خارجيًا)
    val state = _state.asStateFlow()

    // عند بدء الـ ViewModel يتم تحميل بيانات المستخدم والبيانات الأولية
    init {
        loadUserData()
        loadInitialData()
    }

    /**
     * تحميل بيانات المستخدم من Firestore باستخدام البريد الإلكتروني للمستخدم الحالي
     * ويعرض الاسم الأول والاسم الأخير فقط
     */
    private fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true) // بدء حالة التحميل

            val currentUserEmail = auth.currentUser?.email
            if (currentUserEmail != null) {
                try {
                    // استعلام لجلب بيانات المستخدم بناء على البريد الإلكتروني
                    val querySnapshot = firestore.collection("students")
                        .whereEqualTo("email", currentUserEmail)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val userData = querySnapshot.documents[0].toObject(UserData::class.java)
                        if (userData != null) {
                            // تقسيم الاسم الكامل للحصول على الاسم الأول والاسم الأخير فقط
                            val nameParts = userData.fullName.trim().split("\\s+".toRegex())
                            val firstName = nameParts.firstOrNull() ?: ""
                            val lastName = if (nameParts.size > 1) nameParts.last() else ""

                            // دمج الاسم الأول والأخير أو الاسم الأول فقط إذا لم يوجد آخر
                            val displayName = if (lastName.isNotEmpty()) {
                                "$firstName $lastName"
                            } else {
                                firstName
                            }

                            // تحديث حالة الشاشة مع بيانات المستخدم المستخرجة
                            _state.value = _state.value.copy(
                                studentName = displayName,
                                studentGrade = userData.grade,
                                isLoading = false
                            )
                        } else {
                            // في حال فشل التحويل إلى نموذج UserData
                            _state.value = _state.value.copy(
                                isLoading = false,
                                studentName = "غير معروف",
                                studentGrade = "غير معروف"
                            )
                        }
                    } else {
                        // إذا لم يتم العثور على بيانات للمستخدم في Firestore
                        _state.value = _state.value.copy(
                            isLoading = false,
                            studentName = "غير معروف",
                            studentGrade = "غير معروف"
                        )
                    }
                } catch (e: Exception) {
                    // التعامل مع أي خطأ في جلب البيانات من Firestore
                    _state.value = _state.value.copy(
                        isLoading = false,
                        studentName = "خطأ في التحميل",
                        studentGrade = "خطأ في التحميل"
                    )
                }
            } else {
                // حالة عدم وجود مستخدم مسجل دخول
                _state.value = _state.value.copy(
                    isLoading = false,
                    studentName = "لم يتم تسجيل الدخول",
                    studentGrade = "لم يتم تسجيل الدخول"
                )
            }
        }
    }

    /**
     * تحديث البيانات عند السحب لتحديث الشاشة (Refresh)
     * يتم عرض تأثير تحميل مع تأخير ثم تحديث البيانات
     */
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true) // بدء حالة التحديث
            delay(1500)  // محاكاة تأخير تحميل البيانات
            fetchAndProcessData() // جلب البيانات وتحديث الحالة
            _state.value = _state.value.copy(isRefreshing = false) // انتهاء التحديث
        }
    }

    /**
     * تحميل البيانات الأولية عند بدء الشاشة
     * مع محاكاة تأخير للتحميل
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true) // بدء تحميل أولي
            delay(1000) // محاكاة تأخير
            fetchAndProcessData() // جلب بيانات وهمية
            _state.value = _state.value.copy(isLoading = false) // انتهاء التحميل
        }
    }

    /**
     * جلب ومعالجة البيانات الوهمية (Dummy Data)
     * تشمل المهام العاجلة، المواد الدراسية، والدروس المميزة
     */
    private fun fetchAndProcessData() {
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

        // تحديث الحالة مع البيانات الجديدة
        _state.value = _state.value.copy(
            profileImageUrl = "https://instagram.fgza2-5.fna.fbcdn.net/v/t51.2885-19/519497419_17974326737899750_3401532740011521622_n.jpg?_nc_cat=110",
            urgentTasks = urgentTasksList,
            enrolledSubjects = subjectsList,
            featuredLessons = lessonsList
        )
    }

    /**
     * تحديث حالة البحث عند تغير نص البحث
     */
    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }
}
