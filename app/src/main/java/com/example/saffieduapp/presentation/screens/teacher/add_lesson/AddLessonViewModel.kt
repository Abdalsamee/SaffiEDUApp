package com.example.saffieduapp.presentation.screens.teacher.add_lesson

import android.content.Context
import android.net.Uri
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
import javax.inject.Inject
import android.util.Base64

@HiltViewModel
class AddLessonViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lessonRepository: LessonRepository, // ✅ لازم تنحقن
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(AddLessonState())
    val state = _state.asStateFlow()
    val availableGrades = listOf(
        "الصف الأول", "الصف الثاني", "الصف الثالث", "الصف الرابع",
        "الصف الخامس", "الصف السادس", "الصف السابع", "الصف الثامن",
        "الصف التاسع", "الصف العاشر", "الصف الحادي عشر", "الصف الثاني عشر"
    )

    private suspend fun fetchTeacherAndSubjectIds(): Pair<String?, String?> {
        return try {
            val currentUserEmail = auth.currentUser?.email
            if (currentUserEmail.isNullOrEmpty()) return null to null

            // جلب المستند الخاص بالمعلم
            val teacherSnapshot = firestore.collection("teachers")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .await()

            if (teacherSnapshot.isEmpty) return null to null

            val teacherDoc = teacherSnapshot.documents[0]
            val teacherId = teacherDoc.id

            // جلب أول مادة مرتبطة بهذا المعلم
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
        // --- تم تنظيف جملة when من التكرار ---
        when (event) {
            is AddLessonEvent.TitleChanged -> {
                _state.update { it.copy(lessonTitle = event.title) }
            }

            is AddLessonEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description) }
            }

            is AddLessonEvent.ClassSelected -> {
                _state.update { it.copy(selectedClass = event.className) }
            }

            is AddLessonEvent.VideoSelected -> {
                _state.update {
                    it.copy(
                        selectedVideoUri = event.uri,
                        selectedVideoName = event.uri?.let { uri -> getFileName(uri) },
                        selectedContentType = if (event.uri != null) ContentType.VIDEO else ContentType.NONE,
                        selectedPdfUri = null,
                        selectedPdfName = null
                    )
                }
            }

            is AddLessonEvent.PdfSelected -> {
                _state.update {
                    it.copy(
                        selectedPdfUri = event.uri,
                        selectedPdfName = event.uri?.let { uri -> getFileName(uri) },
                        selectedContentType = if (event.uri != null) ContentType.PDF else ContentType.NONE,
                        selectedVideoUri = null,
                        selectedVideoName = null
                    )
                }
            }

            is AddLessonEvent.ClearVideoSelection -> {
                _state.update {
                    it.copy(
                        selectedVideoUri = null,
                        selectedVideoName = null,
                        // أعد النوع إلى "لا شيء" إذا لم يكن هناك ملف PDF مختار
                        selectedContentType = if (it.selectedPdfUri == null) ContentType.NONE else ContentType.PDF
                    )
                }
            }

            is AddLessonEvent.ClearPdfSelection -> {
                _state.update {
                    it.copy(
                        selectedPdfUri = null,
                        selectedPdfName = null,
                        // أعد النوع إلى "لا شيء" إذا لم يكن هناك فيديو مختار
                        selectedContentType = if (it.selectedVideoUri == null) ContentType.NONE else ContentType.VIDEO
                    )
                }
            }

            is AddLessonEvent.SaveClicked -> {
                saveLesson()
            }

            is AddLessonEvent.DateChanged -> {
                _state.update { it.copy(publicationDate = event.date) }
            }

            is AddLessonEvent.NotifyStudentsToggled -> {
                _state.update { it.copy(notifyStudents = event.isEnabled) }
            }

            is AddLessonEvent.SaveClicked -> {
                saveLesson()
            }
        }
    }

    private fun saveLesson(isDraft: Boolean = false) {
        viewModelScope.launch {
            val current = state.value

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

                // تحويل ملفات PDF والفيديو إلى Base64
                val pdfBase64: String? = current.selectedPdfUri?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        Base64.encodeToString(inputStream.readBytes(), Base64.DEFAULT)
                    }
                }

                val videoBase64: String? = current.selectedVideoUri?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        Base64.encodeToString(inputStream.readBytes(), Base64.DEFAULT)
                    }
                }

                val lessonData = mapOf(
                    "title" to current.lessonTitle,
                    "description" to current.description,
                    "className" to current.selectedClass,
                    "publicationDate" to current.publicationDate,
                    "notifyStudents" to current.notifyStudents,
                    "isDraft" to isDraft,
                    "createdAt" to System.currentTimeMillis(),
                    "pdfBase64" to pdfBase64,
                    "videoBase64" to videoBase64,
                    "subjectId" to subjectId,
                    "teacherId" to teacherId
                )

                firestore.collection("lessons").add(lessonData).await()

                if (!isDraft && current.notifyStudents) {
                    lessonRepository.sendNotificationToStudents(
                        className = current.selectedClass,
                        title = current.lessonTitle,
                        description = current.description
                    )
                }

                Toast.makeText(context, "✅ تم حفظ الدرس بنجاح", Toast.LENGTH_SHORT).show()
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
                        notifyStudents = false
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "❌ فشل حفظ الدرس: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }


    // --- تم نقل الدالة إلى داخل الكلاس ---
    private fun getFileName(uri: Uri): String? {
        // الآن يمكنها الوصول إلى context بشكل صحيح
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }
}
