package com.example.saffieduapp.presentation.screens.student.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class StdData(
    val fullName: String = "",
    val grade: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    application: Application
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
                            val formattedStudentName = formatStudentName(userData.fullName)
                            val studentWithGrade = formattedStudentName

                            _state.value = _state.value.copy(
                                studentName = studentWithGrade,
                                studentGrade = userData.grade,
                                isLoading = false
                            )

                            // تحميل المواد للصف المناسب
                            loadSubjects()

                            // استدعاء التنبيهات لكل مادة بعد تحميلها
                            _state.value.enrolledSubjects.forEach { subject ->
                                listenForAlerts(subject.id, userData.grade)
                            }

                            // الاستماع للدرس الجديد بعد معرفة الصف
                            listenForNewLessons(userData.grade)
                        }
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        studentName = "خطأ في التحميل",
                        studentGrade = "خطأ في التحميل",
                        isLoading = false
                    )
                }
            } else {
                _state.value = _state.value.copy(
                    studentName = "لم يتم تسجيل الدخول",
                    studentGrade = "لم يتم تسجيل الدخول",
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            try {
                withTimeout(5000) {
                    loadSubjects()
                    loadInitialData()
                }
            } catch (_: Exception) {
            } finally {
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }

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

                // صياغة اسم المعلم بصيغة ثابتة "أ.الاسم الأول الاسم الأخير"
                val formattedTeacherName = formatTeacherName(teacherFullName)

                val gradeName = doc.getString("className") ?: "غير محدد"
                val lessonsCount = (doc.getLong("lessonsCount") ?: 0).toInt()
                val rating = (doc.getDouble("rating") ?: 0.0).toFloat()

                Subject(
                    id = doc.id,
                    name = subjectName,
                    teacherName = formattedTeacherName,
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

    // صياغة اسم المعلم بالثابت "أ."
    private fun formatTeacherName(fullName: String): String {
        val nameParts = fullName.trim().split("\\s+".toRegex())
        if (nameParts.isEmpty()) return "أ.غير معروف"

        val firstName = nameParts[0]
        val lastName = nameParts.last()

        return "أ.$firstName $lastName"
    }

    // صياغة اسم الطالب ليظهر فقط الاسم الأول والأخير
    private fun formatStudentName(fullName: String): String {
        val nameParts = fullName.trim().split("\\s+".toRegex())
        if (nameParts.isEmpty()) return "غير معروف"

        val firstName = nameParts[0]
        val lastName = if (nameParts.size > 1) nameParts.last() else ""
        return "$firstName $lastName".trim()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }

    private fun listenForAlerts(currentSubjectId: String, currentClassId: String) {
        firestore.collection("alerts")
            .whereEqualTo("subjectId", currentSubjectId)
            .whereEqualTo("targetClass", currentClassId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                for (doc in snapshot.documents) {
                    val description = doc.getString("description") ?: continue
                    val sendDate = doc.getString("sendDate") ?: continue
                    val sendTime = doc.getString("sendTime") ?: "00:00 AM"

                    val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)
                    val triggerDate = try {
                        format.parse("$sendDate $sendTime")
                    } catch (ex: Exception) {
                        null
                    } ?: continue

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

    private fun listenForNewLessons(studentGrade: String) {
        firestore.collection("lessons")
            .whereEqualTo("className", studentGrade)
            .whereEqualTo("notifyStudents", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                snapshot.documents.forEach { doc ->
                    val lessonTitle = doc.getString("title") ?: return@forEach
                    val lessonDescription = doc.getString("description") ?: ""
                    val publicationDateStr = doc.getString("publicationDate") ?: ""

                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    val triggerDate = format.parse(publicationDateStr) ?: Date()

                    AlertScheduler.scheduleAlert(
                        context = appContext,
                        alertId = doc.id,
                        title = "درس جديد: $lessonTitle",
                        message = lessonDescription,
                        triggerTime = triggerDate
                    )
                }
            }
    }
}
