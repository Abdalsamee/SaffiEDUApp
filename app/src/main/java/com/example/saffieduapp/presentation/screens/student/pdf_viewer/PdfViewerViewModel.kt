package com.example.saffieduapp.presentation.screens.student.pdf_viewer

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(PdfViewerState())
    val state = _state.asStateFlow()

    init {
        val pdfUrl = savedStateHandle.get<String>("pdfUrl")
        val pdfId = savedStateHandle.get<String>("pdfId") ?: "dummy_pdf"

        if (pdfUrl != null) {
            loadPdf(pdfUrl, pdfId)
        } else {
            // يمكنك تعيين حالة الخطأ مباشرة
            _state.value = _state.value.copy(isLoading = false, error = "لا يوجد رابط PDF")
        }
    }

    fun loadPdf(pdfUrl: String, pdfId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val localFile = File(context.filesDir, "$pdfId.pdf")

            if (!localFile.exists()) {
                try {
                    withContext(Dispatchers.IO) {
                        URL(pdfUrl).openStream().use { input ->
                            localFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(isLoading = false, error = "فشل تحميل الملف")
                    return@launch
                }
            }

            _state.value = _state.value.copy(isLoading = false, localFile = localFile)
        }
    }
}
