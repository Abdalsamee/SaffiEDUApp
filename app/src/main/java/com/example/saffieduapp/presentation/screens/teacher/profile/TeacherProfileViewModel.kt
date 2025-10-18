package com.example.saffieduapp.presentation.screens.teacher.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherProfileViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(TeacherProfileState(isLoading = true))
    val state: StateFlow<TeacherProfileState> = _state

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    init {
        loadTeacherProfile()
    }

    /**
     * 🧩 تحميل بيانات وهمية بدلًا من Firestore
     * تحاكي العملية الحقيقية لعرض شاشة المعلم أثناء التطوير أو بدون اتصال Firebase.
     */
    private fun loadTeacherProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "لم يتم تسجيل الدخول"
                )
            }
            return
        }

        val email = currentUser.email ?: return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            db.collection("teachers")
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
                                subject = doc.getString("subject") ?: "",
                                classesCount = doc.getLong("classesCount")?.toInt() ?: 0,
                                profileImageUrl = doc.getString("profileImageUrl")
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "لم يتم العثور على بيانات المعلم"
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "حدث خطأ أثناء تحميل البيانات: ${e.message}"
                        )
                    }
                }
        }
    }

    /**
     * 🚪 تسجيل الخروج (وهمي أيضًا)
     */
    /**
     * تسجيل الخروج
     */
    fun logout(onLogoutSuccess: () -> Unit) {
        auth.signOut()
        onLogoutSuccess()
    }
}
