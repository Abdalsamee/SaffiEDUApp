package com.example.saffieduapp.presentation.screens.teacher.add_lesson

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.LessonRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AddLessonViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lessonRepository: LessonRepository // ✅ لازم تنحقن
) : ViewModel() {

    private val _state = MutableStateFlow(AddLessonState())
    val state = _state.asStateFlow()
    val availableGrades = listOf(
        "الصف الأول", "الصف الثاني", "الصف الثالث", "الصف الرابع",
        "الصف الخامس", "الصف السادس", "الصف السابع", "الصف الثامن",
        "الصف التاسع", "الصف العاشر", "الصف الحادي عشر", "الصف الثاني عشر"
    )

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
                _state.update { it.copy(
                    selectedVideoUri = event.uri,
                    selectedVideoName = event.uri?.let { uri -> getFileName(uri) },
                    selectedContentType = if (event.uri != null) ContentType.VIDEO else ContentType.NONE,
                    selectedPdfUri = null,
                    selectedPdfName = null
                ) }
            }
            is AddLessonEvent.PdfSelected -> {
                _state.update { it.copy(
                    selectedPdfUri = event.uri,
                    selectedPdfName = event.uri?.let { uri -> getFileName(uri) },
                    selectedContentType = if (event.uri != null) ContentType.PDF else ContentType.NONE,
                    selectedVideoUri = null,
                    selectedVideoName = null
                ) }
            }
            is AddLessonEvent.ClearVideoSelection -> {
                _state.update { it.copy(
                    selectedVideoUri = null,
                    selectedVideoName = null,
                    // أعد النوع إلى "لا شيء" إذا لم يكن هناك ملف PDF مختار
                    selectedContentType = if (it.selectedPdfUri == null) ContentType.NONE else ContentType.PDF
                ) }
            }
            is AddLessonEvent.ClearPdfSelection -> {
                _state.update { it.copy(
                    selectedPdfUri = null,
                    selectedPdfName = null,
                    // أعد النوع إلى "لا شيء" إذا لم يكن هناك فيديو مختار
                    selectedContentType = if (it.selectedVideoUri == null) ContentType.NONE else ContentType.VIDEO
                ) }
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

            // ✅ التحقق من الحقول الفارغة
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
                // 🔹 رفع ملفات PDF و الفيديو إذا تم اختيارها
                var pdfUrl: String? = null
                var videoUrl: String? = null

                current.selectedPdfUri?.let { uri ->
                    val pdfRef = FirebaseStorage.getInstance()
                        .reference.child("lessons/${current.selectedPdfName ?: "file_${System.currentTimeMillis()}.pdf"}")
                    pdfRef.putFile(uri).await() // استخدام kotlinx.coroutines.tasks.await()
                    pdfUrl = pdfRef.downloadUrl.await().toString()
                }

                current.selectedVideoUri?.let { uri ->
                    val videoRef = FirebaseStorage.getInstance()
                        .reference.child("lessons/${current.selectedVideoName ?: "video_${System.currentTimeMillis()}.mp4"}")
                    videoRef.putFile(uri).await()
                    videoUrl = videoRef.downloadUrl.await().toString()
                }

                // 🔹 تحضير بيانات الدرس مع روابط الملفات
                val lessonData = mapOf(
                    "title" to current.lessonTitle,
                    "description" to current.description,
                    "className" to current.selectedClass,
                    "publicationDate" to current.publicationDate,
                    "notifyStudents" to current.notifyStudents,
                    "isDraft" to isDraft,
                    "createdAt" to System.currentTimeMillis(),
                    "pdfUrl" to pdfUrl,
                    "videoUrl" to videoUrl
                )

                // 🔹 حفظ الدرس في Firestore
                FirebaseFirestore.getInstance().collection("lessons")
                    .add(lessonData)
                    .await()

                // 🔹 إرسال إشعارات إذا لزم الأمر
                if (!isDraft && current.notifyStudents) {
                    lessonRepository.sendNotificationToStudents(
                        className = current.selectedClass,
                        title = current.lessonTitle,
                        description = current.description
                    )
                }

                Toast.makeText(context, "✅ تم حفظ الدرس بنجاح", Toast.LENGTH_SHORT).show()

                // ✅ تفريغ جميع الحقول بعد الحفظ
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
