package com.example.saffieduapp.data.FireBase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    // التحقق من وجود رقم الهوية في قاعدة البيانات (المستند هو رقم الهوية)
    suspend fun isIdNumberExists(idNumber: String): Boolean {
        val snapshot = firestore.collection("users").document(idNumber).get().await()
        return snapshot.exists()
    }

    // إنشاء مستخدم جديد في Firebase Authentication مع إرسال بريد التحقق
    suspend fun createUserWithEmailAndPassword(email: String, password: String) {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.sendEmailVerification()?.await()
    }

    // حفظ بيانات المستخدم في Firestore ضمن مجموعة "users" باستخدام رقم الهوية كمفتاح
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
