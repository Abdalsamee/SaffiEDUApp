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
        // استلام البيانات التي تم تمريرها. لاحقًا ستأتي من شاشة تفاصيل المادة
        val pdfUrl = savedStateHandle.get<String>("pdfUrl") ?: "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
        val pdfId = savedStateHandle.get<String>("pdfId") ?: "dummy_pdf_1"

        loadPdf(pdfUrl, pdfId)
    }

    private fun loadPdf(pdfUrl: String, pdfId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // ١. تحديد مسار الملف في الذاكرة الداخلية للتطبيق
            val localFile = File(context.filesDir, "$pdfId.pdf")

            // ٢. التحقق إذا كان الملف موجودًا (تم تنزيله مسبقًا)
            if (localFile.exists()) {
                _state.value = PdfViewerState(isLoading = false, localFile = localFile)
                return@launch
            }

            // ٣. إذا لم يكن موجودًا، قم بتنزيله
            try {
                // نستخدم withContext(Dispatchers.IO) لأن عمليات الشبكة والملفات يجب أن تتم في الخلفية
                withContext(Dispatchers.IO) {
                    URL(pdfUrl).openStream().use { input ->
                        localFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                // بعد نجاح التنزيل، قم بتحديث الحالة
                _state.value = PdfViewerState(isLoading = false, localFile = localFile)
            } catch (e: Exception) {
                e.printStackTrace()
                // في حالة فشل التنزيل
                _state.value = PdfViewerState(isLoading = false, error = "فشل تحميل الملف.")
            }
        }
    }
}