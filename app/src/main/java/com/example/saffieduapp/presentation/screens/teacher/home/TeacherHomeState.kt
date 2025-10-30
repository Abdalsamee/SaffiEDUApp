package com.example.saffieduapp.presentation.screens.teacher.home

// لاحقًا، سنضيف هنا كلاسات بيانات لأقسام المعلم الأخرى
// مثل StudentUpdate, TeacherClass, TopStudent
data class StudentUpdate(
    val studentId: String,
    val studentName: String,
    val studentImageUrl: String, // صورة الطالب المصغرة
    val taskTitle: String,
    val submissionTime: String
)

data class TeacherClass(
    val classId: String,
    val className: String, // مثال: "الصف العاشر"
    val subjectName: String,
    val subjectImageUrl: String,
    val studentCount: Int,
    val studentImages: List<String> // قائمة بروابط صور أول 3 طلاب مثلاً
)

data class TopStudent(
    val studentId: String,
    val studentName: String,
    val studentImageUrl: String,
    val rank: Int,
    val overallProgress: Int, // النسبة المئوية
    val assignmentsProgress: String, // مثال: "9/10"
    val quizzesProgress: String // مثال: "10/10"
)

data class TeacherHomeState(
    val isLoading: Boolean = true,
    val isActivating: Boolean = false,
    val isRefreshing: Boolean = false,
    val teacherName: String = "",
    val teacherSub: String = "", // مثال: "مدرس رياضيات"
    val profileImageUrl: String = "",
    val searchQuery: String = "",
    val studentUpdates: List<StudentUpdate> = emptyList(),
    val teacherClasses: List<TeacherClass> = emptyList(),
    // --- إضافة الخصائص الجديدة هنا ---
    // قائمة الصفوف المتاحة للفلترة
    val availableClassesForFilter: List<String> = emptyList(),
    // الصف المختار حاليًا في الفلتر
    val selectedClassFilter: String? = null,
    // قائمة الطلاب المتفوقين للصف المختار
    val topStudents: List<TopStudent> = emptyList(),
    val showActivateButton: Boolean = false // ⬅️ إضافة هذه الخاصية

)

