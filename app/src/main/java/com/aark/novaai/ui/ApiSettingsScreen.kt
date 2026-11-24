package com.aark.novaai.ui

import android.app.Activity
import android.graphics.Color
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.aark.novaai.core.NovaPalette
import com.aark.novaai.security.ApiKeyManager
import com.aark.novaai.ui.components.NovaUi

class ApiSettingsScreen(
    private val activity: Activity,
    private val apiKeyManager: ApiKeyManager,
    private val onBack: () -> Unit
) {
    fun create(): LinearLayout {
        val root = NovaUi.pageRoot(activity)
        root.addView(NovaUi.centerLabel(activity, "API KEY SETTINGS", 23f, NovaPalette.cyan, true).apply {
            setPadding(0, NovaUi.dp(activity, 14), 0, NovaUi.dp(activity, 22))
        })
        val info = NovaUi.card(activity, 18).apply {
            addView(NovaUi.label(activity, "Connect your AI provider", 17f, Color.WHITE, true))
            addView(NovaUi.label(activity,
                "Add your Gemini API key. It is stored in encrypted local preferences on this device.", 13f, NovaPalette.muted).apply {
                setPadding(0, NovaUi.dp(activity, 7), 0, 0)
            })
        }
        root.addView(info, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = NovaUi.dp(activity, 18) })
        root.addView(NovaUi.label(activity, "GEMINI API KEY", 11f, NovaPalette.muted, true).apply {
            setPadding(NovaUi.dp(activity, 3), 0, 0, NovaUi.dp(activity, 8))
        })
        val keyInput = EditText(activity).apply {
            hint = "Paste your Gemini API key"
            setHintTextColor(Color.rgb(125, 143, 176))
            setTextColor(Color.WHITE)
            textSize = 14f
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            background = NovaUi.rounded(NovaPalette.surface2, 18f, Color.rgb(55, 79, 119))
            val p = NovaUi.dp(activity, 16)
            setPadding(p, NovaUi.dp(activity, 14), p, NovaUi.dp(activity, 14))
        }
        root.addView(keyInput, LinearLayout.LayoutParams(-1, NovaUi.dp(activity, 58)))
        if (!apiKeyManager.isAvailable()) {
            root.addView(NovaUi.label(activity, "Secure storage is unavailable on this device.", 13f, Color.YELLOW))
        } else {
            root.addView(NovaUi.actionTile(activity, "Save API key", "Store this key on this device", "✓", NovaPalette.cyan) {
                val value = keyInput.text.toString().trim()
                if (value.isBlank()) Toast.makeText(activity, "Please enter an API key.", Toast.LENGTH_SHORT).show()
                else {
                    apiKeyManager.saveApiKey(value)
                    keyInput.text.clear()
                    Toast.makeText(activity, "API key saved on this device.", Toast.LENGTH_LONG).show()
                }
            }, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = NovaUi.dp(activity, 16); bottomMargin = NovaUi.dp(activity, 10)
            })
            root.addView(NovaUi.actionTile(activity, "Delete saved key", "Remove the locally stored key", "×", Color.rgb(255, 130, 150)) {
                apiKeyManager.deleteApiKey()
                Toast.makeText(activity, "Saved key deleted.", Toast.LENGTH_SHORT).show()
            })
        }
        root.addView(NovaUi.label(activity,
            "Security note: device-side encryption helps protect stored data, but a client-side API key is not fully secret. Never share your key or upload it to GitHub.",
            12f, NovaPalette.muted).apply {
            val p = NovaUi.dp(activity, 4)
            setPadding(p, NovaUi.dp(activity, 20), p, NovaUi.dp(activity, 20))
        })
        root.addView(NovaUi.actionTile(activity, "Back to home", "Return to your NOVA dashboard", "‹", NovaPalette.purple, onBack))
        return root
    }

    fun show() = NovaUi.installScrollable(activity, create())
}
