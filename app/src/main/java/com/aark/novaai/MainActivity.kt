package com.aark.novaai

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.aark.novaai.ai.GeminiStreamingClient
import com.aark.novaai.chat.ChatManager
import com.aark.novaai.core.NovaPalette
import com.aark.novaai.security.ApiKeyManager
import com.aark.novaai.ui.ApiSettingsScreen
import com.aark.novaai.ui.ChatScreen
import com.aark.novaai.ui.HomeDashboardScreen
import com.aark.novaai.voice.VoiceManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Activity coordinator: screen navigation and Gemini request lifecycle only. */
class MainActivity : Activity() {
    private val apiKeyManager by lazy { ApiKeyManager(this) }
    private val chatManager = ChatManager()
    private val geminiClient = GeminiStreamingClient()
    private val networkExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    @Volatile private var requestRunning = false
    private var chatScreen: ChatScreen? = null
    private var voiceManager: VoiceManager? = null
    private var listeningForSpeech = false

    companion object {
        private const val REQUEST_RECORD_AUDIO = 4107
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = NovaPalette.background
        window.navigationBarColor = NovaPalette.navigationBar
        voiceManager = VoiceManager(this, object : VoiceManager.Listener {
            override fun onListeningReady() {
                listeningForSpeech = true
                chatScreen?.setMicActive(true)
                chatScreen?.setStatus("Listening… speak now")
            }

            override fun onPartialTranscript(text: String) {
                chatScreen?.setInputText(text)
            }

            override fun onTranscript(text: String) {
                listeningForSpeech = false
                chatScreen?.setMicActive(false)
                chatScreen?.setInputText(text)
                chatScreen?.setStatus("Voice input ready — review and tap SEND")
            }

            override fun onVoiceError(message: String) {
                listeningForSpeech = false
                chatScreen?.setMicActive(false)
                chatScreen?.setStatus(message)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        })
        showHome()
    }

    override fun onDestroy() {
        stopRequest(showMessage = false)
        voiceManager?.shutdown()
        voiceManager = null
        networkExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun showHome() {
        stopVoiceInput()
        chatScreen = null
        HomeDashboardScreen(this, ::showApiSettings, ::showChat).show()
    }

    private fun showApiSettings() {
        chatScreen = null
        ApiSettingsScreen(this, apiKeyManager, ::showHome).show()
    }

    private fun showChat() {
        val screen = ChatScreen(this, ::showHome, ::sendMessage, { stopRequest() }, ::onVoiceInputTap, ::readLatestResponse)
        chatScreen = screen
        setContentView(screen.create())
        if (chatManager.size() == 0) screen.addChatBubble("assistant", "Hello! I’m NOVA. Ask me anything to get started.")
        else screen.rebuildChatBubbles(chatManager.history)
        screen.scrollToBottom()
    }

    private fun sendMessage() {
        if (requestRunning) return
        val screen = chatScreen ?: return
        val prompt = screen.input.text?.toString()?.trim().orEmpty()
        if (prompt.isBlank()) return
        val apiKey = apiKeyManager.getApiKey()
        if (apiKey.isNullOrBlank()) {
            Toast.makeText(this, "First save your Gemini API key in API key settings.", Toast.LENGTH_LONG).show()
            return
        }
        screen.input.text.clear()
        chatManager.addUserMessage(prompt)
        screen.addChatBubble("user", prompt)
        geminiClient.resetCancellation()
        setRequestUi(true, "NOVA is thinking…")
        networkExecutor.execute { performGeminiRequest(apiKey) }
    }

    private fun performGeminiRequest(apiKey: String) {
        geminiClient.execute(apiKey, chatManager.history, object : GeminiStreamingClient.Listener {
            private var liveBubble: TextView? = null

            override fun onStarted() {
                if (!isFinishing) {
                    liveBubble = chatScreen?.addStreamingBubble()
                    chatScreen?.setStatus("NOVA is replying…")
                }
            }

            override fun onChunk(text: String) {
                if (!isFinishing) {
                    if (liveBubble == null) liveBubble = chatScreen?.addStreamingBubble()
                    liveBubble?.text = text
                    chatScreen?.scrollToBottom()
                }
            }

            override fun onCompleted(answer: String, firstTokenSeconds: Double, totalSeconds: Double) {
                chatManager.addModelMessage(answer)
                if (!isFinishing) {
                    liveBubble?.text = answer
                    val first = String.format(java.util.Locale.US, "%.1f", firstTokenSeconds)
                    val total = String.format(java.util.Locale.US, "%.1f", totalSeconds)
                    setRequestUi(false, "First text ${first}s • Total ${total}s")
                }
            }

            override fun onFailed(message: String, partialAnswer: String) {
                if (!isFinishing) {
                    if (partialAnswer.isEmpty()) chatScreen?.addChatBubble("assistant", message)
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
        chatScreen?.setRequestUi(running, status)
    }

    private fun stopRequest(showMessage: Boolean = true) {
        if (!requestRunning) return
        geminiClient.cancel()
        requestRunning = false
        setRequestUi(false, "Request stopped")
        if (showMessage && chatScreen != null) chatScreen?.addChatBubble("assistant", "Request stopped.")
    }


    private fun onVoiceInputTap() {
        if (listeningForSpeech) {
            stopVoiceInput()
            chatScreen?.setStatus("Voice input stopped")
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
            return
        }
        voiceManager?.startListening()
    }

    private fun stopVoiceInput() {
        voiceManager?.stopListening()
        listeningForSpeech = false
        chatScreen?.setMicActive(false)
    }

    private fun readLatestResponse() {
        var latest = ""
        for (index in chatManager.size() - 1 downTo 0) {
            val item = chatManager.messageAt(index) ?: continue
            if (item.optString("role") != "model") continue
            latest = item.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty()
            if (latest.isNotBlank()) break
        }
        if (latest.isBlank()) {
            Toast.makeText(this, "NOVA has no response to read yet.", Toast.LENGTH_SHORT).show()
            return
        }
        if (voiceManager?.speak(latest) != true) {
            Toast.makeText(this, "Text-to-speech is unavailable for the current language.", Toast.LENGTH_LONG).show()
        } else {
            chatScreen?.setStatus("Reading the latest response aloud")
        }
    }

    @Deprecated("Use the platform permission result callback")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceManager?.startListening()
            } else {
                chatScreen?.setStatus("Microphone permission was not granted")
                Toast.makeText(this, "Allow microphone permission to use voice input.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
