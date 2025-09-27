package com.example.saffieduapp.presentation.screens.teacher.add_lesson

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.DraftLessonManager
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

    // إضافة MutableStateFlow للتحكم في حالة زر حفظ المسودة
    private val _isDraftSaved = MutableStateFlow(false)
    val isDraftSaved = _isDraftSaved.asStateFlow()

    private val MAX_FILE_SIZE = 200L * 1024L * 1024L // 200 ميغابايت

    private val draftManager = DraftLessonManager(context)

    init {
        viewModelScope.launch {
            draftManager.draftFlow.collect { (draft, isSaved) ->
                draft?.let {
                    _state.update { current ->
                        current.copy(
                            lessonTitle = it.lessonTitle,
                            description = it.description,
                            selectedClass = it.selectedClass,
                            selectedVideoUriString = it.selectedVideoUriString,
                            selectedPdfUriString = it.selectedPdfUriString,
                            selectedVideoName = it.selectedVideoName,
                            selectedPdfName = it.selectedPdfName,
                            publicationDate = it.publicationDate,
                            notifyStudents = it.notifyStudents,
                            selectedContentType = it.selectedContentType
                        )
                    }
                }
                _isDraftSaved.value = isSaved
            }
        }
    }

    private fun saveDraft() {
        viewModelScope.launch {
            draftManager.saveDraft(state.value)
        }
    }

    fun onEvent(event: AddLessonEvent) {
        when (event) {
            is AddLessonEvent.SaveDraftClicked -> {
                viewModelScope.launch {
                    draftManager.saveDraft(state.value, isButtonClick = true)
                    _isDraftSaved.value = true // الزر أصبح تم الحفظ
                }
            }
            is AddLessonEvent.SaveClicked -> {
                saveLesson(isDraft = false) // ← هنا بيتم تشغيل الحفظ الحقيقي
            }
            is AddLessonEvent.TitleChanged,
            is AddLessonEvent.DescriptionChanged,
            is AddLessonEvent.ClassSelected,
            is AddLessonEvent.VideoSelected,
            is AddLessonEvent.PdfSelected,
            is AddLessonEvent.DateChanged,
            is AddLessonEvent.NotifyStudentsToggled -> {
                updateStateFromEvent(event)
                _isDraftSaved.value = false
            }
            else -> {}
        }
    }


    private fun updateStateFromEvent(event: AddLessonEvent) {
        when (event) {
            is AddLessonEvent.TitleChanged -> _state.update { it.copy(lessonTitle = event.title) }
            is AddLessonEvent.DescriptionChanged -> _state.update { it.copy(description = event.description) }
            is AddLessonEvent.ClassSelected -> _state.update { it.copy(selectedClass = event.className) }
            is AddLessonEvent.VideoSelected -> _state.update {
                it.copy(
                    selectedVideoUriString = event.uri.toString(),
                    selectedVideoName = event.uri?.let { uri -> getFileName(uri) },
                    selectedContentType = if (event.uri != null) ContentType.VIDEO else ContentType.NONE,
                    selectedPdfUriString = null,
                    selectedPdfName = null
                )
            }
            is AddLessonEvent.PdfSelected -> _state.update {
                it.copy(
                    selectedPdfUriString = event.uri.toString(),
                    selectedPdfName = event.uri?.let { uri -> getFileName(uri) },
                    selectedContentType = if (event.uri != null) ContentType.PDF else ContentType.NONE,
                    selectedVideoUriString = null,
                    selectedVideoName = null,
                    description = ""
                )
            }
            is AddLessonEvent.DateChanged -> _state.update { it.copy(publicationDate = event.date) }
            is AddLessonEvent.NotifyStudentsToggled -> _state.update { it.copy(notifyStudents = event.isEnabled) }
            else -> {}
        }
        // إعادة الزر إلى الحالة الطبيعية عند أي تعديل
        _isDraftSaved.value = false
    }

    private suspend fun fetchSubjectId(teacherId: String, className: String): String? {
        return try {
            // جلب المادة التي تطابق المعلم والصف المحدد
            val subjectsSnapshot = firestore.collection("subjects")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("className", className) // ← استخدام className بدلاً من subjectName
                .get()
                .await()

            if (subjectsSnapshot.isEmpty) {
                println("❌ لم يتم العثور على مادة للمعلم $teacherId في الصف $className")
                null
            } else {
                val subjectDoc = subjectsSnapshot.documents[0]
                println("✅ تم العثور على المادة: ${subjectDoc.id} للصف $className")
                subjectDoc.id
            }
        } catch (e: Exception) {
            println("❌ خطأ في البحث عن المادة: ${e.message}")
            null
        }
    }

    private suspend fun getTeacherSubjectName(teacherId: String): String? {
        return try {
            val teacherDoc = firestore.collection("teachers")
                .document(teacherId)
                .get()
                .await()

            teacherDoc.getString("subject") // اسم المادة من بيانات المعلم
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchTeacherAndSubjectIds(selectedClassName: String): Triple<String?, String?, String?> {
        return try {
            val currentUserEmail = auth.currentUser?.email ?: return Triple(null, null, null)

            // جلب بيانات المعلم
            val teacherSnapshot = firestore.collection("teachers")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .await()

            if (teacherSnapshot.isEmpty) return Triple(null, null, null)

            val teacherDoc = teacherSnapshot.documents[0]
            val teacherId = teacherDoc.id

            // جلب اسم المادة من بيانات المعلم
            val subjectName = getTeacherSubjectName(teacherId)

            // البحث عن المادة التي تطابق المعلم والصف المحدد
            val subjectId = fetchSubjectId(teacherId, selectedClassName)

            Triple(teacherId, subjectId, subjectName)
        } catch (e: Exception) {
            e.printStackTrace()
            Triple(null, null, null)
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
                // استخدام الصف المحدد في البحث
                val (teacherId, subjectId, subjectName) = fetchTeacherAndSubjectIds(current.selectedClass)

                if (teacherId == null) {
                    Toast.makeText(context, "❌ لم يتم العثور على بيانات المعلم", Toast.LENGTH_LONG).show()
                    _state.update { it.copy(isSaving = false) }
                    return@launch
                }

                if (subjectId == null) {
                    Toast.makeText(context, "❌ لم يتم العثور على المادة للصف ${current.selectedClass}", Toast.LENGTH_LONG).show()
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

                    val pdfFile = uriToFile(uri)
                    pagesCount = getPdfPageCount(pdfFile)

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
                    "pagesCount" to pagesCount,
                    "subjectId" to subjectId,
                    "subjectName" to subjectName, // إضافة اسم المادة
                    "teacherId" to teacherId,
                    "notificationStatus" to "pending",
                    "isNotified" to false
                )

                lessonRepository.saveLessonAndReturnId(lessonData)

                Toast.makeText(context, "✅ تم حفظ الدرس ${current.selectedClass}", Toast.LENGTH_SHORT).show()

                // إعادة تعيين الحالة
                _state.update {
                    it.copy(
                        lessonTitle = "",
                        description = "",
                        selectedClass = "",
                        publicationDate = "",
                        selectedVideoUriString = null,
                        selectedVideoName = null,
                        selectedPdfUriString = null,
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
