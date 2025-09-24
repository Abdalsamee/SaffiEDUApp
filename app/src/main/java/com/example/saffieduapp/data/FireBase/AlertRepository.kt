package com.example.saffieduapp.data.FireBase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Alert(
    val description: String = "",
    val targetClass: String = "",
    val sendDate: String = "",
    val sendTime: String = "",
    val subjectId: String = "" // 🔹 إضافة معرف المادة
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

    suspend fun getLatestAlertBySubject(subjectId: String): Alert? {
        return try {
            val querySnapshot = firestore.collection("alerts")
                .whereEqualTo("subjectId", subjectId)
                .orderBy("sendDate") // ترتيب حسب التاريخ
                .orderBy("sendTime") // ثم الوقت
                .limit(1)
                .get()
                .await()

            querySnapshot.documents.firstOrNull()?.toObject(Alert::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}