package com.example.saffieduapp.presentation.screens.student.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.WorkManager.AlertScheduler
import com.example.saffieduapp.domain.model.FeaturedLesson
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.domain.model.UrgentTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Locale

data class StdData(
    val fullName: String = "",
    val grade: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    application: Application // حقن التطبيق
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()
    private val appContext = application.applicationContext


    init {
        loadUserData()
        viewModelScope.launch {
            loadInitialData()
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val currentUserEmail = auth.currentUser?.email
            if (currentUserEmail != null) {
                try {
                    val querySnapshot = firestore.collection("students")
                        .whereEqualTo("email", currentUserEmail)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val userData = querySnapshot.documents[0].toObject(StdData::class.java)
                        if (userData != null) {
                            val nameParts = userData.fullName.trim().split("\\s+".toRegex())
                            val firstName = nameParts.firstOrNull() ?: ""
                            val lastName = if (nameParts.size > 1) nameParts.last() else ""
                            val displayName = if (lastName.isNotEmpty()) "$firstName $lastName" else firstName

                            _state.value = _state.value.copy(
                                studentName = displayName,
                                studentGrade = userData.grade
                            )

                            // ✅ استدعاء المواد للصف المناسب بعد معرفة grade
                            loadSubjects()

                            // استدعاء التنبيهات لكل مادة بعد تحميلها
                            _state.value.enrolledSubjects.forEach { subject ->
                                listenForAlerts(subject.id, userData.grade)}
                        }
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        studentName = "خطأ في التحميل",
                        studentGrade = "خطأ في التحميل"
                    )
                }
            } else {
                _state.value = _state.value.copy(
                    studentName = "لم يتم تسجيل الدخول",
                    studentGrade = "لم يتم تسجيل الدخول"
                )
            }
        }
    }


    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            try {
                // أقصى وقت لجلب البيانات: 3 ثواني
                withTimeout(3000) {
                    loadSubjects()
                    loadInitialData()
                }
            } catch (_: Exception) {
                // تجاهل الخطأ أو سجّله إذا لزم
            } finally {
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }

    // suspend function → ما في launch داخله
    private suspend fun loadInitialData() {
        val urgentTasksList = listOf(
            UrgentTask("1", "اختبار نصفي", "التربية الإسلامية", "24/8/2025", "11 صباحاً", ""),
            UrgentTask("2", "المهمة رقم 1", "اللغة الانجليزية", "24/8/2025", "12 مساءً", "")
        )

        val lessonsList = listOf(
            FeaturedLesson("l1", "Romeo story", "English", "15 دقيقة", 30, ""),
            FeaturedLesson("l2", "درس الكسور", "رياضيات", "15 دقيقة", 80, "")
        )

        _state.value = _state.value.copy(
            urgentTasks = urgentTasksList,
            featuredLessons = lessonsList
        )
    }

    // suspend function → مباشرة تستخدم await
    private suspend fun loadSubjects() {
        try {
            val grade = _state.value.studentGrade
            if (grade.isBlank()) return

            val querySnapshot = firestore.collection("subjects")
                .whereEqualTo("className", grade)
                .get()
                .await()

            val subjectsList = querySnapshot.documents.map { doc ->
                val subjectName = doc.getString("subjectName") ?: "غير معروف"
                val teacherFullName = doc.getString("teacherName") ?: "غير معروف"

                val formattedUserName = formatUserName(teacherFullName)

                val gradeName = doc.getString("className") ?: "غير محدد"
                val lessonsCount = (doc.getLong("lessonsCount") ?: 0).toInt()
                val rating = (doc.getDouble("rating") ?: 0.0).toFloat()

                Subject(
                    id = doc.id,
                    name = subjectName,
                    teacherName = formattedUserName, // ✅ الصياغة الجديدة
                    grade = gradeName,
                    rating = rating,
                    imageUrl = "",
                    totalLessons = lessonsCount
                )
            }

            _state.value = _state.value.copy(
                enrolledSubjects = subjectsList,
                isLoading = false
            )
        } catch (_: Exception) {
            _state.value = _state.value.copy(isLoading = false)
        }
    }
    fun formatUserName(fullName: String): String {
        return try {
            val nameParts = fullName.trim().split("\\s+".toRegex())

            when {
                nameParts.isEmpty() -> "غير معروف"
                nameParts.size == 1 -> nameParts[0] // اسم واحد فقط
                else -> {
                    val firstName = nameParts[0]
                    val lastName = nameParts[nameParts.size - 1]
                    "$firstName $lastName"
                }
            }
        } catch (e: Exception) {
            "غير معروف"
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }

    private fun listenForAlerts(currentSubjectId: String, currentClassId: String) {
        firestore.collection("alerts")
            .whereEqualTo("subjectId", currentSubjectId) // فلترة حسب المادة
            .whereEqualTo("targetClass", currentClassId) // فلترة حسب الصف
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                for (doc in snapshot.documents) {
                    val description = doc.getString("description") ?: continue
                    val sendDate = doc.getString("sendDate") ?: continue // "yyyy-MM-dd"
                    val sendTime = doc.getString("sendTime") ?: "00:00 AM"

                    // دمج التاريخ مع الوقت وتحويله إلى Date
                    val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)
                    val triggerDate = try {
                        format.parse("$sendDate $sendTime")
                    } catch (ex: Exception) {
                        null
                    } ?: continue

                    // جدولة الإشعار
                    AlertScheduler.scheduleAlert(
                        context = appContext,
                        alertId = doc.id,
                        title = "📢 تنبيه جديد",
                        message = description,
                        triggerTime = triggerDate
                    )
                }
            }
    }

}
