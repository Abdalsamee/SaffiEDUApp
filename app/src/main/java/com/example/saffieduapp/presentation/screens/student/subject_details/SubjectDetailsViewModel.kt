package com.example.saffieduapp.presentation.screens.student.subject_details

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.presentation.screens.student.subject_details.components.Lesson
import com.example.saffieduapp.presentation.screens.student.subject_details.components.PdfLesson
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

sealed class DetailsUiEvent {
    data class OpenPdf(val uri: Uri) : DetailsUiEvent()
    data class OpenVideo(val url1: String?, val url: String) : DetailsUiEvent()
    data class ShowToast(val message: String) : DetailsUiEvent()
}

@HiltViewModel
class SubjectDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectDetailsState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DetailsUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val subjectId: String = checkNotNull(savedStateHandle["subjectId"])

    private var pendingPdfReadId: String? = null

    init {
        loadSubjectDetails(subjectId)
        loadLessons()
        loadAlerts()
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            try {
                val snapshot =
                    firestore.collection("alerts").whereEqualTo("subjectId", subjectId.trim()).get()
                        .await()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)
                val now = Calendar.getInstance().time

                val validAlerts = snapshot.documents.mapNotNull { doc ->
                    val message = doc.getString("description") ?: return@mapNotNull null
                    val dateStr = doc.getString("sendDate") ?: return@mapNotNull null
                    val timeStr = doc.getString("sendTime") ?: "00:00 AM"

                    val dateTime = try {
                        dateFormat.parse("$dateStr $timeStr")
                    } catch (e: Exception) {
                        null
                    } ?: return@mapNotNull null

                    // ✅ أظهر التنبيه فقط عند وصول الوقت أو بعده
                    if (dateTime.after(now)) return@mapNotNull null

                    Alert(
                        id = doc.id, message = message, dateTime = dateTime
                    )
                }

                // اختيار آخر تنبيه حسب الوقت
                val lastAlert = validAlerts.maxByOrNull { it.dateTime?.time ?: 0 }

                _state.update { it.copy(alerts = lastAlert?.let { listOf(it) } ?: emptyList()) }

            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(alerts = emptyList()) }
            }
        }
    }

    fun onTabSelected(tab: SubjectTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun loadSubjectDetails(subjectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val allSubjects = listOf(
                Subject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, "", 1),
                Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, "", 2),
                Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 3)
            )
            val subject = allSubjects.find { it.id == subjectId }
            _state.update { it.copy(subject = subject) }
        }
    }

    private suspend fun getStudentClass(studentId: String): String? {
        return try {
            val doc = firestore.collection("students").document(studentId).get().await()
            doc.getString("grade")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadLessons() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val email = FirebaseAuth.getInstance().currentUser?.email
                val querySnapshot =
                    firestore.collection("students").whereEqualTo("email", email).get().await()

                if (querySnapshot.isEmpty) {
                    _state.update {
                        it.copy(
                            isLoading = false, error = "لم يتم العثور على بيانات الطالب"
                        )
                    }
                    return@launch
                }

                // جلب بيانات الطالب من Firestore باستخدام UID
                val studentDoc2 = querySnapshot.documents[0]
                val studentNationalId = studentDoc2.id // هنا رقم الهوية كـ Document ID
                val studentDoc = firestore.collection("students")
                    .document(studentNationalId)  // هنا رقم الهوية مباشرة
                    .get().await()

                val grade = studentDoc.getString("grade")?.trim()
                if (grade.isNullOrEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false, error = "الصف الخاص بالطالب غير موجود"
                        )
                    }
                    return@launch
                }

                // جلب الدروس حسب المادة والصف
                val docs = firestore.collection("lessons")
                    .whereEqualTo("subjectId", subjectId) // درس المادة المطلوبة
                    .whereEqualTo("className", grade)     // جلب الدروس للصف الحالي للطالب
                    .get().await()

                val videoLessons = mutableListOf<Lesson>()
                val pdfLessons = mutableListOf<PdfLesson>()

                // الحصول على التاريخ الحالي
                val currentDate = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH)

                docs.documents.forEach { doc ->
                    val id = doc.id
                    val title = doc.getString("title") ?: return@forEach
                    val description = doc.getString("description") ?: ""
                    val duration = (doc.getLong("duration") ?: 0).toInt()
                    val pagesCount = (doc.getLong("pagesCount") ?: 0).toInt()
                    val publicationDateStr = doc.getString("publicationDate") ?: ""
                    val videoUrl = doc.getString("videoUrl")
                    val pdfUrl = doc.getString("pdfUrl")

                    // تحقق من صحة تاريخ النشر
                    try {
                        val publicationDate = dateFormat.parse(publicationDateStr)

                        // إذا كان تاريخ النشر بعد التاريخ الحالي، تخطى هذا الدرس
                        if (publicationDate != null && publicationDate.after(currentDate)) {
                            return@forEach
                        }
                    } catch (e: Exception) {
                        // إذا كان هناك خطأ في تحليل التاريخ، تخطى هذا الدرس
                        return@forEach
                    }

                    if (!videoUrl.isNullOrEmpty()) {
                        videoLessons.add(
                            Lesson(
                                id = id,
                                title = title,
                                subTitle = description,
                                duration = duration,
                                videoUrl = videoUrl
                            )
                        )
                    }
                    val isRead = doc.getBoolean("isRead") ?: false
                    if (!pdfUrl.isNullOrEmpty()) {
                        pdfLessons.add(
                            PdfLesson(
                                id = id,
                                title = title,
                                subTitle = description,
                                pagesCount = pagesCount,
                                pdfUrl = pdfUrl,
                                isRead = isRead
                            )
                        )
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false, videoLessons = videoLessons, pdfSummaries = pdfLessons
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onVideoCardClick(videoLesson: Lesson) {
        viewModelScope.launch {
            if (videoLesson.videoUrl.isNotEmpty()) {
                _eventFlow.emit(DetailsUiEvent.OpenVideo(videoLesson.id, videoLesson.videoUrl))
            } else {
                _eventFlow.emit(DetailsUiEvent.ShowToast("لا يوجد فيديو"))
            }
        }
    }

    fun onPdfCardClick(pdfLesson: PdfLesson) {
        viewModelScope.launch {
            try {

                // ✅ سنقوم فقط بتخزين الـ ID قبل فتح الملف
                pendingPdfReadId = pdfLesson.id

                // فتح الملف فعليًا
                val localFile = downloadPdf(pdfLesson.pdfUrl!!, pdfLesson.id)
                val uri = FileProvider.getUriForFile(
                    context, "${context.packageName}.provider", localFile
                )
                _eventFlow.emit(DetailsUiEvent.OpenPdf(uri))
            } catch (e: Exception) {
                _eventFlow.emit(DetailsUiEvent.ShowToast("فشل فتح الملف"))
                e.printStackTrace()
            }
        }
    }

    fun onScreenResumed() {
        // التحقق مما إذا كان هناك ملف PDF معلّق تم فتحه
        pendingPdfReadId?.let { idToMark ->
            // امسح الـ ID فورًا لمنع تشغيله عدة مرات
            pendingPdfReadId = null

            // قم بتشغيل التحديث في coroutine
            setPdfAsRead(idToMark)
        }
    }

    private fun setPdfAsRead(pdfId: String) {
        viewModelScope.launch {
            try {
                // ✅ تحديث حالة الملف كمقروء في Firestore
                firestore.collection("lessons").document(pdfId).update("isRead", true).await()

                // ✅ تحديث الـ State محليًا
                _state.update { currentState ->
                    currentState.copy(
                        pdfSummaries = currentState.pdfSummaries.map {
                            if (it.id == pdfId) it.copy(isRead = true) else it
                        })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun downloadPdf(pdfUrl: String, id: String): File {
        val file = File(context.filesDir, "$id.pdf")
        if (!file.exists()) {
            withContext(Dispatchers.IO) {
                URL(pdfUrl).openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
        return file
    }

}