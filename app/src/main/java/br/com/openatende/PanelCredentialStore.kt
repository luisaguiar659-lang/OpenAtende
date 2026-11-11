package br.com.openatende

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores panel credentials encrypted with an AES/GCM key held by Android Keystore.
 * Provider values must be stable identifiers such as "masterflix" and "stv".
 */
object PanelCredentialStore {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS_PREFIX = "openatende_panel_"
    private const val PREFS_NAME = "encrypted_panel_credentials"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    data class Credentials(val username: String, val password: String)

    fun save(context: Context, provider: String, username: String, password: String) {
        require(provider == "masterflix" || provider == "stv") { "Provedor inválido" }
        val key = getOrCreateKey(provider)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("${provider}_username", username)
            .putString("${provider}_password", Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString("${provider}_iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    fun load(context: Context, provider: String): Credentials? {
        require(provider == "masterflix" || provider == "stv") { "Provedor inválido" }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString("${provider}_username", null) ?: return null
        val encrypted = prefs.getString("${provider}_password", null) ?: return null
        val iv = prefs.getString("${provider}_iv", null) ?: return null
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(provider),
                GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
            )
            val password = cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP))
                .toString(Charsets.UTF_8)
            Credentials(username, password)
        } catch (_: Exception) {
            null
        }
    }

    fun clear(context: Context, provider: String) {
        require(provider == "masterflix" || provider == "stv") { "Provedor inválido" }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .remove("${provider}_username")
            .remove("${provider}_password")
            .remove("${provider}_iv")
            .apply()
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        keyStore.deleteEntry(KEY_ALIAS_PREFIX + provider)
    }

    private fun getOrCreateKey(provider: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val alias = KEY_ALIAS_PREFIX + provider
        val existing = keyStore.getKey(alias, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}
