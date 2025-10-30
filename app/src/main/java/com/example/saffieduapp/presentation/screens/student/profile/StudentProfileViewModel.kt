package com.example.saffieduapp.presentation.screens.student.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

        db.collection("students").whereEqualTo("email", email).get()
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
                            profileImageUrl = doc.getString("profileImageUrl"),
                            phoneNumber = doc.id
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false, errorMessage = "لم يتم العثور على بيانات الطالب"
                        )
                    }
                }
            }.addOnFailureListener {
                _state.update {
                    it.copy(
                        isLoading = false, errorMessage = "حدث خطأ أثناء تحميل البيانات"
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

        // 🌟 بدء التحميل لتحديث الصورة
        _state.update { it.copy(isPhotoUpdating = true, message = null, errorMessage = null) }

        // يجب أن نستخدم coroutines here لتجنب callback hell وتحسين القراءة
        viewModelScope.launch {
            try {
                val storageRef = storage.reference.child("profile_images/${user.uid}.jpg")

                // 1. رفع الصورة
                storageRef.putFile(imageUri).await()

                // 2. الحصول على رابط التحميل
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 3. البحث عن مستند الطالب
                val snapshot = db.collection("students").whereEqualTo("email", email).get().await()
                val doc = snapshot.documents.firstOrNull()

                if (doc != null) {
                    // 4. تحديث رابط الصورة في Firestore
                    db.collection("students").document(doc.id)
                        .update("profileImageUrl", downloadUrl).await()

                    // 5. تحديث الحالة بالنجاح
                    _state.update {
                        it.copy(
                            isPhotoUpdating = false,
                            profileImageUrl = downloadUrl,
                            message = "تم تحديث الصورة بنجاح ✅"
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isPhotoUpdating = false,
                            errorMessage = "لم يتم العثور على بيانات الطالب لتحديث الصورة ❌"
                        )
                    }
                }
            } catch (e: Exception) {
                // 6. التعامل مع أي خطأ
                _state.update {
                    it.copy(
                        isPhotoUpdating = false,
                        errorMessage = "فشل في تحديث الصورة: ${e.message} ❌"
                    )
                }
            }
        }
    }
}
