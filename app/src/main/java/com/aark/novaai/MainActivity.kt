package com.aark.novaai

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MainActivity : Activity() {

    private val bg = Color.rgb(7, 12, 28)
    private val surface = Color.rgb(18, 27, 52)
    private val surface2 = Color.rgb(24, 37, 68)
    private val cyan = Color.rgb(83, 211, 255)
    private val purple = Color.rgb(155, 120, 255)
    private val muted = Color.rgb(165, 181, 211)
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = Color.rgb(10, 15, 31)
        showHome()
    }

    private fun rounded(color: Int, radius: Float = 26f, stroke: Int = Color.TRANSPARENT): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (stroke != Color.TRANSPARENT) setStroke(dp(1), stroke)
        }
    }

    private fun gradient(start: Int, end: Int, radius: Float = 30f): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(start, end)
        ).apply { cornerRadius = radius }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun label(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL
            if (bold) setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = true
        }
    }

    private fun centerLabel(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView {
        return label(value, size, color, bold).apply { gravity = Gravity.CENTER }
    }

    private fun card(padding: Int = 18): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
            background = rounded(surface, 25f, Color.rgb(39, 58, 91))
            elevation = dp(5).toFloat()
        }
    }

    // TextView-based action surface avoids Android's default all-caps/font-padding clipping.
    private fun actionTile(
        title: String,
        subtitle: String,
        symbol: String,
        accent: Int,
        onTap: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(17), dp(15), dp(17), dp(15))
            background = gradient(surface2, surface, 24f)
            elevation = dp(4).toFloat()
            isClickable = true
            isFocusable = true
            setOnClickListener { onTap() }

            val icon = centerLabel(symbol, 23f, accent, true).apply {
                background = rounded(Color.rgb(29, 47, 82), 18f)
            }
            addView(icon, LinearLayout.LayoutParams(dp(54), dp(54)))

            val textBox = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), 0, 0, 0)
                addView(label(title, 16f, Color.WHITE, true))
                addView(label(subtitle, 12f, muted).apply {
                    setPadding(0, dp(4), 0, 0)
                })
            }
            addView(textBox, LinearLayout.LayoutParams(0, -2, 1f))
            addView(centerLabel("›", 27f, accent, true))
        }
    }

    private fun pageRoot(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(28))
            setBackgroundColor(bg)
        }
    }

    private fun installScrollable(content: LinearLayout) {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(bg)
            addView(content)
        }
        setContentView(scroll)
    }

    private fun showHome() {
        root = pageRoot()

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(label("NOVA AI", 27f, cyan, true))
            addView(label("YOUR PERSONAL AI SPACE", 10f, muted, true))
        }
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(centerLabel("●  READY", 11f, Color.rgb(112, 238, 190), true).apply {
            background = rounded(Color.rgb(20, 55, 55), 30f)
            setPadding(dp(12), dp(9), dp(12), dp(9))
        })
        root.addView(top)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(24), dp(18), dp(24))
            background = gradient(Color.rgb(24, 48, 87), Color.rgb(37, 28, 78), 30f)
            elevation = dp(7).toFloat()
        }
        val orb = centerLabel("✦", 66f, cyan, true).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(35, 88, 132), Color.rgb(76, 58, 132))
            ).apply {
                shape = GradientDrawable.OVAL
                setStroke(dp(2), Color.rgb(93, 189, 255))
            }
            elevation = dp(12).toFloat()
        }
        hero.addView(orb, LinearLayout.LayoutParams(dp(124), dp(124)))
        hero.addView(centerLabel("A smarter space, built around you", 18f, Color.WHITE, true).apply {
            setPadding(0, dp(18), 0, dp(5))
        })
        hero.addView(centerLabel("Set up NOVA to begin your AI journey", 12f, muted))
        root.addView(hero, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(26)
        })

        root.addView(label("QUICK SETUP", 12f, muted, true).apply {
            setPadding(dp(3), dp(25), 0, dp(12))
        })

        root.addView(actionTile(
            "API key settings",
            "Connect your Gemini key securely",
            "⌘",
            cyan
        ) { showApiSettings() }, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(12)
        })

        root.addView(actionTile(
            "Voice assistant",
            "Voice features are planned for the next stage",
            "♫",
            purple
        ) {
            Toast.makeText(this, "Voice assistant is not implemented yet.", Toast.LENGTH_LONG).show()
        })

        val gridTitle = label("YOUR NOVA WORKSPACE", 12f, muted, true).apply {
            setPadding(dp(3), dp(25), 0, dp(12))
        }
        root.addView(gridTitle)

        val grid = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val vision = miniCard("◉", "Screen vision", "Coming later", cyan)
        val privacy = miniCard("◇", "Privacy", "You stay in control", purple)
        grid.addView(vision, LinearLayout.LayoutParams(0, dp(130), 1f).apply {
            marginEnd = dp(8)
        })
        grid.addView(privacy, LinearLayout.LayoutParams(0, dp(130), 1f).apply {
            marginStart = dp(8)
        })
        root.addView(grid)

        root.addView(centerLabel("NOVA AI  •  VERSION 0.1.0", 10f, Color.rgb(105, 124, 158)).apply {
            setPadding(0, dp(26), 0, dp(4))
        })

        installScrollable(root)
    }

    private fun miniCard(icon: String, title: String, subtitle: String, accent: Int): LinearLayout {
        return card(14).apply {
            addView(centerLabel(icon, 25f, accent, true).apply {
                gravity = Gravity.START
            })
            addView(label(title, 14f, Color.WHITE, true).apply {
                setPadding(0, dp(9), 0, dp(3))
            })
            addView(label(subtitle, 11f, muted))
        }
    }

    private fun securePrefs() = try {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            this,
            "nova_secure_settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        null
    }

    private fun showApiSettings() {
        root = pageRoot()

        root.addView(centerLabel("API KEY SETTINGS", 23f, cyan, true).apply {
            setPadding(0, dp(14), 0, dp(22))
        })

        val info = card(18).apply {
            addView(label("Connect your AI provider", 17f, Color.WHITE, true))
            addView(label(
                "Add your Gemini API key. It is stored in encrypted local preferences on this device.",
                13f,
                muted
            ).apply { setPadding(0, dp(7), 0, 0) })
        }
        root.addView(info, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(18)
        })

        root.addView(label("GEMINI API KEY", 11f, muted, true).apply {
            setPadding(dp(3), 0, 0, dp(8))
        })

        val keyInput = EditText(this).apply {
            hint = "Paste your Gemini API key"
            setHintTextColor(Color.rgb(125, 143, 176))
            setTextColor(Color.WHITE)
            textSize = 14f
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            background = rounded(surface2, 18f, Color.rgb(55, 79, 119))
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }
        root.addView(keyInput, LinearLayout.LayoutParams(-1, dp(58)))

        val prefs = securePrefs()
        if (prefs == null) {
            root.addView(label("Secure storage is unavailable on this device.", 13f, Color.YELLOW))
        } else {
            root.addView(actionTile("Save API key", "Store this key on this device", "✓", cyan) {
                val value = keyInput.text.toString().trim()
                if (value.isBlank()) {
                    Toast.makeText(this, "Please enter an API key.", Toast.LENGTH_SHORT).show()
                } else {
                    prefs.edit().putString("gemini_api_key", value).apply()
                    keyInput.text.clear()
                    Toast.makeText(this, "API key saved on this device.", Toast.LENGTH_LONG).show()
                }
            }, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(16)
                bottomMargin = dp(10)
            })

            root.addView(actionTile("Delete saved key", "Remove the locally stored key", "×", Color.rgb(255, 130, 150)) {
                prefs.edit().remove("gemini_api_key").apply()
                Toast.makeText(this, "Saved key deleted.", Toast.LENGTH_SHORT).show()
            })
        }

        root.addView(label(
            "Security note: device-side encryption helps protect stored data, but a client-side API key is not fully secret. Never share your key or upload it to GitHub.",
            12f,
            muted
        ).apply { setPadding(dp(4), dp(20), dp(4), dp(20)) })

        root.addView(actionTile("Back to home", "Return to your NOVA dashboard", "‹", purple) {
            showHome()
        })

        installScrollable(root)
    }
}
