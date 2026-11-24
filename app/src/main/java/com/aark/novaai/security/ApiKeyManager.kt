package com.aark.novaai.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aark.novaai.core.NovaConstants

/** Stores the Gemini key in encrypted, device-local preferences. */
class ApiKeyManager(context: Context) {
    private val preferences = try {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            NovaConstants.SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        null
    }

    fun isAvailable(): Boolean = preferences != null
    fun getApiKey(): String? = preferences?.getString(NovaConstants.GEMINI_API_KEY_PREF, null)
    fun saveApiKey(value: String) { preferences?.edit()?.putString(NovaConstants.GEMINI_API_KEY_PREF, value)?.apply() }
    fun deleteApiKey() { preferences?.edit()?.remove(NovaConstants.GEMINI_API_KEY_PREF)?.apply() }

}
