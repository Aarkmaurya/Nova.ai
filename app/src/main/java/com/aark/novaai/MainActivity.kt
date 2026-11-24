package com.aark.novaai

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.aark.novaai.ai.GeminiStreamingClient
import com.aark.novaai.chat.ChatManager
import com.aark.novaai.core.NovaPalette
import com.aark.novaai.security.ApiKeyManager

class MainActivity : Activity() {
    private val bg = NovaPalette.background
    private val surface = NovaPalette.surface
    private val surface2 = NovaPalette.surface2
    private val cyan = NovaPalette.cyan
    private val purple = NovaPalette.purple
    private val muted = NovaPalette.muted
    private val apiKeyManager by lazy { ApiKeyManager(this) }
    private val chatManager = ChatManager()
    private val geminiClient = GeminiStreamingClient()
    private lateinit var root: LinearLayout

    private val networkExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    @Volatile private var requestRunning = false
    private var chatMessages: LinearLayout? = null
    private var chatScroll: ScrollView? = null
    private var chatInput: EditText? = null
    private var sendTile: TextView? = null
    private var stopTile: TextView? = null
    private var chatStatus: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = NovaPalette.navigationBar
        showHome()
    }

    override fun onDestroy() {
        stopRequest(showMessage = false)
        networkExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun rounded(color: Int, radius: Float = 26f, stroke: Int = Color.TRANSPARENT): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (stroke != Color.TRANSPARENT) setStroke(dp(1), stroke)
        }

    private fun gradient(start: Int, end: Int, radius: Float = 30f): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(start, end)).apply {
            cornerRadius = radius
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun label(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL
            if (bold) setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = true
        }

    private fun centerLabel(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView =
        label(value, size, color, bold).apply { gravity = Gravity.CENTER }

    private fun card(padding: Int = 18): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
        background = rounded(surface, 25f, Color.rgb(39, 58, 91))
        elevation = dp(5).toFloat()
    }

    private fun actionTile(title: String, subtitle: String, symbol: String, accent: Int, onTap: () -> Unit): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(17), dp(15), dp(17), dp(15))
            background = gradient(surface2, surface, 24f)
            elevation = dp(4).toFloat()
            isClickable = true
            isFocusable = true
            setOnClickListener { onTap() }
            addView(centerLabel(symbol, 23f, accent, true).apply {
                background = rounded(Color.rgb(29, 47, 82), 18f)
            }, LinearLayout.LayoutParams(dp(54), dp(54)))
            val textBox = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), 0, 0, 0)
                addView(label(title, 16f, Color.WHITE, true))
                addView(label(subtitle, 12f, muted).apply { setPadding(0, dp(4), 0, 0) })
            }
            addView(textBox, LinearLayout.LayoutParams(0, -2, 1f))
            addView(centerLabel("›", 27f, accent, true))
        }

    private fun pageRoot(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(16), dp(20), dp(28))
        setBackgroundColor(bg)
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
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(35, 88, 132), Color.rgb(76, 58, 132))).apply {
                shape = GradientDrawable.OVAL
                setStroke(dp(2), Color.rgb(93, 189, 255))
            }
            elevation = dp(12).toFloat()
        }
        hero.addView(orb, LinearLayout.LayoutParams(dp(124), dp(124)))
        hero.addView(centerLabel("A smarter space, built around you", 18f, Color.WHITE, true).apply {
            setPadding(0, dp(18), 0, dp(5))
        })
        hero.addView(centerLabel("Your AI workspace is ready to explore", 12f, muted))
        root.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(26) })

        root.addView(label("QUICK SETUP", 12f, muted, true).apply {
            setPadding(dp(3), dp(25), 0, dp(12))
        })
        root.addView(actionTile("API key settings", "Connect your Gemini key securely", "⌘", cyan) {
            showApiSettings()
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
        root.addView(actionTile("Gemini chat", "Ask questions and get AI responses", "✧", purple) {
            showChat()
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
        root.addView(actionTile("Voice assistant", "Voice features are planned for a later stage", "♫", purple) {
            Toast.makeText(this, "Voice assistant is not implemented yet.", Toast.LENGTH_LONG).show()
        })

        root.addView(label("YOUR NOVA WORKSPACE", 12f, muted, true).apply {
            setPadding(dp(3), dp(25), 0, dp(12))
        })
        val grid = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        grid.addView(miniCard("◉", "Screen vision", "Coming later", cyan),
            LinearLayout.LayoutParams(0, dp(130), 1f).apply { marginEnd = dp(8) })
        grid.addView(miniCard("◇", "Privacy", "You stay in control", purple),
            LinearLayout.LayoutParams(0, dp(130), 1f).apply { marginStart = dp(8) })
        root.addView(grid)
        root.addView(centerLabel("NOVA AI  •  GEMINI CHAT PREVIEW", 10f, Color.rgb(105, 124, 158)).apply {
            setPadding(0, dp(26), 0, dp(4))
        })
        installScrollable(root)
    }

    private fun miniCard(icon: String, title: String, subtitle: String, accent: Int): LinearLayout = card(14).apply {
        addView(centerLabel(icon, 25f, accent, true).apply { gravity = Gravity.START })
        addView(label(title, 14f, Color.WHITE, true).apply { setPadding(0, dp(9), 0, dp(3)) })
        addView(label(subtitle, 11f, muted))
    }

    private fun showApiSettings() {
        root = pageRoot()
        root.addView(centerLabel("API KEY SETTINGS", 23f, cyan, true).apply {
            setPadding(0, dp(14), 0, dp(22))
        })
        val info = card(18).apply {
            addView(label("Connect your AI provider", 17f, Color.WHITE, true))
            addView(label("Add your Gemini API key. It is stored in encrypted local preferences on this device.",
                13f, muted).apply { setPadding(0, dp(7), 0, 0) })
        }
        root.addView(info, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(18) })
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
        if (!apiKeyManager.isAvailable()) {
            root.addView(label("Secure storage is unavailable on this device.", 13f, Color.YELLOW))
        } else {
            root.addView(actionTile("Save API key", "Store this key on this device", "✓", cyan) {
                val value = keyInput.text.toString().trim()
                if (value.isBlank()) Toast.makeText(this, "Please enter an API key.", Toast.LENGTH_SHORT).show()
                else {
                    apiKeyManager.saveApiKey(value)
                    keyInput.text.clear()
                    Toast.makeText(this, "API key saved on this device.", Toast.LENGTH_LONG).show()
                }
            }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16); bottomMargin = dp(10) })
            root.addView(actionTile("Delete saved key", "Remove the locally stored key", "×", Color.rgb(255, 130, 150)) {
                apiKeyManager.deleteApiKey()
                Toast.makeText(this, "Saved key deleted.", Toast.LENGTH_SHORT).show()
            })
        }
        root.addView(label("Security note: device-side encryption helps protect stored data, but a client-side API key is not fully secret. Never share your key or upload it to GitHub.",
            12f, muted).apply { setPadding(dp(4), dp(20), dp(4), dp(20)) })
        root.addView(actionTile("Back to home", "Return to your NOVA dashboard", "‹", purple) { showHome() })
        installScrollable(root)
    }

    private fun showChat() {
        root = pageRoot()
        root.setPadding(dp(14), dp(12), dp(14), dp(12))
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(label("NOVA CHAT", 21f, cyan, true))
            addView(label("Gemini • text conversation", 11f, muted))
        }
        header.addView(titleBox, LinearLayout.LayoutParams(0, -2, 1f))
        val back = centerLabel("‹ Home", 13f, Color.WHITE, true).apply {
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(surface2, 18f)
            isClickable = true
            setOnClickListener { showHome() }
        }
        header.addView(back)
        root.addView(header)

        chatStatus = label("Ready when you are", 11f, muted).apply {
            setPadding(dp(3), dp(12), 0, dp(8))
        }
        root.addView(chatStatus)
        chatScroll = ScrollView(this).apply {
            isFillViewport = false
            clipToPadding = false
            background = rounded(Color.rgb(11, 19, 39), 22f)
            setPadding(dp(10), dp(12), dp(10), dp(12))
        }
        val messagesView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        chatMessages = messagesView
        chatScroll!!.addView(messagesView)
        root.addView(chatScroll, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = dp(5); bottomMargin = dp(12)
        })
        if (chatManager.size() == 0) addChatBubble("assistant", "Hello! I’m NOVA. Ask me anything to get started.")
        else rebuildChatBubbles()

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(surface, 22f, Color.rgb(39, 58, 91))
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        chatInput = EditText(this).apply {
            hint = "Message NOVA…"
            setHintTextColor(Color.rgb(125, 143, 176))
            setTextColor(Color.WHITE)
            textSize = 15f
            minLines = 1
            maxLines = 4
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            imeOptions = EditorInfo.IME_ACTION_SEND
            background = rounded(surface2, 16f)
            setPadding(dp(13), dp(12), dp(13), dp(12))
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
            }
        }
        composer.addView(chatInput, LinearLayout.LayoutParams(-1, -2))
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        sendTile = centerLabel("SEND  ↑", 13f, Color.rgb(5, 20, 36), true).apply {
            background = gradient(cyan, Color.rgb(107, 164, 255), 16f)
            setPadding(dp(20), dp(12), dp(20), dp(12))
            isClickable = true
            setOnClickListener { sendMessage() }
        }
        stopTile = centerLabel("STOP", 12f, Color.WHITE, true).apply {
            background = rounded(Color.rgb(95, 43, 65), 16f)
            setPadding(dp(17), dp(12), dp(17), dp(12))
            visibility = View.GONE
            isClickable = true
            setOnClickListener { stopRequest() }
        }
        controls.addView(label("Responses may use your Gemini API quota", 10f, muted),
            LinearLayout.LayoutParams(0, -2, 1f))
        controls.addView(stopTile!!)
        controls.addView(sendTile!!, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(8) })
        composer.addView(controls, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(9) })
        root.addView(composer)
        installScrollableChat(root)
        scrollChatToBottom()
    }

    private fun installScrollableChat(content: LinearLayout) {
        // Chat owns its own transcript scroller; the page itself must not scroll.
        setContentView(content)
    }

    private fun addChatBubble(role: String, message: String) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (role == "user") Gravity.END else Gravity.START
            setPadding(0, dp(5), 0, dp(5))
        }
        val bubble = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dp(14), dp(11), dp(14), dp(11))
            background = if (role == "user") gradient(Color.rgb(35, 92, 132), Color.rgb(45, 66, 125), 19f)
                else rounded(Color.rgb(27, 39, 67), 19f, Color.rgb(50, 72, 111))
            maxWidth = (resources.displayMetrics.widthPixels * 0.82f).toInt()
        }
        container.addView(bubble, LinearLayout.LayoutParams(-2, -2))
        chatMessages?.addView(container, LinearLayout.LayoutParams(-1, -2))
        scrollChatToBottom()
    }

    private fun addStreamingBubble(): TextView {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.START
            setPadding(0, dp(5), 0, dp(5))
        }
        val bubble = TextView(this).apply {
            text = ""
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dp(14), dp(11), dp(14), dp(11))
            background = rounded(Color.rgb(27, 39, 67), 19f, Color.rgb(50, 72, 111))
            maxWidth = (resources.displayMetrics.widthPixels * 0.82f).toInt()
        }
        container.addView(bubble, LinearLayout.LayoutParams(-2, -2))
        chatMessages?.addView(container, LinearLayout.LayoutParams(-1, -2))
        scrollChatToBottom()
        return bubble
    }

    private fun rebuildChatBubbles() {
        chatMessages?.removeAllViews()
        for (i in 0 until chatManager.size()) {
            val item = chatManager.messageAt(i) ?: continue
            val role = item.optString("role")
            val text = item.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty()
            if (text.isNotBlank()) addChatBubble(if (role == "user") "user" else "assistant", text)
        }
    }

    private fun scrollChatToBottom() {
        chatScroll?.post { chatScroll?.fullScroll(View.FOCUS_DOWN) }
    }

    private fun sendMessage() {
        if (requestRunning) return
        val prompt = chatInput?.text?.toString()?.trim().orEmpty()
        if (prompt.isBlank()) return
        val apiKey = apiKeyManager.getApiKey()
        if (apiKey.isNullOrBlank()) {
            Toast.makeText(this, "First save your Gemini API key in API key settings.", Toast.LENGTH_LONG).show()
            return
        }
        chatInput?.text?.clear()
        chatManager.addUserMessage(prompt)
        addChatBubble("user", prompt)
        geminiClient.resetCancellation()
        setRequestUi(true, "NOVA is thinking…")
        networkExecutor.execute { performGeminiRequest(apiKey) }
    }

    private fun performGeminiRequest(apiKey: String) {
        geminiClient.execute(apiKey, chatManager.history, object : GeminiStreamingClient.Listener {
            private var liveBubble: TextView? = null

            override fun onStarted() {
                if (!isFinishing) {
                    liveBubble = addStreamingBubble()
                    chatStatus?.text = "NOVA is replying…"
                }
            }

            override fun onChunk(text: String) {
                if (!isFinishing) {
                    if (liveBubble == null) liveBubble = addStreamingBubble()
                    liveBubble?.text = text
                    scrollChatToBottom()
                }
            }

            override fun onCompleted(answer: String, firstTokenSeconds: Double, totalSeconds: Double) {
                chatManager.addModelMessage(answer)
                if (!isFinishing) {
                    liveBubble?.text = answer
                    setRequestUi(false, "First text ${String.format(java.util.Locale.US, "%.1f", firstTokenSeconds)}s • Total ${String.format(java.util.Locale.US, "%.1f", totalSeconds)}s")
                }
            }

            override fun onFailed(message: String, partialAnswer: String) {
                if (!isFinishing) {
                    if (partialAnswer.isEmpty()) addChatBubble("assistant", message)
                    else liveBubble?.text = partialAnswer + "\n\n[Stream interrupted: $message]"
                    setRequestUi(false, "Something needs attention")
                }
            }

            override fun onCancelled() {
                if (!isFinishing) setRequestUi(false, "Request stopped")
            }
        })
    }

    private fun setRequestUi(running: Boolean, status: String) {
        requestRunning = running
        chatStatus?.text = status
        sendTile?.isEnabled = !running
        sendTile?.alpha = if (running) 0.45f else 1f
        stopTile?.visibility = if (running) View.VISIBLE else View.GONE
    }

    private fun stopRequest(showMessage: Boolean = true) {
        if (!requestRunning) return
        geminiClient.cancel()
        requestRunning = false
        setRequestUi(false, "Request stopped")
        if (showMessage && chatMessages != null) addChatBubble("assistant", "Request stopped.")
    }

}
