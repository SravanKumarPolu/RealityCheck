package com.realitycheck.app.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Helper class for encrypting/decrypting sensitive data
 * Uses AES-256-GCM for authenticated encryption
 * 
 * Note: For production, consider using Android Keystore for key management
 */
object EncryptionHelper {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    
    // In production, this should be stored securely (e.g., Android Keystore)
    // For now, using a simple approach - consider migrating to Keystore
    private var secretKey: SecretKey? = null
    
    /**
     * Initialize encryption with a key
     * In production, generate/store key securely using Android Keystore
     */
    fun initialize(key: ByteArray) {
        secretKey = SecretKeySpec(key, "AES")
    }
    
    /**
     * Generate a new encryption key
     * Store this securely in production (Android Keystore recommended)
     */
    fun generateKey(): ByteArray {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(KEY_SIZE)
        val key = keyGenerator.generateKey()
        return key.encoded
    }
    
    /**
     * Encrypt a string value
     * Returns base64-encoded encrypted data
     */
    fun encrypt(plaintext: String): String? {
        if (secretKey == null) {
            // Auto-initialize with generated key (for demo)
            // In production, load from secure storage
            val key = generateKey()
            initialize(key)
        }
        
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
            
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            // Combine IV + ciphertext
            val encrypted = ByteArray(iv.size + ciphertext.size)
            System.arraycopy(iv, 0, encrypted, 0, iv.size)
            System.arraycopy(ciphertext, 0, encrypted, iv.size, ciphertext.size)
            
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Decrypt a base64-encoded encrypted string
     */
    fun decrypt(encrypted: String): String? {
        if (secretKey == null) {
            return null
        }
        
        return try {
            val encryptedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
            
            // Extract IV and ciphertext
            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(encryptedBytes, 0, iv, 0, iv.size)
            
            val ciphertext = ByteArray(encryptedBytes.size - iv.size)
            System.arraycopy(encryptedBytes, iv.size, ciphertext, 0, ciphertext.size)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
            
            val plaintext = cipher.doFinal(ciphertext)
            String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if encryption is available
     */
    fun isEncryptionAvailable(): Boolean {
        return secretKey != null
    }
}

/**
 * Extension functions for encrypting/decrypting Decision fields
 * These can be used to encrypt sensitive fields like title, description, outcome
 */
fun String.encrypt(): String? = EncryptionHelper.encrypt(this)
fun String.decrypt(): String? = EncryptionHelper.decrypt(this)

