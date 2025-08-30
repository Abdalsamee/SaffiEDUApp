package com.example.saffieduapp.data.FireBase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun isIdNumberExists(idNumber: String, role: String): Boolean {
        val collection = if (role == "student") "students" else "teachers"
        val snapshot = firestore.collection(collection).document(idNumber).get().await()
        return snapshot.exists()
    }

    suspend fun createUserWithEmailAndPassword(email: String, password: String) {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.sendEmailVerification()?.await()
    }

    // ✅ حفظ بيانات الطالب مع role + grade + idNumber
    suspend fun registerStuData(
        collectionName: String = "students",
        idNumber: String,
        fullName: String,
        email: String,
        grade: String,
    ) {

        val userData = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "grade" to grade,
            "role" to "student"
        )

        firestore.collection(collectionName)
            .document(idNumber) // المستند يبقى برقم الهوية
            .set(userData)
            .await()
    }


}
