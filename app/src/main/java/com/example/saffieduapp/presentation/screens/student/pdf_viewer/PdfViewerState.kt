package com.example.saffieduapp.presentation.screens.student.pdf_viewer

import java.io.File

data class PdfViewerState(
    val isLoading: Boolean = true,
    val localFile: File? = null,
    val error: String? = null
)