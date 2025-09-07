package com.example.saffieduapp.presentation.screens.teacher.add_lesson

import android.net.Uri


enum class ContentType {
    NONE, VIDEO, PDF
}
data class AddLessonState(
    val lessonTitle: String = "",
    val description: String = "",
    val selectedClass: String = "", // الصف الذي سيتم إضافة الدرس له
    val selectedVideoUri: Uri? = null,
    val selectedPdfUri: Uri? = null,
    val selectedVideoName: String? = null,
    val selectedPdfName: String? = null,
    val publicationDate: String = "",
    val notifyStudents: Boolean = false,
    val isSaving: Boolean = false,
    val selectedContentType: ContentType = ContentType.NONE
)