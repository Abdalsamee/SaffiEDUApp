package com.example.saffieduapp.presentation.screens.teacher.add_lesson

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.LessonRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddLessonViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lessonRepository: LessonRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(AddLessonState())
    val state = _state.asStateFlow()

    private val MAX_FILE_SIZE = 200L * 1024L * 1024L // 200 ميغابايت

    private suspend fun fetchTeacherAndSubjectIds(): Pair<String?, String?> {
        return try {
            val currentUserEmail = auth.currentUser?.email ?: return null to null
            val teacherSnapshot = firestore.collection("teachers")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .await()
            if (teacherSnapshot.isEmpty) return null to null
            val teacherDoc = teacherSnapshot.documents[0]
            val teacherId = teacherDoc.id
            val subjectsSnapshot = firestore.collection("subjects")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .await()
            val subjectId = if (subjectsSnapshot.isEmpty) null else subjectsSnapshot.documents[0].id
            teacherId to subjectId
        } catch (e: Exception) {
            e.printStackTrace()
            null to null
        }
    }

    fun onEvent(event: AddLessonEvent) {
        when (event) {
            is AddLessonEvent.TitleChanged -> _state.update { it.copy(lessonTitle = event.title) }
            is AddLessonEvent.DescriptionChanged -> _state.update { it.copy(description = event.description) }
            is AddLessonEvent.ClassSelected -> _state.update { it.copy(selectedClass = event.className) }
            is AddLessonEvent.VideoSelected -> _state.update {
                it.copy(
                    selectedVideoUri = event.uri,
                    selectedVideoName = event.uri?.let { uri -> getFileName(uri) },
                    selectedContentType = if (event.uri != null) ContentType.VIDEO else ContentType.NONE,
                    selectedPdfUri = null,
                    selectedPdfName = null
                )
            }
            is AddLessonEvent.PdfSelected -> _state.update {
                it.copy(
                    selectedPdfUri = event.uri,
                    selectedPdfName = event.uri?.let { uri -> getFileName(uri) },
                    selectedContentType = if (event.uri != null) ContentType.PDF else ContentType.NONE,
                    selectedVideoUri = null,
                    selectedVideoName = null,
                    description = ""
                )
            }
            is AddLessonEvent.ClearVideoSelection -> _state.update {
                it.copy(
                    selectedVideoUri = null,
                    selectedVideoName = null,
                    selectedContentType = if (it.selectedPdfUri == null) ContentType.NONE else ContentType.PDF
                )
            }
            is AddLessonEvent.ClearPdfSelection -> _state.update {
                it.copy(
                    selectedPdfUri = null,
                    selectedPdfName = null,
                    selectedContentType = if (it.selectedVideoUri == null) ContentType.NONE else ContentType.VIDEO
                )
            }
            is AddLessonEvent.DateChanged -> _state.update { it.copy(publicationDate = event.date) }
            is AddLessonEvent.NotifyStudentsToggled -> _state.update { it.copy(notifyStudents = event.isEnabled) }
            is AddLessonEvent.SaveClicked -> saveLesson()
        }
    }

    private fun saveLesson(isDraft: Boolean = false) {
        viewModelScope.launch {
            val current = state.value

            // تحقق من الحقول الأساسية
            if (current.lessonTitle.isBlank()) {
                Toast.makeText(context, "يرجى إدخال عنوان الدرس", Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (current.selectedClass.isBlank()) {
                Toast.makeText(context, "يرجى اختيار الصف", Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (current.publicationDate.isBlank()) {
                Toast.makeText(context, "يرجى اختيار تاريخ النشر", Toast.LENGTH_SHORT).show()
                return@launch
            }

            _state.update { it.copy(isSaving = true) }

            try {
                val (teacherId, subjectId) = fetchTeacherAndSubjectIds()
                if (teacherId == null || subjectId == null) {
                    Toast.makeText(context, "❌ لم يتم العثور على بيانات المعلم أو المادة", Toast.LENGTH_LONG).show()
                    _state.update { it.copy(isSaving = false) }
                    return@launch
                }

                var pdfUrl: String? = null
                var videoUrl: String? = null
                var pagesCount = 0

                // رفع الفيديو إذا موجود
                current.selectedVideoUri?.let { uri ->
                    val fileSize = getFileSize(uri)
                    if (fileSize > MAX_FILE_SIZE) {
                        Toast.makeText(context, "حجم الفيديو كبير جداً. الحد الأقصى 200 ميغابايت", Toast.LENGTH_LONG).show()
                        _state.update { it.copy(isSaving = false) }
                        return@launch
                    }
                    videoUrl = lessonRepository.uploadFile(
                        "lessons/videos/${System.currentTimeMillis()}_${getFileName(uri)}",
                        uri
                    )
                }

                // رفع PDF وحساب عدد الصفحات
                current.selectedPdfUri?.let { uri ->
                    val fileSize = getFileSize(uri)
                    if (fileSize > MAX_FILE_SIZE) {
                        Toast.makeText(context, "حجم الملف كبير جداً. الحد الأقصى 200 ميغابايت", Toast.LENGTH_LONG).show()
                        _state.update { it.copy(isSaving = false) }
                        return@launch
                    }

                    // تحويل Uri إلى File مؤقت
                    val pdfFile = uriToFile(uri)

                    // حساب عدد الصفحات
                    pagesCount = getPdfPageCount(pdfFile)

                    // رفع الملف
                    pdfUrl = lessonRepository.uploadFile(
                        "lessons/pdf/${System.currentTimeMillis()}_${getFileName(uri)}",
                        uri
                    )
                }

                val lessonData = mapOf(
                    "title" to current.lessonTitle,
                    "description" to current.description,
                    "className" to current.selectedClass,
                    "publicationDate" to current.publicationDate,
                    "notifyStudents" to current.notifyStudents,
                    "isDraft" to isDraft,
                    "createdAt" to System.currentTimeMillis(),
                    "pdfUrl" to pdfUrl,
                    "videoUrl" to videoUrl,
                    "pagesCount" to pagesCount, // العدد الحقيقي للصفحات
                    "subjectId" to subjectId,
                    "teacherId" to teacherId,
                    "notificationStatus" to "pending",
                    "isNotified" to false
                )

                lessonRepository.saveLessonAndReturnId(lessonData)

                Toast.makeText(context, "✅ تم حفظ الدرس بنجاح", Toast.LENGTH_SHORT).show()

                // إعادة تعيين الحالة
                _state.update {
                    it.copy(
                        lessonTitle = "",
                        description = "",
                        selectedClass = "",
                        publicationDate = "",
                        selectedVideoUri = null,
                        selectedVideoName = null,
                        selectedPdfUri = null,
                        selectedPdfName = null,
                        selectedContentType = ContentType.NONE,
                        notifyStudents = false,
                        isSaving = false
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "❌ فشل حفظ الدرس: ${e.message}", Toast.LENGTH_LONG).show()
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    // الحصول على اسم الملف من Uri
    private fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    // الحصول على حجم الملف
    private fun getFileSize(uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            cursor.getLong(sizeIndex)
        } ?: 0L
    }

    // تحويل Uri إلى File مؤقت
    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val tempFile = File(context.cacheDir, getFileName(uri) ?: "temp.pdf")
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    // حساب عدد صفحات PDF
    fun getPdfPageCount(file: File): Int {
        val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(parcelFileDescriptor)
        val count = pdfRenderer.pageCount
        pdfRenderer.close()
        parcelFileDescriptor.close()
        return count
    }
}
