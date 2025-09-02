package  com.example.saffieduapp.presentation.screens.student.subject_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.presentation.screens.student.subject_details.components.Lesson
import com.example.saffieduapp.presentation.screens.student.subject_details.components.PdfLesson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectDetailsState())
    val state = _state.asStateFlow()

    // <--- تعديل 1: احتفظنا بتعريف subjectId هنا مرة واحدة فقط
    private val subjectId: String = checkNotNull(savedStateHandle["subjectId"])

    init {
        // <--- تعديل 2: استدعينا الدالة بدون تمرير متغيرات
        loadSubjectDetails()
        loadVideoLessons()
        loadAlerts()
        println("ViewModel: Received and will search for ID -> $subjectId")
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun loadAlerts() {
        // <--- تعديل 4: قمنا بتفعيل البيانات التجريبية
        val sampleAlerts = listOf(
            Alert(id = "1", message = "تم إلغاء الدرس الأول بعنوان شرح سورة نوح")
        )
        _state.update { it.copy(alerts = sampleAlerts) }
    }

    fun onTabSelected(tab: SubjectTab) {
        _state.update { it.copy(selectedTab = tab) }
        if (tab == SubjectTab.PDFS && state.value.pdfSummaries.isEmpty()) {
            loadPdfSummaries()
        }
    }

    // <--- تعديل 3: الدالة الآن لا تستقبل متغيرات وتعتمد على subjectId المعرف في الكلاس
    private fun loadSubjectDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(200)

            val allSubjects = listOf(
                Subject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, "", 1),
                Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, "", 2),
                Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 3)
            )
            // هي تستخدم subjectId المعرف في الكلاس مباشرة
            val subject = allSubjects.find { it.id == subjectId }
            _state.update { it.copy(subject = subject) }
        }
    }

    private fun loadVideoLessons() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(500)

            // <--- تعديل 4: قمنا بتفعيل البيانات التجريبية
            val lessons = listOf(
                Lesson(id = 1, title = "الدرس الأول", subTitle = "شرح سورة نوح", duration =15, imageUrl = "", progress = 30f),
                Lesson(id = 2, title = "الدرس الثاني", subTitle = "شرح سورة البقرة", duration = 22, imageUrl = "", progress = 50f),
                Lesson(id = 3, title = "الدرس الثاني", subTitle = "شرح سورة البقرة", duration = 22, imageUrl = "", progress = 90f),
                Lesson(id = 4, title = "الدرس الثاني", subTitle = "شرح سورة البقرة", duration = 22, imageUrl = "", progress = 100f),
                Lesson(id = 5, title = "الدرس الثالث", subTitle = "شرح سورة آل عمران", duration = 32, imageUrl = "", progress = 100f)
            )
            _state.update { it.copy(isLoading = false, videoLessons = lessons) }
        }
    }
    private fun loadPdfSummaries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(500)

            val pdfs = listOf(
                PdfLesson(id = 1, title = "الوحدة الأولى", subTitle = "النحو والصرف", pagesCount = 12, isRead = false, imageUrl = ""),
                PdfLesson(id = 2, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = false, imageUrl = ""),
                PdfLesson(id = 3, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = false, imageUrl = ""),
                PdfLesson(id = 4, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = true, imageUrl = ""),
                PdfLesson(id = 5, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = false, imageUrl = ""),
                PdfLesson(id = 6, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = true, imageUrl = ""),
                PdfLesson(id = 7, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = false, imageUrl = ""),
                PdfLesson(id = 8, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = true, imageUrl = ""),
                PdfLesson(id = 9, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = false, imageUrl = ""),
                PdfLesson(id = 10, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = true, imageUrl = ""),
                PdfLesson(id = 11, title = "ملخص الوحدة الثانية", subTitle = "البلاغة", pagesCount = 8, isRead = true, imageUrl = ""),
            )
            _state.update { it.copy(isLoading = false, pdfSummaries = pdfs) }
        }
    }

    // --- 👇👇👇 التصحيح الثاني: إضافة الدالة المفقودة هنا 👇👇👇 ---
    /**
     * تحديث حالة "مقروء" لملخص PDF معين.
     */
    fun updatePdfLessonReadStatus(lesson: PdfLesson, isRead: Boolean) {
        viewModelScope.launch {
            _state.update { currentState ->
                // ابحث عن العنصر في القائمة وقم بتحديثه
                val updatedPdfs = currentState.pdfSummaries.map { pdf ->
                    if (pdf.id == lesson.id) {
                        pdf.copy(isRead = isRead) // قم بتغيير حالة `isRead` فقط للعنصر المطلوب
                    } else {
                        pdf // أعد بقية العناصر كما هي
                    }
                }
                // قم بإرجاع الحالة الجديدة مع القائمة المحدثة
                currentState.copy(pdfSummaries = updatedPdfs)
            }
        }
    }
}