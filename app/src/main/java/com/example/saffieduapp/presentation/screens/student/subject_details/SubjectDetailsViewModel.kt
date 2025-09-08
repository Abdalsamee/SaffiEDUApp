package com.example.saffieduapp.presentation.screens.student.subject_details

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.model.Subject // Assuming this is in domain/model
// Use the imports as they exist in your project

import com.example.saffieduapp.presentation.screens.student.subject_details.components.Lesson
import com.example.saffieduapp.presentation.screens.student.subject_details.components.PdfLesson
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
    // --- الإضافة ٢: حقن Context ---
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectDetailsState())
    val state = _state.asStateFlow()

    // --- الإضافة ٣: إنشاء مجرى لإرسال الأحداث ---
    private val _eventFlow = MutableSharedFlow<DetailsUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // المعرف القادم من Navigation
    private val subjectId: String = checkNotNull(savedStateHandle["subjectId"])

    // --- الكود الأصلي الخاص بك (بقي كما هو) ---
    init {
        loadSubjectDetails()
        loadVideoLessons()
        loadAlerts()
        println("ViewModel: Received and will search for ID -> $subjectId")
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

    private fun loadVideoLessons() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(500)
            val lessons = listOf(
                Lesson(id = "l1", title = "الدرس الأول", subTitle = "شرح سورة نوح", duration = 15, imageUrl = "", progress = 30f),
                Lesson(id = "l2", title = "الدرس الثاني", subTitle = "شرح سورة البقرة", duration = 22, imageUrl = "", progress = 50f)
            )
            _state.update { it.copy(isLoading = false, videoLessons = lessons) }
        }
    }

    private fun loadPdfSummaries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(500)
            val pdfs = listOf(
                PdfLesson(id = "p1", title = "الوحدة الأولى", subTitle = "النحو والصرف", pagesCount = 12, isRead = false, imageUrl = "", pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"),
                PdfLesson(id = "p2", title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = false, imageUrl = "", pdfUrl = "https://bitcoin.org/bitcoin.pdf")
            )
            _state.update { it.copy(isLoading = false, pdfSummaries = pdfs) }
        }
    }

    // --- الإضافة ٤: الدوال الجديدة الخاصة بالـ PDF ---
    fun onPdfCardClick(pdfId: String, pdfUrl: String) {
        viewModelScope.launch {
            try {
                _eventFlow.emit(DetailsUiEvent.ShowToast("جاري تجهيز الملف..."))
                val localFile = getOrDownloadFile(pdfId, pdfUrl)
                val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", localFile)
                _eventFlow.emit(DetailsUiEvent.OpenPdf(fileUri))
            } catch (e: Exception) {
                _eventFlow.emit(DetailsUiEvent.ShowToast("فشل تحميل الملف"))
                e.printStackTrace()
            }
        }
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
}
    // --- التعامل مع فتح ملفات PDF ---
    fun onPdfCardClick(pdfId: String, pdfUrl: String) {
        viewModelScope.launch {
            try {
                _eventFlow.emit(DetailsUiEvent.ShowToast("جاري تجهيز الملف..."))
                val localFile = getOrDownloadFile(pdfId, pdfUrl)
                val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", localFile)
                _eventFlow.emit(DetailsUiEvent.OpenPdf(fileUri))
            } catch (e: Exception) {
                _eventFlow.emit(DetailsUiEvent.ShowToast("فشل تحميل الملف"))
                e.printStackTrace()
            }
        }
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

    // --- تحديث حالة قراءة PDF ---
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