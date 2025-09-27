package com.example.saffieduapp.presentation.screens.teacher.add_lesson

import android.net.Uri


enum class ContentType {
    NONE, VIDEO, PDF
}
data class AddLessonState(
    val lessonTitle: String = "",
    val description: String = "",
    val selectedClass: String = "",
    val selectedVideoUriString: String? = null, // حفظ كـ String
    val selectedPdfUriString: String? = null,   // حفظ كـ String
    val selectedVideoName: String? = null,
    val selectedPdfName: String? = null,
    val publicationDate: String = "",
    val notifyStudents: Boolean = false,
    val isSaving: Boolean = false,
    val selectedContentType: ContentType = ContentType.NONE
) {
    val selectedVideoUri: Uri? get() = selectedVideoUriString?.let { Uri.parse(it) }
    val selectedPdfUri: Uri? get() = selectedPdfUriString?.let { Uri.parse(it) }
}
