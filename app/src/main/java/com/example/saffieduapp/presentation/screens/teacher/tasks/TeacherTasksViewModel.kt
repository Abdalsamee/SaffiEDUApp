package com.example.saffieduapp.presentation.screens.teacher.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.saffieduapp.presentation.screens.teacher.tasks.components.TaskType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class TeacherTasksViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserEmail = auth.currentUser?.email

    private var teacherId: String? = null // سيتم تخزين معرف المعلم بعد الجلب الأول

    private val _state = MutableStateFlow(TeacherTasksState())
    val state: StateFlow<TeacherTasksState> = _state

    init {
        fetchTeacherIdAndLoadTasks() // تحميل المهام عند بدء التشغيل
    }

    // جلب معرف المعلم من Firestore باستخدام البريد الإلكتروني
    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchTeacherIdAndLoadTasks(selectedClass: String? = null) {
        // إذا كان المعرف موجود مسبقًا، نعيد تحميل المهام فقط
        teacherId?.let {
            loadAssignments(it, selectedClass)
            loadExams(it, selectedClass)
            return
        }

        // إذا لم يوجد بريد المستخدم
        if (currentUserEmail == null) return

        _state.value = _state.value.copy(isLoading = true)

        db.collection("teachers").whereEqualTo("email", currentUserEmail).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val teacherDoc = snapshot.documents[0]
                    teacherId = teacherDoc.id
                    loadAssignments(teacherId!!, selectedClass)
                    loadExams(teacherId!!, selectedClass)
                } else {
                    // لم يتم العثور على المعلم
                    _state.value = _state.value.copy(
                        assignments = emptyList(), exams = emptyList(), isLoading = false
                    )
                }
            }.addOnFailureListener {
                _state.value = _state.value.copy(isLoading = false)
            }
    }

    // تحميل الواجبات الخاصة بالمعلم حسب الصف
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadAssignments(teacherId: String, selectedClass: String? = null) {
        _state.value = _state.value.copy(isLoading = true)

        var query: Query = db.collection("assignments").whereEqualTo("teacherId", teacherId)

        if (!selectedClass.isNullOrEmpty()) {
            query = query.whereEqualTo("className", selectedClass)
        }

        query.get().addOnSuccessListener { snapshot ->
            val assignments = snapshot.documents.map { doc ->
                val dueDate = doc.getString("dueDate") ?: ""

                // 💡 التحديث هنا لتحديد isActive
                val isActive = isTaskActive(dueDate)

                TeacherTaskItem(
                    id = doc.id,
                    subject = doc.getString("subjectName") ?: "",
                    date = dueDate,
                    time = doc.getString("dueTime") ?: "23:59",
                    isActive = isActive, // ⬅️ تم تحديث القيمة بناءً على الدالة الجديدة
                    type = TaskType.ASSIGNMENT,
                    title = doc.getString("title")
                )
            }
            _state.value = _state.value.copy(assignments = assignments, isLoading = false)
        }.addOnFailureListener {
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    // تحميل الاختبارات الخاصة بالمعلم حسب الصف
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadExams(teacherId: String, selectedClass: String? = null) {
        _state.value = _state.value.copy(isLoading = true)

        var query: Query = db.collection("exams").whereEqualTo("teacherId", teacherId)

        if (!selectedClass.isNullOrEmpty()) {
            query = query.whereEqualTo("className", selectedClass)
        }

        query.get().addOnSuccessListener { snapshot ->
            val exams = snapshot.documents.map { doc ->
                val examDate = doc.getString("examDate") ?: ""

                // 💡 التحديث هنا لتحديد isActive بناءً على نهاية اليوم (23:59)
                val isActive = isTaskActive(examDate)

                TeacherTaskItem(
                    id = doc.id,
                    subject = doc.getString("subjectName") ?: "",
                    date = examDate,
                    time = doc.getString("examStartTime") ?: "N/A", // قد لا يكون لهذا الحقل علاقة بالانتهاء
                    isActive = isActive, // ⬅️ تم تحديث القيمة هنا
                    type = TaskType.EXAM,
                    title = doc.getString("title")
                )
            }
            _state.value = _state.value.copy(exams = exams, isLoading = false)
        }.addOnFailureListener {
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteTask(taskId: String, taskType: TaskType) {
        // تحديد اسم المجموعة بناءً على نوع المهمة
        val collectionName = when (taskType) {
            TaskType.ASSIGNMENT -> "assignments"
            TaskType.EXAM -> "exams"
        }

        _state.value = _state.value.copy(isLoading = true) // عرض مؤشر التحميل

        db.collection(collectionName).document(taskId).delete()
            .addOnSuccessListener {
                // بعد الحذف بنجاح، قم بإعادة تحميل المهام المتبقية
                teacherId?.let { id ->
                    // لا نحتاج لإعادة جلب معرف المعلم، فقط إعادة تحميل المهام باستخدام الصف المحدد
                    loadAssignments(id, _state.value.selectedClass)
                    loadExams(id, _state.value.selectedClass)
                }
            }
            .addOnFailureListener { e ->
                // يمكنك هنا إضافة منطق لعرض رسالة خطأ للمستخدم
                _state.value = _state.value.copy(isLoading = false)
                // يمكن استخدام Log.e أو أي نظام Logging
                println("Error deleting task: $e")
            }
    }

    // تغيير التبويب (واجبات / اختبارات)
    fun onTabSelected(index: Int) {
        _state.value = _state.value.copy(selectedTabIndex = index)
    }

    // تغيير الصف وإعادة تحميل المهام والاختبارات
    @RequiresApi(Build.VERSION_CODES.O)
    fun onClassSelected(className: String) {
        _state.value = _state.value.copy(selectedClass = className)
        fetchTeacherIdAndLoadTasks(className)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isTaskActive(dueDateString: String): Boolean {
        // تحديد تنسيق التاريخ كما هو مخزن في Firebase
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

        return try {
            // تحويل تاريخ الاستحقاق المخزن إلى كائن LocalDate
            val dueDate = LocalDate.parse(dueDateString, dateFormatter)

            // تحديد وقت انتهاء اليوم (23:59)
            val endOfDayTime = LocalTime.of(23, 59)

            // دمج التاريخ والوقت لتكوين LocalDateTime للانتهاء
            val dueDateTime = LocalDateTime.of(dueDate, endOfDayTime)

            // الحصول على التاريخ والوقت الحاليين
            val now = LocalDateTime.now()

            // التحقق مما إذا كان الوقت الحالي قبل أو يساوي وقت الاستحقاق
            !now.isAfter(dueDateTime)
        } catch (e: Exception) {
            // في حالة حدوث خطأ في التحويل (مثل تنسيق تاريخ خاطئ)، نعتبرها نشطة افتراضيًا
            println("Error parsing date: $dueDateString. Error: ${e.message}")
            true
        }
    }
}