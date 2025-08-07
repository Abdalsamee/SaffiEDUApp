package com.example.saffieduapp.data.local.preferences.FireBase


import android.annotation.SuppressLint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class AuthRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun isIdNumberExists(idNumber: String): Boolean {
        val result = firestore.collection("users")
            .document(idNumber)
            .get()
            .await()
        return result.exists()
    }

    suspend fun registerUser(
        idNumber: String,
        fullName: String,
        email: String,
        password: String,
        grade: String
    ) {
        val userData = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "password" to password,
            "grade" to grade
        )
        firestore.collection("users")
            .document(idNumber)
            .set(userData)
            .await()
    }

}