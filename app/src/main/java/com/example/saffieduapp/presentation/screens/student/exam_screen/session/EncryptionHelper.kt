package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * مساعد التشفير - لتشفير وفك تشفير البيانات الحساسة
 */
object EncryptionHelper {

    private const val TAG = "EncryptionHelper"
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"

    /**
     * توليد مفتاح تشفير جديد
     */
    fun generateKey(): SecretKey {
        return try {
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
            keyGenerator.init(256)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating key", e)
            throw e
        }
    }

    /**
     * تحويل المفتاح إلى String
     */
    fun keyToString(key: SecretKey): String {
        return Base64.encodeToString(key.encoded, Base64.NO_WRAP)
    }

    /**
     * تحويل String إلى مفتاح
     */
    fun stringToKey(keyString: String): SecretKey {
        val decodedKey = Base64.decode(keyString, Base64.NO_WRAP)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, ALGORITHM)
    }

    /**
     * تشفير String
     */
    fun encryptString(data: String, key: SecretKey): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting string", e)
            null
        }
    }

    /**
     * فك تشفير String
     */
    fun decryptString(encryptedData: String, key: SecretKey): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decodedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting string", e)
            null
        }
    }

    /**
     * تشفير ByteArray
     */
    fun encryptBytes(data: ByteArray, key: SecretKey): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            cipher.doFinal(data)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting bytes", e)
            null
        }
    }

    /**
     * فك تشفير ByteArray
     */
    fun decryptBytes(encryptedData: ByteArray, key: SecretKey): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key)
            cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting bytes", e)
            null
        }
    }
}