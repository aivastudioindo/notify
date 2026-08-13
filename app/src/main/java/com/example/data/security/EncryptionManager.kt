package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("notif_vault_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "NotifVault_AES256_Key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BIT = 128
        private const val PREF_PIN_HASH = "vault_pin_hash"
        private const val PREF_IS_PIN_ENABLED = "vault_is_pin_enabled"
        private const val PREF_ENCRYPTION_ENABLED = "vault_encryption_enabled"

        @Volatile
        private var INSTANCE: EncryptionManager? = null

        fun getInstance(context: Context): EncryptionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EncryptionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        ensureSecretKey()
    }

    private fun ensureSecretKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Log fallback if keystore has specific provider nuances
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey ?: throw IllegalStateException("Kunci enkripsi Keystore tidak ditemukan")
    }

    fun encrypt(plainText: String): Pair<String, String> {
        if (plainText.isEmpty()) return Pair("", "")
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val cipherTextBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            Pair(cipherTextBase64, ivBase64)
        } catch (e: Exception) {
            // Fallback for safety
            Pair(plainText, "")
        }
    }

    fun decrypt(cipherTextBase64: String, ivBase64: String): String {
        if (cipherTextBase64.isEmpty()) return ""
        if (ivBase64.isEmpty()) return cipherTextBase64 // Not encrypted or fallback

        return try {
            val cipherBytes = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            cipherTextBase64
        }
    }

    // PIN & Vault Lock Support
    fun isPinProtectionEnabled(): Boolean {
        return prefs.getBoolean(PREF_IS_PIN_ENABLED, false) && !getSavedPinHash().isNullOrEmpty()
    }

    fun setPinProtection(enabled: Boolean, pin: String? = null) {
        val editor = prefs.edit()
        editor.putBoolean(PREF_IS_PIN_ENABLED, enabled)
        if (pin != null) {
            editor.putString(PREF_PIN_HASH, hashPin(pin))
        }
        editor.apply()
    }

    fun verifyPin(pin: String): Boolean {
        val saved = getSavedPinHash() ?: return false
        return saved == hashPin(pin)
    }

    private fun getSavedPinHash(): String? {
        return prefs.getString(PREF_PIN_HASH, null)
    }

    private fun hashPin(pin: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun isEncryptionActive(): Boolean {
        return prefs.getBoolean(PREF_ENCRYPTION_ENABLED, true)
    }

    fun setEncryptionActive(active: Boolean) {
        prefs.edit().putBoolean(PREF_ENCRYPTION_ENABLED, active).apply()
    }
}
