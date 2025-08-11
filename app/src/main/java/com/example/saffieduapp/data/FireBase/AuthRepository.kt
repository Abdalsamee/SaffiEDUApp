package com.example.saffieduapp.data.FireBase


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun isIdNumberExists(idNumber: String): Boolean {
        val result = firestore.collection("users")
            .document(idNumber)
            .get()
            .await()
        return result.exists()
    }


    // إنشاء حساب في Firebase Authentication + إرسال بريد التحقق
    suspend fun createUserWithEmailAndPassword(email: String, password: String) {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.sendEmailVerification()?.await()
    }

    // تخزين بيانات المستخدم في Firestore (بدون كلمة المرور)
    suspend fun registerUserData(
        idNumber: String,
        fullName: String,
        email: String,
        grade: String
    ) {
        val userData = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "grade" to grade
        )
        firestore.collection("users")
            .document(idNumber)
            .set(userData)
            .await()
    }

}
