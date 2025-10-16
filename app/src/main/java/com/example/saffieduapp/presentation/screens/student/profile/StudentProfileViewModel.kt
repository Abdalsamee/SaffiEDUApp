package com.example.saffieduapp.presentation.screens.student.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(StudentProfileState())
    val state: StateFlow<StudentProfileState> = _state

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        loadStudentProfile()
    }

    private fun loadStudentProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _state.update { it.copy(isLoading = false, errorMessage = "لم يتم تسجيل الدخول") }
            return
        }

        val email = currentUser.email ?: return
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            db.collection("students")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { snapshot ->
                    val doc = snapshot.documents.firstOrNull()
                    if (doc != null) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                fullName = doc.getString("fullName") ?: "",
                                email = email,
                                phoneNumber = doc.getString("phoneNumber") ?: "",
                                className = doc.getString("className") ?: "",
                                average = doc.getString("average") ?: "",
                                profileImageUrl = doc.getString("profileImageUrl")
                            )
                        }
                    } else {
                        _state.update { it.copy(isLoading = false, errorMessage = "لم يتم العثور على بيانات الطالب") }
                    }
                }
                .addOnFailureListener {
                    _state.update { it.copy(isLoading = false, errorMessage = "حدث خطأ أثناء تحميل البيانات") }
                }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
