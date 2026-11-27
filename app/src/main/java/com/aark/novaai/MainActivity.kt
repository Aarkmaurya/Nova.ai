package com.aark.novaai

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
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

/** Activity coordinator: navigation, Gemini requests and explicit voice conversation mode. */
class MainActivity : Activity() {
    private val apiKeyManager by lazy { ApiKeyManager(this) }
    private val chatManager by lazy { ChatManager(applicationContext) }
    private val geminiClient = GeminiStreamingClient()
    private val networkExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    @Volatile private var requestRunning = false
    private var chatScreen: ChatScreen? = null
    private var voiceManager: VoiceManager? = null
    private var listeningForSpeech = false
    private var voiceConversationEnabled = false
    private var currentScreen = SCREEN_HOME
    private val voicePreferences by lazy { getSharedPreferences("nova_voice_settings", MODE_PRIVATE) }

    companion object {
        private const val REQUEST_RECORD_AUDIO = 4107
        private const val SCREEN_HOME = "home"
        private const val SCREEN_SETTINGS = "settings"
        private const val SCREEN_CHAT = "chat"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = NovaPalette.background
        window.navigationBarColor = NovaPalette.navigationBar
        voiceManager = VoiceManager(this, object : VoiceManager.Listener {
            override fun onListeningReady() {
                listeningForSpeech = true
                chatScreen?.setMicActive(true)
                chatScreen?.setVoiceModeState(true, "Listening…")
            }

            override fun onPartialTranscript(text: String) {
                chatScreen?.setInputText(text)
            }

            override fun onTranscript(text: String) {
                listeningForSpeech = false
                chatScreen?.setMicActive(false)
                chatScreen?.setInputText(text)
                if (voiceConversationEnabled) sendMessage()
                else chatScreen?.setStatus("Voice input ready — tap Send")
            }

            override fun onVoiceError(message: String) {
                listeningForSpeech = false
                chatScreen?.setMicActive(false)
                if (voiceConversationEnabled) {
                    voiceConversationEnabled = false
                    chatScreen?.setVoiceModeState(false, message)
                } else chatScreen?.setStatus(message)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        })
        showHome()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        when (currentScreen) {
            SCREEN_CHAT, SCREEN_SETTINGS -> showHome()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        voiceConversationEnabled = false
        stopVoiceInput()
        stopRequest(showMessage = false)
        voiceManager?.shutdown()
        voiceManager = null
        networkExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun showHome() {
        stopVoiceConversation()
        chatScreen = null
        currentScreen = SCREEN_HOME
        HomeDashboardScreen(this, ::showApiSettings, ::showChat).show()
    }

    private fun showApiSettings() {
        stopVoiceConversation()
        chatScreen = null
        currentScreen = SCREEN_SETTINGS
        ApiSettingsScreen(this, apiKeyManager, voicePreferences, ::showHome).show()
    }

    private fun showChat() {
        currentScreen = SCREEN_CHAT
        val screen = ChatScreen(
            this, ::showHome, ::startNewChat, ::sendMessage, { stopRequest() },
            ::onVoiceInputTap, ::toggleVoiceConversation, ::speakText, ::regenerateLatest
        )
        chatScreen = screen
        val chatRoot = screen.create()
        val baseLeft = com.aark.novaai.ui.components.NovaUi.dp(this, 14)
        val baseTop = com.aark.novaai.ui.components.NovaUi.dp(this, 8)
        val baseRight = com.aark.novaai.ui.components.NovaUi.dp(this, 14)
        val baseBottom = com.aark.novaai.ui.components.NovaUi.dp(this, 8)
        chatRoot.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                baseLeft + insets.systemWindowInsetLeft,
                baseTop + insets.systemWindowInsetTop,
                baseRight + insets.systemWindowInsetRight,
                baseBottom + insets.systemWindowInsetBottom
            )
            insets
        }
        setContentView(chatRoot)
        chatRoot.requestApplyInsets()
        if (chatManager.size() == 0) screen.showWelcome()
        else screen.rebuildChatBubbles(chatManager.history)
        screen.scrollToBottom()
    }

    private fun startNewChat() {
        if (requestRunning) {
            Toast.makeText(this, "Wait until NOVA finishes the current response.", Toast.LENGTH_SHORT).show()
            return
        }
        if (chatManager.createNewChat()) chatScreen?.showNewChatWelcome()
        else chatScreen?.setStatus("This chat is already empty")
    }

    private fun sendMessage() {
        if (requestRunning) return
        val screen = chatScreen ?: return
        val prompt = screen.input.text?.toString()?.trim().orEmpty()
        if (prompt.isBlank()) return
        val apiKey = apiKeyManager.getApiKey()
        if (apiKey.isNullOrBlank()) {
            Toast.makeText(this, "First save your Gemini API key in Settings.", Toast.LENGTH_LONG).show()
            if (voiceConversationEnabled) stopVoiceConversation()
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
            private var liveResponse: android.widget.TextView? = null

            override fun onStarted() {
                if (!isFinishing) liveResponse = chatScreen?.addStreamingBubble()
            }

            override fun onChunk(text: String) {
                if (!isFinishing) {
                    if (liveResponse == null) liveResponse = chatScreen?.addStreamingBubble()
                    liveResponse?.text = text
                    chatScreen?.scrollToBottom()
                }
            }

            override fun onCompleted(answer: String, firstTokenSeconds: Double, totalSeconds: Double) {
                chatManager.addModelMessage(answer)
                if (!isFinishing) {
                    liveResponse?.text = answer
                    liveResponse?.let { chatScreen?.attachResponseActions(it, answer) }
                    setRequestUi(false, "")
                    if (voiceConversationEnabled) speakText(answer) {
                        if (voiceConversationEnabled && !isFinishing) startVoiceListening()
                    } else if (voicePreferences.getBoolean("auto_speak_enabled", false)) {
                        speakText(answer)
                    }
                }
            }

            override fun onFailed(message: String, partialAnswer: String) {
                if (!isFinishing) {
                    if (partialAnswer.isEmpty()) chatScreen?.addChatBubble("assistant", message)
                    else {
                        val visibleText = "$partialAnswer\n\n[Response interrupted: $message]"
                        liveResponse?.text = visibleText
                        liveResponse?.let { chatScreen?.attachResponseActions(it, visibleText) }
                    }
                    setRequestUi(false, "Something needs attention")
                    if (voiceConversationEnabled) startVoiceListening()
                }
            }

            override fun onCancelled() {
                if (!isFinishing) setRequestUi(false, "Response stopped")
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
        setRequestUi(false, "Response stopped")
        if (showMessage && chatScreen != null) chatScreen?.addChatBubble("assistant", "Response stopped.")
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

    private fun toggleVoiceConversation() {
        if (voiceConversationEnabled) {
            stopVoiceConversation()
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
            return
        }
        voiceConversationEnabled = true
        chatScreen?.setVoiceModeState(true, "Starting voice conversation…")
        startVoiceListening()
    }

    private fun startVoiceListening() {
        if (!voiceConversationEnabled || requestRunning || isFinishing) return
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
            return
        }
        voiceManager?.startListening()
    }

    private fun stopVoiceConversation() {
        voiceConversationEnabled = false
        stopVoiceInput()
        voiceManager?.stopSpeaking()
        chatScreen?.setVoiceModeState(false, "Voice conversation off")
    }

    private fun stopVoiceInput() {
        voiceManager?.stopListening()
        listeningForSpeech = false
        chatScreen?.setMicActive(false)
    }

    private fun speakText(text: String, onDone: (() -> Unit)? = null) {
        if (voiceManager?.speak(text, onDone) != true) {
            if (voiceConversationEnabled) stopVoiceConversation()
            Toast.makeText(this, "Text-to-speech is unavailable. Check installed voice/language data.", Toast.LENGTH_LONG).show()
        } else if (!voiceConversationEnabled) {
            chatScreen?.setStatus("Speaking response aloud")
        }
    }

    private fun regenerateLatest() {
        if (requestRunning) return
        val history = chatManager.history
        if (history.length() < 2) return
        val last = history.optJSONObject(history.length() - 1)
        if (last?.optString("role") != "model") {
            Toast.makeText(this, "There is no completed response to regenerate.", Toast.LENGTH_SHORT).show()
            return
        }
        val apiKey = apiKeyManager.getApiKey() ?: return
        if (!chatManager.removeLastModelMessage()) return
        chatScreen?.rebuildChatBubbles(chatManager.history)
        geminiClient.resetCancellation()
        setRequestUi(true, "NOVA is thinking…")
        networkExecutor.execute { performGeminiRequest(apiKey) }
    }

    @Deprecated("Use the platform permission result callback")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (currentScreen == SCREEN_CHAT) {
                    if (voiceConversationEnabled) startVoiceListening() else voiceManager?.startListening()
                }
            } else {
                voiceConversationEnabled = false
                chatScreen?.setVoiceModeState(false, "Microphone permission was not granted")
                Toast.makeText(this, "Allow microphone permission to use voice features.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
