package com.example.saffieduapp.presentation.screens.student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- Data classes for raw data (as if from a repository or Firebase) ---
data class UrgentTask(val id: String, val title: String, val subject: String, val dueDate: String, val startTime: String, val imageUrl: String)
data class EnrolledSubject(val id: String, val name: String, val teacherName: String, val grade: String, val rating: Float, val isFavorite: Boolean, val imageUrl: String)
data class FeaturedLesson(val id: String, val title: String, val subject: String, val duration: String, val progress: Int, val imageUrl: String)


@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        // عند إنشاء الـ ViewModel لأول مرة، قم بالتحميل الأولي للبيانات
        loadInitialData()
    }

    /**
     * يتم استدعاء هذه الدالة من الواجهة عند السحب للأسفل (Pull to Refresh).
     * تعرض مؤشر التحديث في الأعلى.
     */
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            delay(2000) // محاكاة تأخير الشبكة لجلب البيانات الجديدة
            fetchAndProcessData() // استدعاء الدالة المشتركة لجلب البيانات
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    /**
     * يتم استدعاؤها فقط عند بدء تشغيل الواجهة لأول مرة.
     * تعرض مؤشر التحميل في منتصف الشاشة.
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            fetchAndProcessData() // استدعاء الدالة المشتركة لجلب البيانات
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    /**
     * هذه هي الدالة المركزية التي تحتوي على منطق جلب البيانات وتحويلها.
     * في المستقبل، ستضع كود الاتصال بـ Firebase هنا.
     */
    private fun fetchAndProcessData() {
        // --- بيانات مؤقتة (Dummy Data) ---
        // يمكنك التبديل بين القوائم الفارغة والممتلئة لاختبار الواجهة

       // val rawTasks = emptyList<UrgentTask>()
         val rawTasks = listOf(
             UrgentTask("1", "اختبار نصفي", "التربية الإسلامية", "24/8/2025", "11 صباحاً", ""),
             UrgentTask("2", "المهمة رقم 1", "اللغة الانجليزية", "24/8/2025", "12 مساءً", "")
         )

        val rawSubjects = listOf(
            EnrolledSubject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, true, ""),
            EnrolledSubject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, true, ""),
            EnrolledSubject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, false, "")
        )

        val rawLessons = listOf(
            FeaturedLesson("l1", "Romeo story", "English", "15 دقيقة", 30, ""),
            FeaturedLesson("l2", "درس الكسور", "رياضيات", "15 دقيقة", 80, "")
        )

        // --- تحويل البيانات إلى نماذج الواجهة (UI Models) ---
        val tasksUiModel = rawTasks.map {
            UrgentTaskUiModel(
                id = it.id,
                title = it.title,
                subject = it.subject,
                dueDate = it.dueDate,
                imageUrl = it.imageUrl,
                startTime = it.startTime
            )
        }
        val subjectsUiModel = rawSubjects.map {
            EnrolledSubjectUiModel(
                id = it.id,
                name = it.name,
                teacherName = it.teacherName,
                rating = it.rating,
                imageUrl = it.imageUrl,
                grade = it.grade
            )
        }
        val lessonsUiModel = rawLessons.map {
            FeaturedLessonUiModel(
                id = it.id,
                title = it.title,
                subject = it.subject,
                progress = it.progress,
                imageUrl = it.imageUrl,
                duration = it.duration
            )
        }

        // --- تحديث الحالة (State) بالبيانات الجديدة النهائية ---
        _state.value = _state.value.copy(
            studentName = "يزن عادل ضهير",
            studentGrade = "مصمم التطبيق Ui/Ux",
            profileImageUrl = "https://instagram.fgza2-5.fna.fbcdn.net/v/t51.2885-19/519497419_17974326737899750_3401532740011521622_n.jpg?efg=eyJ2ZW5jb2RlX3RhZyI6InByb2ZpbGVfcGljLmRqYW5nby4xMDgwLmMyIn0&_nc_ht=instagram.fgza2-5.fna.fbcdn.net&_nc_cat=110&_nc_oc=Q6cZ2QEAjYEcIt3ibYUz-_ZxzPQ6LWBAHB0LVbTmbyydG8aFuUFgzui5xS3BbPGcDHa2gWI&_nc_ohc=9caozL-qv6UQ7kNvwHa8jHJ&_nc_gid=2X9fZrOnRU9DXAmfrxeNXQ&edm=AP4sbd4BAAAA&ccb=7-5&oh=00_AfUiJ45ROL4CzXl8rLxDR_Fp6xlbE1gVFi9fz1YrCeVIpw&oe=68964CBC&_nc_sid=7a9f4b",
            urgentTasks = tasksUiModel,
            enrolledSubjects = subjectsUiModel,
            featuredLessons = lessonsUiModel
        )
    }


    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }
}