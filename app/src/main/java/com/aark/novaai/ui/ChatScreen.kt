package com.aark.novaai.ui

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.aark.novaai.core.NovaPalette
import com.aark.novaai.ui.components.NovaUi
import org.json.JSONArray

class ChatScreen(
    private val activity: Activity,
    private val onHome: () -> Unit,
    private val onSend: () -> Unit,
    private val onStop: () -> Unit,
    private val onVoiceInput: () -> Unit,
    private val onReadResponse: () -> Unit
) {
    private lateinit var messages: LinearLayout
    private lateinit var transcript: ScrollView
    private lateinit var status: TextView
    private lateinit var sendButton: TextView
    private lateinit var stopButton: TextView
    private lateinit var micButton: TextView
    lateinit var input: EditText
        private set

    fun create(): LinearLayout {
        val root = NovaUi.pageRoot(activity).apply {
            setPadding(NovaUi.dp(activity, 14), NovaUi.dp(activity, 12), NovaUi.dp(activity, 14), NovaUi.dp(activity, 12))
        }
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleBox = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            addView(NovaUi.label(activity, "NOVA CHAT", 21f, NovaPalette.cyan, true))
            addView(NovaUi.label(activity, "Gemini • text conversation", 11f, NovaPalette.muted))
        }
        header.addView(titleBox, LinearLayout.LayoutParams(0, -2, 1f))
        val back = NovaUi.centerLabel(activity, "‹ Home", 13f, Color.WHITE, true).apply {
            val h = NovaUi.dp(activity, 10); val w = NovaUi.dp(activity, 12)
            setPadding(w, h, w, h)
            background = NovaUi.rounded(NovaPalette.surface2, 18f)
            isClickable = true
            setOnClickListener { onHome() }
        }
        header.addView(back)
        root.addView(header)

        status = NovaUi.label(activity, "Ready when you are", 11f, NovaPalette.muted).apply {
            setPadding(NovaUi.dp(activity, 3), NovaUi.dp(activity, 12), 0, NovaUi.dp(activity, 8))
        }
        root.addView(status)
        transcript = ScrollView(activity).apply {
            isFillViewport = false
            clipToPadding = false
            background = NovaUi.rounded(Color.rgb(11, 19, 39), 22f)
            val h = NovaUi.dp(activity, 12); val w = NovaUi.dp(activity, 10)
            setPadding(w, h, w, h)
        }
        messages = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        transcript.addView(messages)
        root.addView(transcript, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = NovaUi.dp(activity, 5); bottomMargin = NovaUi.dp(activity, 12)
        })

        val composer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = NovaUi.rounded(NovaPalette.surface, 22f, Color.rgb(39, 58, 91))
            val p = NovaUi.dp(activity, 10)
            setPadding(p, p, p, p)
        }
        input = EditText(activity).apply {
            hint = "Message NOVA…"
            setHintTextColor(Color.rgb(125, 143, 176))
            setTextColor(Color.WHITE)
            textSize = 15f
            minLines = 1
            maxLines = 4
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            imeOptions = EditorInfo.IME_ACTION_SEND
            background = NovaUi.rounded(NovaPalette.surface2, 16f)
            val p = NovaUi.dp(activity, 13)
            setPadding(p, NovaUi.dp(activity, 12), p, NovaUi.dp(activity, 12))
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) { onSend(); true } else false
            }
        }
        val inputRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        inputRow.addView(input, LinearLayout.LayoutParams(0, -2, 1f))
        micButton = NovaUi.centerLabel(activity, "MIC", 11f, Color.WHITE, true).apply {
            background = NovaUi.rounded(Color.rgb(37, 61, 103), 15f)
            val p = NovaUi.dp(activity, 12)
            setPadding(p, NovaUi.dp(activity, 15), p, NovaUi.dp(activity, 15))
            contentDescription = "Start voice input"
            isClickable = true
            setOnClickListener { onVoiceInput() }
        }
        inputRow.addView(micButton, LinearLayout.LayoutParams(-2, -2).apply {
            marginStart = NovaUi.dp(activity, 8)
        })
        composer.addView(inputRow)
        val controls = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        sendButton = NovaUi.centerLabel(activity, "SEND  ↑", 13f, Color.rgb(5, 20, 36), true).apply {
            background = NovaUi.gradient(NovaPalette.cyan, Color.rgb(107, 164, 255), 16f)
            val h = NovaUi.dp(activity, 12); val w = NovaUi.dp(activity, 20)
            setPadding(w, h, w, h)
            isClickable = true
            setOnClickListener { onSend() }
        }
        stopButton = NovaUi.centerLabel(activity, "STOP", 12f, Color.WHITE, true).apply {
            background = NovaUi.rounded(Color.rgb(95, 43, 65), 16f)
            val h = NovaUi.dp(activity, 12); val w = NovaUi.dp(activity, 17)
            setPadding(w, h, w, h)
            visibility = View.GONE
            isClickable = true
            setOnClickListener { onStop() }
        }
        controls.addView(NovaUi.label(activity, "Gemini responses may use API quota", 9f, NovaPalette.muted),
            LinearLayout.LayoutParams(0, -2, 1f))
        val readButton = NovaUi.centerLabel(activity, "READ", 10f, Color.WHITE, true).apply {
            background = NovaUi.rounded(Color.rgb(54, 48, 91), 14f)
            val h = NovaUi.dp(activity, 11); val w = NovaUi.dp(activity, 10)
            setPadding(w, h, w, h)
            isClickable = true
            setOnClickListener { onReadResponse() }
            contentDescription = "Read latest NOVA response aloud"
        }
        controls.addView(readButton, LinearLayout.LayoutParams(-2, -2).apply {
            marginEnd = NovaUi.dp(activity, 6)
        })
        controls.addView(stopButton)
        controls.addView(sendButton, LinearLayout.LayoutParams(-2, -2).apply { marginStart = NovaUi.dp(activity, 8) })
        composer.addView(controls, LinearLayout.LayoutParams(-1, -2).apply { topMargin = NovaUi.dp(activity, 9) })
        root.addView(composer)
        return root
    }

    fun addChatBubble(role: String, message: String) {
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (role == "user") Gravity.END else Gravity.START
            setPadding(0, NovaUi.dp(activity, 5), 0, NovaUi.dp(activity, 5))
        }
        val bubble = TextView(activity).apply {
            text = message
            textSize = 14f
            setTextColor(Color.WHITE)
            val p = NovaUi.dp(activity, 14)
            setPadding(p, NovaUi.dp(activity, 11), p, NovaUi.dp(activity, 11))
            background = if (role == "user") NovaUi.gradient(Color.rgb(35, 92, 132), Color.rgb(45, 66, 125), 19f)
                else NovaUi.rounded(Color.rgb(27, 39, 67), 19f, Color.rgb(50, 72, 111))
            maxWidth = (activity.resources.displayMetrics.widthPixels * 0.82f).toInt()
        }
        container.addView(bubble, LinearLayout.LayoutParams(-2, -2))
        messages.addView(container, LinearLayout.LayoutParams(-1, -2))
        scrollToBottom()
    }

    fun addStreamingBubble(): TextView {
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.START
            setPadding(0, NovaUi.dp(activity, 5), 0, NovaUi.dp(activity, 5))
        }
        val bubble = TextView(activity).apply {
            text = ""
            textSize = 14f
            setTextColor(Color.WHITE)
            val p = NovaUi.dp(activity, 14)
            setPadding(p, NovaUi.dp(activity, 11), p, NovaUi.dp(activity, 11))
            background = NovaUi.rounded(Color.rgb(27, 39, 67), 19f, Color.rgb(50, 72, 111))
            maxWidth = (activity.resources.displayMetrics.widthPixels * 0.82f).toInt()
        }
        container.addView(bubble, LinearLayout.LayoutParams(-2, -2))
        messages.addView(container, LinearLayout.LayoutParams(-1, -2))
        scrollToBottom()
        return bubble
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

    fun setStatus(value: String) { status.text = value }

    fun setInputText(value: String) {
        input.setText(value)
        input.setSelection(input.text?.length ?: 0)
    }

    fun setMicActive(active: Boolean) {
        micButton.text = if (active) "…" else "MIC"
        micButton.alpha = if (active) 0.65f else 1f
        micButton.contentDescription = if (active) "Listening" else "Start voice input"
    }

    fun setRequestUi(running: Boolean, value: String) {
        status.text = value
        sendButton.isEnabled = !running
        sendButton.alpha = if (running) 0.45f else 1f
        stopButton.visibility = if (running) View.VISIBLE else View.GONE
    }

    fun scrollToBottom() {
        transcript.post { transcript.fullScroll(View.FOCUS_DOWN) }
    }
}
