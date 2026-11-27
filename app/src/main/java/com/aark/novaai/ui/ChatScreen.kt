package com.aark.novaai.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import com.aark.novaai.core.NovaPalette
import com.aark.novaai.ui.components.NovaUi
import org.json.JSONArray

/** Minimal ChatGPT-inspired conversation UI with NOVA colors and existing voice controls. */
class ChatScreen(
    private val activity: Activity,
    private val onHome: () -> Unit,
    private val onNewChat: () -> Unit,
    private val onSend: () -> Unit,
    private val onStop: () -> Unit,
    private val onVoiceInput: () -> Unit,
    private val onVoiceConversation: () -> Unit,
    private val onSpeakText: (String) -> Unit,
    private val onRegenerate: () -> Unit
) {
    private lateinit var messages: LinearLayout
    private lateinit var transcript: ScrollView
    private lateinit var status: TextView
    private lateinit var sendButton: TextView
    private lateinit var stopButton: TextView
    private lateinit var micButton: TextView
    private lateinit var voiceButton: TextView
    private lateinit var newChatButton: TextView
    lateinit var input: EditText
        private set

    fun create(): LinearLayout {
        val root = NovaUi.pageRoot(activity).apply {
            setPadding(NovaUi.dp(activity, 12), NovaUi.dp(activity, 7), NovaUi.dp(activity, 12), NovaUi.dp(activity, 7))
        }
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val menu = NovaUi.centerLabel(activity, "☰", 22f, Color.WHITE, true).apply {
            background = NovaUi.rounded(NovaPalette.surface2, 30f)
            setPadding(NovaUi.dp(activity, 13), NovaUi.dp(activity, 8), NovaUi.dp(activity, 13), NovaUi.dp(activity, 8))
            contentDescription = "Home"
            isClickable = true
            setOnClickListener { onHome() }
        }
        header.addView(menu)
        val brand = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(NovaUi.dp(activity, 11), 0, 0, 0)
            addView(NovaUi.label(activity, "NOVA", 19f, NovaPalette.cyan, true))
            addView(NovaUi.label(activity, "AI CHAT", 9f, NovaPalette.muted, true))
        }
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        newChatButton = NovaUi.centerLabel(activity, "＋", 24f, NovaPalette.cyan, true).apply {
            background = NovaUi.rounded(NovaPalette.surface2, 15f)
            setPadding(NovaUi.dp(activity, 11), NovaUi.dp(activity, 5), NovaUi.dp(activity, 11), NovaUi.dp(activity, 5))
            contentDescription = "New chat"
            isClickable = true
            setOnClickListener { onNewChat() }
        }
        header.addView(newChatButton)
        root.addView(header)

        transcript = ScrollView(activity).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(NovaPalette.background)
            setPadding(NovaUi.dp(activity, 3), NovaUi.dp(activity, 8), NovaUi.dp(activity, 3), NovaUi.dp(activity, 8))
        }
        messages = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, NovaUi.dp(activity, 8), 0, NovaUi.dp(activity, 12))
        }
        transcript.addView(messages)
        root.addView(transcript, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = NovaUi.dp(activity, 5)
            bottomMargin = NovaUi.dp(activity, 8)
        })

        status = NovaUi.label(activity, "", 10f, NovaPalette.muted).apply {
            visibility = View.GONE
            setPadding(NovaUi.dp(activity, 8), 0, 0, NovaUi.dp(activity, 4))
        }
        root.addView(status)

        val composer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = NovaUi.rounded(Color.rgb(20, 29, 49), 28f, Color.rgb(45, 62, 91))
            setPadding(NovaUi.dp(activity, 10), NovaUi.dp(activity, 7), NovaUi.dp(activity, 8), NovaUi.dp(activity, 7))
            elevation = NovaUi.dp(activity, 2).toFloat()
        }
        val inputRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        input = EditText(activity).apply {
            hint = "Message NOVA"
            setHintTextColor(Color.rgb(137, 151, 177))
            setTextColor(Color.WHITE)
            textSize = 16f
            minLines = 1
            maxLines = 5
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            imeOptions = EditorInfo.IME_ACTION_SEND
            background = null
            setPadding(NovaUi.dp(activity, 8), NovaUi.dp(activity, 10), NovaUi.dp(activity, 5), NovaUi.dp(activity, 10))
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) { onSend(); true } else false
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateComposerMode() }
                override fun afterTextChanged(s: Editable?) = Unit
            })
        }
        inputRow.addView(input, LinearLayout.LayoutParams(0, -2, 1f))
        micButton = NovaUi.centerLabel(activity, "🎙", 20f, Color.WHITE, true).apply {
            background = NovaUi.rounded(Color.rgb(35, 48, 73), 22f)
            setPadding(NovaUi.dp(activity, 8), NovaUi.dp(activity, 11), NovaUi.dp(activity, 8), NovaUi.dp(activity, 11))
            contentDescription = "Voice input"
            isClickable = true
            setOnClickListener { onVoiceInput() }
        }
        inputRow.addView(micButton, LinearLayout.LayoutParams(NovaUi.dp(activity, 46), NovaUi.dp(activity, 46)).apply {
            marginStart = NovaUi.dp(activity, 3)
        })
        voiceButton = NovaUi.centerLabel(activity, "≋", 26f, Color.WHITE, true).apply {
            background = NovaUi.rounded(Color.rgb(255, 119, 99), 24f)
            setPadding(NovaUi.dp(activity, 8), NovaUi.dp(activity, 8), NovaUi.dp(activity, 8), NovaUi.dp(activity, 8))
            contentDescription = "Start voice conversation"
            isClickable = true
            setOnClickListener { onVoiceConversation() }
        }
        inputRow.addView(voiceButton, LinearLayout.LayoutParams(NovaUi.dp(activity, 46), NovaUi.dp(activity, 46)).apply {
            marginStart = NovaUi.dp(activity, 7)
        })
        sendButton = NovaUi.centerLabel(activity, "↑", 27f, Color.WHITE, true).apply {
            background = NovaUi.rounded(Color.rgb(83, 190, 255), 24f)
            contentDescription = "Send message"
            isClickable = true
            visibility = View.GONE
            setOnClickListener { onSend() }
        }
        inputRow.addView(sendButton, LinearLayout.LayoutParams(NovaUi.dp(activity, 46), NovaUi.dp(activity, 46)).apply {
            marginStart = NovaUi.dp(activity, 7)
        })
        stopButton = NovaUi.centerLabel(activity, "■", 19f, Color.WHITE, true).apply {
            background = NovaUi.rounded(Color.rgb(176, 65, 83), 24f)
            contentDescription = "Stop response"
            isClickable = true
            visibility = View.GONE
            setOnClickListener { onStop() }
        }
        inputRow.addView(stopButton, LinearLayout.LayoutParams(NovaUi.dp(activity, 46), NovaUi.dp(activity, 46)).apply {
            marginStart = NovaUi.dp(activity, 7)
        })
        composer.addView(inputRow)
        root.addView(composer)
        return root
    }

    private fun updateComposerMode() {
        val hasText = input.text?.toString()?.isNotBlank() == true
        if (hasText) {
            voiceButton.visibility = View.GONE
            sendButton.visibility = View.VISIBLE
        } else {
            sendButton.visibility = View.GONE
            voiceButton.visibility = View.VISIBLE
        }
    }

    fun showWelcome() {
        messages.removeAllViews()
        val welcome = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(NovaUi.dp(activity, 12), NovaUi.dp(activity, 35), NovaUi.dp(activity, 12), NovaUi.dp(activity, 35))
        }
        val orb = NovaUi.centerLabel(activity, "✦", 42f, NovaPalette.cyan, true).apply {
            background = NovaUi.rounded(Color.rgb(25, 49, 83), 50f)
        }
        welcome.addView(orb, LinearLayout.LayoutParams(NovaUi.dp(activity, 82), NovaUi.dp(activity, 82)))
        welcome.addView(NovaUi.centerLabel(activity, "How can I help you?", 23f, Color.WHITE, true).apply {
            setPadding(0, NovaUi.dp(activity, 18), 0, NovaUi.dp(activity, 5))
        })
        welcome.addView(NovaUi.centerLabel(activity, "Ask me anything. I'm here to help.", 13f, NovaPalette.muted))
        messages.addView(welcome, LinearLayout.LayoutParams(-1, -2))
        input.setText("")
        scrollToBottom()
    }

    fun showNewChatWelcome() = showWelcome()

    fun addChatBubble(role: String, message: String) {
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (role == "user") Gravity.END else Gravity.START
            setPadding(0, NovaUi.dp(activity, 7), 0, NovaUi.dp(activity, 7))
        }
        val messageView = TextView(activity).apply {
            text = message
            textSize = 16f
            setTextColor(Color.WHITE)
            includeFontPadding = true
            if (role == "user") {
                val p = NovaUi.dp(activity, 13)
                setPadding(p, NovaUi.dp(activity, 10), p, NovaUi.dp(activity, 10))
                background = NovaUi.rounded(Color.rgb(34, 54, 86), 20f)
            } else {
                setPadding(NovaUi.dp(activity, 2), NovaUi.dp(activity, 4), NovaUi.dp(activity, 2), NovaUi.dp(activity, 4))
            }
            maxWidth = (activity.resources.displayMetrics.widthPixels * 0.88f).toInt()
        }
        container.addView(messageView, LinearLayout.LayoutParams(-2, -2))
        if (role != "user") attachResponseActions(messageView, message)
        messages.addView(container, LinearLayout.LayoutParams(-1, -2))
        scrollToBottom()
    }

    fun addStreamingBubble(): TextView {
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.START
            setPadding(0, NovaUi.dp(activity, 7), 0, NovaUi.dp(activity, 7))
        }
        val thinking = NovaUi.label(activity, "✦  NOVA is thinking…", 12f, NovaPalette.cyan, true)
        container.addView(thinking)
        val response = TextView(activity).apply {
            text = ""
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(NovaUi.dp(activity, 2), NovaUi.dp(activity, 6), NovaUi.dp(activity, 2), NovaUi.dp(activity, 4))
            maxWidth = (activity.resources.displayMetrics.widthPixels * 0.9f).toInt()
        }
        container.addView(response)
        messages.addView(container, LinearLayout.LayoutParams(-1, -2))
        scrollToBottom()
        return response
    }

    fun attachResponseActions(response: TextView, message: String) {
        val container = response.parent as? LinearLayout ?: return
        if (container.tag == "actions_added") return
        container.tag = "actions_added"
        if (container.childCount > 1) {
            val first = container.getChildAt(0)
            if (first is TextView && first.text.toString().startsWith("✦  NOVA is thinking")) {
                container.removeViewAt(0)
            }
        }
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, NovaUi.dp(activity, 7), 0, 0)
        }
        fun action(symbol: String, description: String, onClick: () -> Unit) {
            val button = NovaUi.centerLabel(activity, symbol, 18f, NovaPalette.muted, true).apply {
                contentDescription = description
                isClickable = true
                isFocusable = true
                setPadding(NovaUi.dp(activity, 8), NovaUi.dp(activity, 6), NovaUi.dp(activity, 8), NovaUi.dp(activity, 6))
                setOnClickListener { onClick() }
            }
            row.addView(button)
        }
        action("▢", "Copy response") {
            val clipboard = activity.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("NOVA response", message))
            Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
        }
        action("♡", "Like response") { Toast.makeText(activity, "Thanks for your feedback", Toast.LENGTH_SHORT).show() }
        action("☹", "Dislike response") { Toast.makeText(activity, "Feedback noted", Toast.LENGTH_SHORT).show() }
        action("◖))", "Listen to response") { onSpeakText(message) }
        action("↗", "Share response") {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            activity.startActivity(Intent.createChooser(intent, "Share NOVA response"))
        }
        val more = NovaUi.centerLabel(activity, "⋮", 20f, NovaPalette.muted, true).apply {
            contentDescription = "More response actions"
            isClickable = true
            setPadding(NovaUi.dp(activity, 8), NovaUi.dp(activity, 6), NovaUi.dp(activity, 8), NovaUi.dp(activity, 6))
            setOnClickListener {
                val popup = PopupMenu(activity, this)
                popup.menu.add("Regenerate response").setOnMenuItemClickListener { onRegenerate(); true }
                popup.menu.add("Copy response").setOnMenuItemClickListener {
                    val clipboard = activity.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("NOVA response", message))
                    Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
                    true
                }
                popup.menu.add("Share response").setOnMenuItemClickListener {
                    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message) }
                    activity.startActivity(Intent.createChooser(intent, "Share NOVA response"))
                    true
                }
                popup.show()
            }
        }
        row.addView(more)
        container.addView(row)
    }

    fun rebuildChatBubbles(history: JSONArray) {
        messages.removeAllViews()
        for (i in 0 until history.length()) {
            val item = history.optJSONObject(i) ?: continue
            val role = item.optString("role")
            val text = item.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty()
            if (text.isNotBlank()) addChatBubble(if (role == "user") "user" else "assistant", text)
        }
    }

    fun setStatus(value: String) {
        status.text = value
        status.visibility = if (value.isBlank()) View.GONE else View.VISIBLE
    }

    fun setInputText(value: String) {
        input.setText(value)
        input.setSelection(input.text?.length ?: 0)
    }

    fun setMicActive(active: Boolean) {
        micButton.alpha = if (active) 0.55f else 1f
        micButton.text = if (active) "…" else "🎙"
    }

    fun setVoiceModeState(active: Boolean, message: String) {
        micButton.visibility = if (active) View.GONE else View.VISIBLE
        voiceButton.text = if (active) "■" else "≋"
        voiceButton.background = NovaUi.rounded(if (active) Color.rgb(190, 63, 83) else Color.rgb(255, 119, 99), 24f)
        voiceButton.contentDescription = if (active) "Stop voice conversation" else "Start voice conversation"
        setStatus(message)
    }

    fun setRequestUi(running: Boolean, value: String) {
        setStatus(value)
        sendButton.isEnabled = !running
        stopButton.visibility = if (running) View.VISIBLE else View.GONE
        sendButton.visibility = if (!running && input.text?.isNotBlank() == true) View.VISIBLE else View.GONE
        voiceButton.visibility = if (!running && input.text?.isBlank() != false) View.VISIBLE else View.GONE
        newChatButton.isEnabled = !running
        newChatButton.alpha = if (running) 0.45f else 1f
    }

    fun scrollToBottom() {
        transcript.post { transcript.fullScroll(View.FOCUS_DOWN) }
    }
}
