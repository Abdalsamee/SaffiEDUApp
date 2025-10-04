package com.example.saffieduapp.presentation.screens.teacher.add_assignment

import android.R.attr.name
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import com.example.saffieduapp.data.repository.AssignmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAssignmentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(AddAssignmentState(teacherName = name.toString()))
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>() // لإرسال الرسائل
    val eventFlow = _eventFlow.asSharedFlow()

    private val assigrepository = AssignmentRepository() // الكلاس الجديد
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchTeacherData()
    }

    fun onEvent(event: AddAssignmentEvent) {
        when (event) {
            is AddAssignmentEvent.TitleChanged -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddAssignmentEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description) }
            }
            is AddAssignmentEvent.DateChanged -> {
                _state.update { it.copy(dueDate = event.date) }
            }
            is AddAssignmentEvent.ClassSelected -> {
                _state.update { it.copy(selectedClass = event.className) }
            }
            is AddAssignmentEvent.ImageSelected -> {
                _state.update {
                    it.copy(
                        selectedImageUri = event.uri,
                        selectedImageName = event.uri?.let { uri -> getFileName(uri) }
                    )
                }
            }
            is AddAssignmentEvent.SaveClicked -> {
                saveAssignment()
            }
        }
    }
    // ← دالة جديدة لجلب بيانات المعلم بما فيها اسم المادة
    private fun fetchTeacherData() {
        val email = FirebaseAuth.getInstance().currentUser?.email
        firestore.collection("teachers")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val document = snapshot.documents[0]
                    val name = document.getString("fullName") ?: ""
                    val subject = document.getString("subject") ?: "" // ← جلب اسم المادة

                    _state.update {
                        it.copy(
                            teacherName = name,
                            subjectName = subject
                        )
                    }
                }
            }
            .addOnFailureListener {
                _state.update {
                    it.copy(
                        teacherName = "اسم غير معروف",
                        subjectName = "مادة غير معروفة"
                    )
                }
            }
    }

    private fun saveAssignment() {
        val currentState = state.value
        if (currentState.title.isBlank() || currentState.description.isBlank() || currentState.selectedClass.isBlank()) {
            return
        }

        _state.update { it.copy(isSaving = true) }

        val teacherName = currentState.teacherName
        val subjectName = currentState.subjectName // ← الحصول على اسم المادة

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val success = assigrepository.saveAssignment(
                title = currentState.title,
                description = currentState.description,
                dueDate = currentState.dueDate,
                className = currentState.selectedClass,
                imageUri = currentState.selectedImageUri,
                imageName = currentState.selectedImageName,
                teacherName = currentState.teacherName,
                subjectName = currentState.subjectName // ← إرسال اسم المادة
            )

            if (success) {
                _eventFlow.emit("تم حفظ الواجب بنجاح!")
                _state.update {
                    AddAssignmentState(
                        teacherName = teacherName,
                        subjectName = subjectName
                    )
                }
            } else {
                _eventFlow.emit("حدث خطأ أثناء حفظ الواجب")
                _state.update { it.copy(isSaving = false) }
            }
        }
    }


    private fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }
}