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
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import javax.inject.Inject

// --- الأحداث التي سترسل للواجهة ---
sealed class DetailsUiEvent {
    data class OpenPdf(val uri: Uri) : DetailsUiEvent()
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

    init {
        loadSubjectDetails()
        loadVideoLessons()
        loadAlerts()
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun loadAlerts() {
        val sampleAlerts = listOf(
            Alert(id = "1", message = "تم إلغاء الدرس الأول بعنوان شرح سورة نوح")
        )
        _state.update { it.copy(alerts = sampleAlerts) }
    }

    fun onTabSelected(tab: SubjectTab) {
        _state.update { it.copy(selectedTab = tab) }
        if (tab == SubjectTab.PDFS && state.value.pdfSummaries.isEmpty()) {
            loadPdfSummaries()
        }
    }

    private fun loadSubjectDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(200)
            val allSubjects = listOf(
                Subject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, "", 1),
                Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, "", 2),
                Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 3)
            )
            val subject = allSubjects.find { it.id == subjectId }
            _state.update { it.copy(subject = subject) }
        }
    }

    // --- تحميل الفيديوهات والـ PDF ---
    private fun loadVideoLessons() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val lessonsDocs = firestore.collection("lessons")
                    .whereEqualTo("subjectId", subjectId)
                    .get().await()

                val videoLessons = mutableListOf<Lesson>()
                val pdfLessons = mutableListOf<PdfLesson>()

                lessonsDocs.documents.forEach { doc ->
                    val title = doc.getString("title") ?: return@forEach
                    var description = doc.getString("description") ?: ""
                    val videoBase64 = doc.getString("videoBase64")
                    val pdfBase64 = doc.getString("pdfBase64")
                    val duration = (doc.getLong("duration") ?: 0).toInt()
                    val pagesCount = (doc.getLong("pagesCount") ?: 0).toInt()

                    if (description.contains("df", ignoreCase = true)) description = ""

                    if (!videoBase64.isNullOrEmpty()) {
                        val videoFile = saveBase64ToFile(doc.id, videoBase64, "mp4")
                        videoLessons.add(
                            Lesson(
                                id = doc.id,
                                title = title,
                                subTitle = description,
                                duration = duration,
                                videoFile = videoFile,
                                progress = 0f
                            )
                        )
                    }

                    if (!pdfBase64.isNullOrEmpty()) {
                        pdfLessons.add(
                            PdfLesson(
                                id = doc.id,
                                title = title,
                                subTitle = description,
                                pagesCount = pagesCount,
                                isRead = false,
                                pdfFile = saveBase64ToFile(doc.id, pdfBase64, "pdf"),
                                base64 = pdfBase64
                            )
                        )
                    }
                }

                _state.update {
                    it.copy(isLoading = false, videoLessons = videoLessons, pdfSummaries = pdfLessons)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadPdfSummaries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val pdfDocs = firestore.collection("lessons")
                    .whereEqualTo("subjectId", subjectId)
                    .whereLessThanOrEqualTo("publicationDate", System.currentTimeMillis())
                    .get().await()

                val pdfs = pdfDocs.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    var subTitle = doc.getString("description") ?: ""
                    val pdfUrl = doc.getString("pdfUrl") ?: return@mapNotNull null
                    val pagesCount = (doc.getLong("pagesCount") ?: 0).toInt()

                    // ✅ تعطيل الوصف إذا يحتوي df
                    if (subTitle.contains("df", ignoreCase = true)) {
                        subTitle = ""
                    }

                    PdfLesson(
                        id = doc.id,
                        title = title,
                        subTitle = subTitle,
                        pagesCount = pagesCount,
                        isRead = false,
                        imageUrl = "",
                        pdfUrl = pdfUrl
                    )
                }

                _state.update { it.copy(isLoading = false, pdfSummaries = pdfs) }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- التعامل مع فتح ملفات PDF ---
    fun onPdfCardClick(pdfLesson: PdfLesson) {
        viewModelScope.launch {
            try {
                _eventFlow.emit(DetailsUiEvent.ShowToast("جاري تجهيز الملف..."))
                val file = saveBase64ToFile(pdfLesson.id, pdfLesson.base64 ?: "", "pdf")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                _eventFlow.emit(DetailsUiEvent.OpenPdf(uri))
            } catch (e: Exception) {
                _eventFlow.emit(DetailsUiEvent.ShowToast("فشل فتح الملف"))
                e.printStackTrace()
            }
        }
    }
    private suspend fun saveBase64ToFile(fileId: String, base64Data: String, extension: String): File {
        val localFile = File(context.filesDir, "$fileId.$extension")
        if (!localFile.exists()) {
            val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            withContext(Dispatchers.IO) {
                localFile.outputStream().use { it.write(bytes) }
            }
        }
        return localFile
    }

    private suspend fun getOrDownloadFile(fileId: String, fileUrl: String): File {
        val localFile = File(context.filesDir, "$fileId.pdf")
        if (localFile.exists()) {
            return localFile
        } else {
            withContext(Dispatchers.IO) {
                URL(fileUrl).openStream().use { input ->
                    localFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            return localFile
        }
    }

    fun updatePdfLessonReadStatus(lesson: PdfLesson, isRead: Boolean) {
        viewModelScope.launch {
            _state.update { currentState ->
                val updatedPdfs = currentState.pdfSummaries.map { pdf ->
                    if (pdf.id == lesson.id) {
                        pdf.copy(isRead = isRead)
                    } else {
                        pdf
                    }
                }
                currentState.copy(pdfSummaries = updatedPdfs)
            }
        }
    }
}
