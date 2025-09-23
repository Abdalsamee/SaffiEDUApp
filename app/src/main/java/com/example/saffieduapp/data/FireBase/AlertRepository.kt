package com.example.saffieduapp.data.FireBase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Alert(
    val description: String = "",
    val targetClass: String = "",
    val sendDate: String = "",
    val sendTime: String = ""
)

class AlertRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun saveAlert(alert: Alert): Boolean {
        return try {
            firestore.collection("alerts")
                .add(alert)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}