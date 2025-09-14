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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// --- الأحداث التي سترسل للواجهة ---
sealed class DetailsUiEvent {
    data class OpenPdf(val uri: Uri) : DetailsUiEvent()
    data class OpenVideo(val uri: Uri) : DetailsUiEvent()
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
        loadLessons()
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
    }

    private fun loadSubjectDetails() {
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

    private fun loadLessons() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val docs = firestore.collection("lessons")
                    .whereEqualTo("subjectId", subjectId)
                    .get().await()

                val videoLessons = mutableListOf<Lesson>()
                val pdfLessons = mutableListOf<PdfLesson>()

                docs.documents.forEach { doc ->
                    val id = doc.id
                    val title = doc.getString("title") ?: return@forEach
                    var description = doc.getString("description") ?: ""
                    val videoBase64 = doc.getString("videoBase64")
                    val pdfBase64 = doc.getString("pdfBase64")
                    val duration = (doc.getLong("duration") ?: 0).toInt()
                    val pagesCount = (doc.getLong("pagesCount") ?: 0).toInt()

                    if (description.contains("df", ignoreCase = true)) description = ""

                    // حفظ الفيديو
                    if (!videoBase64.isNullOrEmpty()) {
                        val videoFile = saveBase64ToFile("${id}_${System.currentTimeMillis()}", videoBase64, "mp4")
                        videoLessons.add(
                            Lesson(
                                id = id,
                                title = title,
                                subTitle = description,
                                duration = duration,
                                videoFile = videoFile,
                                progress = 0f,
                                base64 = null
                            )
                        )
                    }

                    // حفظ PDF
                    if (!pdfBase64.isNullOrEmpty()) {
                        val pdfFile = saveBase64ToFile("${id}_${System.currentTimeMillis()}", pdfBase64, "pdf")
                        pdfLessons.add(
                            PdfLesson(
                                id = id,
                                title = title,
                                subTitle = description,
                                pagesCount = pagesCount,
                                isRead = false,
                                pdfFile = pdfFile,
                                base64 = null
                            )
                        )
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        videoLessons = videoLessons,
                        pdfSummaries = pdfLessons
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onPdfCardClick(pdfLesson: PdfLesson) {
        viewModelScope.launch {
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfLesson.pdfFile)
                _eventFlow.emit(DetailsUiEvent.OpenPdf(uri))
            } catch (e: Exception) {
                _eventFlow.emit(DetailsUiEvent.ShowToast("فشل فتح الملف"))
                e.printStackTrace()
            }
        }
    }

    fun onVideoCardClick(videoLesson: Lesson) {
        viewModelScope.launch {
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", videoLesson.videoFile)
                _eventFlow.emit(DetailsUiEvent.OpenVideo(uri))
            } catch (e: Exception) {
                _eventFlow.emit(DetailsUiEvent.ShowToast("فشل تشغيل الفيديو"))
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveBase64ToFile(fileId: String, base64Data: String, extension: String): File {
        val file = File(context.filesDir, "$fileId.$extension")
        if (!file.exists()) {
            val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            withContext(Dispatchers.IO) {
                file.outputStream().use { it.write(bytes) }
            }
        }
        return file
    }

    fun updatePdfLessonReadStatus(lesson: PdfLesson, isRead: Boolean) {
        viewModelScope.launch {
            _state.update { current ->
                val updated = current.pdfSummaries.map {
                    if (it.id == lesson.id) it.copy(isRead = isRead) else it
                }
                current.copy(pdfSummaries = updated)
            }
        }
    }
}
