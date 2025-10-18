package com.example.saffieduapp.presentation.screens.student.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(StudentProfileState(message = null))
    val state: StateFlow<StudentProfileState> = _state

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()


    init {
        loadStudentProfile()
    }

    /**
     * تحميل بيانات الطالب من Firestore
     */
    /**
     * تحميل بيانات الطالب من Firestore
     */
    private fun loadStudentProfile() {
        val currentUser = auth.currentUser ?: return
        val email = currentUser.email ?: return

        _state.update { it.copy(isLoading = true) }

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
                            className = doc.getString("grade") ?: "",
                            average = doc.getString("average") ?: "",
                            profileImageUrl = doc.getString("profileImageUrl")
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "لم يتم العثور على بيانات الطالب"
                        )
                    }
                }
            }
            .addOnFailureListener {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "حدث خطأ أثناء تحميل البيانات"
                    )
                }
            }
    }

    /**
     * تسجيل الخروج
     */
    fun logout(onLogoutSuccess: () -> Unit) {
        auth.signOut()
        onLogoutSuccess()
    }


    /**
     * 🔹 رفع صورة جديدة إلى Firebase Storage وتحديثها في Firestore
     */
    fun updateProfileImage(imageUri: Uri) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return

        _state.update { it.copy(isLoading = true, message = null) }

        val storageRef = storage.reference.child("profile_images/${user.uid}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    db.collection("students")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val doc = snapshot.documents.firstOrNull()
                            if (doc != null) {
                                db.collection("students").document(doc.id)
                                    .update("profileImageUrl", downloadUrl.toString())
                                    .addOnSuccessListener {
                                        _state.update {
                                            it.copy(
                                                isLoading = false,
                                                profileImageUrl = downloadUrl.toString(),
                                                message = "تم تحديث الصورة بنجاح ✅"
                                            )
                                        }
                                    }
                                    .addOnFailureListener {
                                        _state.update {
                                            it.copy(
                                                isLoading = false,
                                                message = "حدث خطأ أثناء تحديث الصورة في قاعدة البيانات ❌"
                                            )
                                        }
                                    }
                            }
                        }
                }
            }
            .addOnFailureListener {
                _state.update {
                    it.copy(isLoading = false, message = "فشل في رفع الصورة ❌")
                }
            }
    }
}
