package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * مساعد تشفير الملفات - AES-256-GCM
 */
object EncryptionHelper {
    private const val TAG = "EncryptionHelper"

    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    /**
     * توليد مفتاح تشفير جديد
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey()
    }

    /**
     * تحويل المفتاح إلى String للحفظ
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
     * تشفير ملف
     */
    fun encryptFile(
        inputFile: File,
        outputFile: File,
        key: SecretKey
    ): Boolean {
        return try {
            Log.d(TAG, "Encrypting file: ${inputFile.name}")

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val iv = cipher.iv

            FileOutputStream(outputFile).use { output ->
                // كتابة حجم IV أولاً
                output.write(iv.size)
                // كتابة IV
                output.write(iv)

                // تشفير وكتابة المحتوى
                FileInputStream(inputFile).use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val encryptedChunk = cipher.update(buffer, 0, bytesRead)
                        if (encryptedChunk != null) {
                            output.write(encryptedChunk)
                        }
                    }

                    val finalChunk = cipher.doFinal()
                    if (finalChunk != null) {
                        output.write(finalChunk)
                    }
                }
            }

            Log.d(TAG, "✅ File encrypted successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to encrypt file", e)
            false
        }
    }

    /**
     * فك تشفير ملف
     */
    fun decryptFile(
        encryptedFile: File,
        outputFile: File,
        key: SecretKey
    ): Boolean {
        return try {
            Log.d(TAG, "Decrypting file: ${encryptedFile.name}")

            FileInputStream(encryptedFile).use { input ->
                // قراءة حجم IV
                val ivSize = input.read()

                // قراءة IV
                val iv = ByteArray(ivSize)
                input.read(iv)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, spec)

                // فك التشفير والكتابة
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val decryptedChunk = cipher.update(buffer, 0, bytesRead)
                        if (decryptedChunk != null) {
                            output.write(decryptedChunk)
                        }
                    }

                    val finalChunk = cipher.doFinal()
                    if (finalChunk != null) {
                        output.write(finalChunk)
                    }
                }
            }

            Log.d(TAG, "✅ File decrypted successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to decrypt file", e)
            false
        }
    }

    /**
     * تشفير نص
     */
    fun encryptString(text: String, key: SecretKey): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val iv = cipher.iv
            val encrypted = cipher.doFinal(text.toByteArray())

            // دمج IV مع البيانات المشفرة
            val combined = iv + encrypted
            Base64.encodeToString(combined, Base64.NO_WRAP)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt string", e)
            null
        }
    }

    /**
     * فك تشفير نص
     */
    fun decryptString(encryptedText: String, key: SecretKey): String? {
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            // فصل IV عن البيانات
            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val decrypted = cipher.doFinal(encrypted)
            String(decrypted)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt string", e)
            null
        }
    }
}