package com.example.saffieduapp.presentation.screens.teacher.add_assignment
import android.net.Uri

data class AddAssignmentState(
    val title: String = "",
    val description: String = "",
    val dueDate: String = "", // تاريخ التسليم
    val selectedClass: String = "",
    val selectedImageUri: Uri? = null,
    val selectedImageName: String? = null,
    val isSaving: Boolean = false
)