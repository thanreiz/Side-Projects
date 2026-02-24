package com.floapp.agriflo.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.Cipher
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages cryptographic keys stored in the Android Keystore.
 * Used for encrypting the SQLCipher database passphrase and any API secrets.
 *
 * Keys are NOT extractable from the device hardware-backed keystore.
 * No secret material is ever hardcoded or written to disk in plaintext.
 */
@Singleton
class KeystoreHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    /**
     * Returns the SQLCipher database passphrase.
     * Creates and stores the key on first call; retrieves it on subsequent calls.
     * The passphrase is 256-bit AES-derived and stored encrypted in Android Keystore.
     */
    fun getOrCreateDatabasePassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedPassphrase = prefs.getString(KEY_DB_PASSPHRASE, null)

        return if (encryptedPassphrase != null) {
            decrypt(encryptedPassphrase, DB_KEY_ALIAS)
        } else {
            val passphrase = generatePassphrase()
            val encrypted = encrypt(passphrase, DB_KEY_ALIAS)
            prefs.edit().putString(KEY_DB_PASSPHRASE, encrypted).apply()
            passphrase
        }
    }

    private fun generatePassphrase(): ByteArray {
        val keyGen = KeyGenerator.getInstance("AES").apply {
            init(256)
        }
        return keyGen.generateKey().encoded
    }

    private fun getOrCreateAesKey(alias: String): SecretKey {
        return if (!keyStore.containsAlias(alias)) {
            val keyGen = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
            keyGen.init(spec)
            keyGen.generateKey()
        } else {
            (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
        }
    }

    private fun encrypt(data: ByteArray, keyAlias: String): String {
        val key = getOrCreateAesKey(keyAlias)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
        val encrypted = cipher.doFinal(data)
        val iv = cipher.iv
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    private fun decrypt(encryptedData: String, keyAlias: String): ByteArray {
        val key = getOrCreateAesKey(keyAlias)
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val data = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        }
        return cipher.doFinal(data)
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val PREFS_NAME = "flo_keystore_prefs"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
        private const val DB_KEY_ALIAS = "flo_db_key_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH_BITS = 128
    }
}
