package com.example.saffieduapp.presentation.screens.teacher.add_assignment

import android.net.Uri

sealed interface AddAssignmentEvent {
    data class TitleChanged(val title: String) : AddAssignmentEvent
    data class DescriptionChanged(val description: String) : AddAssignmentEvent
    data class DateChanged(val date: String) : AddAssignmentEvent
    data class ClassSelected(val className: String) : AddAssignmentEvent
    data class ImageSelected(val uri: Uri?) : AddAssignmentEvent
    data object SaveClicked : AddAssignmentEvent
}