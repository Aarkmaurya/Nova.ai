package com.aark.novaai

import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import android.app.Activity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private val bg = Color.rgb(8, 13, 30)
    private val panel = Color.rgb(18, 28, 55)
    private val cyan = Color.rgb(76, 205, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun baseLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(bg)
            gravity = Gravity.CENTER_HORIZONTAL
        }
    }

    private fun text(value: String, size: Float, color: Int = Color.WHITE): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
        }

    private fun button(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            setTextColor(Color.WHITE)
            setBackgroundColor(panel)
            setOnClickListener { action() }
        }

    private fun showHome() {
        root = baseLayout()
        root.addView(text("N O V A   A I", 30f, cyan), LinearLayout.LayoutParams(-1, 90))
        root.addView(text("Your personal AI assistant", 16f), LinearLayout.LayoutParams(-1, 42))
        val orb = TextView(this).apply {
            text = "✦"
            textSize = 72f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            setBackgroundColor(panel)
        }
        root.addView(orb, LinearLayout.LayoutParams(190, 190).apply {
            gravity = Gravity.CENTER
            setMargins(0, 40, 0, 28)
        })
        root.addView(text("NOVA is ready for setup", 17f, Color.LTGRAY))
        root.addView(button("API KEY SETTINGS") { showApiSettings() },
            LinearLayout.LayoutParams(-1, 58).apply { setMargins(0, 28, 0, 12) })
        root.addView(button("VOICE ASSISTANT  •  COMING NEXT") {
            Toast.makeText(this, "Voice module is planned for the next stage.", Toast.LENGTH_LONG).show()
        }, LinearLayout.LayoutParams(-1, 58))
        setContentView(root)
    }

    private fun securePrefs() = try {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            this, "nova_secure_settings", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        null
    }

    private fun showApiSettings() {
        root = baseLayout()
        root.addView(text("API KEY SETTINGS", 24f, cyan), LinearLayout.LayoutParams(-1, 70))
        root.addView(text("Gemini API key", 16f), LinearLayout.LayoutParams(-1, 42))
        val keyInput = EditText(this).apply {
            hint = "Paste Gemini API key"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setSingleLine(true)
            inputType =  android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setBackgroundColor(panel)
            setPadding(14, 12, 14, 12)
        }
        root.addView(keyInput, LinearLayout.LayoutParams(-1, 58))
        val prefs = securePrefs()
        if (prefs == null) {
            root.addView(text("Secure storage is unavailable on this device.", 13f, Color.YELLOW))
        } else {
            root.addView(button("SAVE API KEY") {
                val value = keyInput.text.toString().trim()
                if (value.isBlank()) {
                    Toast.makeText(this, "Please enter an API key.", Toast.LENGTH_SHORT).show()
                } else {
                    prefs.edit().putString("gemini_api_key", value).apply()
                    keyInput.text.clear()
                    Toast.makeText(this, "API key saved on this device.", Toast.LENGTH_LONG).show()
                }
            }, LinearLayout.LayoutParams(-1, 58).apply { setMargins(0, 18, 0, 12) })
            root.addView(button("DELETE SAVED KEY") {
                prefs.edit().remove("gemini_api_key").apply()
                Toast.makeText(this, "Saved key deleted.", Toast.LENGTH_SHORT).show()
            }, LinearLayout.LayoutParams(-1, 58))
        }
        root.addView(text(
            "Note: device-side encryption helps protect stored data, but a client-side API key is not fully secret. Do not share your key or upload it to GitHub.",
            12f, Color.LTGRAY
        ), LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 22, 0, 20) })
        root.addView(button("BACK") { showHome() }, LinearLayout.LayoutParams(-1, 54))
        setContentView(root)
    }
}
