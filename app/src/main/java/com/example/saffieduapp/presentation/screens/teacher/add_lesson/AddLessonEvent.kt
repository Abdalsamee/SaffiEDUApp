package com.example.saffieduapp.presentation.screens.teacher.add_lesson

import android.net.Uri

sealed interface AddLessonEvent {
    data class TitleChanged(val title: String) : AddLessonEvent
    data class DescriptionChanged(val description: String) : AddLessonEvent
    data class ClassSelected(val className: String) : AddLessonEvent
    data class VideoSelected(val uri: Uri?) : AddLessonEvent
    data class PdfSelected(val uri: Uri?) : AddLessonEvent
    data class DateChanged(val date: String) : AddLessonEvent
    data class NotifyStudentsToggled(val isEnabled: Boolean) : AddLessonEvent
    data object SaveClicked : AddLessonEvent
    data object ClearVideoSelection : AddLessonEvent
    data object ClearPdfSelection : AddLessonEvent
}