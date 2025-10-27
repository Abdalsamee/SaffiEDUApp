package com.example.saffieduapp.presentation.screens.teacher.profile

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
class TeacherProfileViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(
        TeacherProfileState(
            isLoading = true
        )
    )
    val state: StateFlow<TeacherProfileState> = _state

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    init {
        loadTeacherProfile()
    }

    private fun loadTeacherProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _state.update {
                it.copy(
                    isLoading = false, error = "لم يتم تسجيل الدخول"
                )
            }
            return
        }

        val email = currentUser.email ?: return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            db.collection("teachers").whereEqualTo("email", email).get()
                .addOnSuccessListener { snapshot ->
                    val doc = snapshot.documents.firstOrNull()
                    if (doc != null) {

                        // ✅ 1. احصل على مصفوفة الصفوف (List) من المستند
                        val classesList = doc.get("className") as? List<*>

                        _state.update {
                            it.copy(
                                isLoading = false,
                                fullName = doc.getString("fullName") ?: "",
                                email = email,

                                // ✅ 2. اسحب رقم الهوية من مُعرّف المستند
                                nationalId = doc.id,

                                subject = doc.getString("subject") ?: "",

                                // ✅ 3. احسب عدد الصفوف بناءً على حجم المصفوفة
                                classesCount = classesList?.size ?: 0,

                                profileImageUrl = doc.getString("profileImageUrl")
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false, error = "لم يتم العثور على بيانات المعلم"
                            )
                        }
                    }
                }.addOnFailureListener { e ->
                    _state.update {
                        it.copy(
                            isLoading = false, error = "حدث خطأ أثناء تحميل البيانات: ${e.message}"
                        )
                    }
                }
        }
    }

    fun updateProfilePhoto(uri: Uri) {
        val nationalId = _state.value.nationalId
        // التأكد من وجود رقم الهوية (معرّف المستند) قبل المتابعة
        if (nationalId.isBlank()) {
            _state.update { it.copy(error = "خطأ: لا يمكن تحديث الصورة بدون معرّف المستخدم") }
            return
        }

        // 1. تحديث الحالة لبدء إظهار مؤشر التحميل
        _state.update { it.copy(error = null) }

        viewModelScope.launch {
            try {
                // 2. تحديد مسار الصورة في Firebase Storage
                val storageRef = storage.reference.child("profile_images/teachers/$nationalId.jpg")

                // 3. رفع الملف (باستخدام await لانتظار الاكتمال)
                storageRef.putFile(uri).await()

                // 4. الحصول على رابط التحميل (باستخدام await)
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 5. تحديث مستند المعلم في Firestore بالرابط الجديد
                db.collection("teachers").document(nationalId)
                    .update("profileImageUrl", downloadUrl).await()

                // 6. تحديث الحالة في التطبيق بالصورة الجديدة وإيقاف التحميل
                _state.update {
                    it.copy(
                        profileImageUrl = downloadUrl
                    )
                }

            } catch (e: Exception) {
                // 7. التعامل مع أي خطأ يحدث أثناء الرفع أو التحديث
                _state.update {
                    it.copy(
                        error = "فشل تحديث الصورة: ${e.message}"
                    )
                }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        // نفّذ sign out
        FirebaseAuth.getInstance().signOut()

        // أي تنظيف محلي آخر هنا (مثال: مسح pref أو datastore)...
        // ثم استدعي callback على الـ main thread
        viewModelScope.launch {
            onComplete()
        }
    }
}
