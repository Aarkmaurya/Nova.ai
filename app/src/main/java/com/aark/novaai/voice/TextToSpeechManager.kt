package com.aark.novaai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Text-to-speech output. Speech starts only after the user taps READ. */
class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var pendingText: String? = null

    override fun onInit(status: Int) {
        val tts = engine ?: return
        ready = status == TextToSpeech.SUCCESS
        if (!ready) {
            pendingText = null
            return
        }
        val languageResult = tts.setLanguage(Locale.getDefault())
        if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
            languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            ready = false
            pendingText = null
            return
        }
        pendingText?.let { text ->
            pendingText = null
            speakNow(text)
        }
    }

    /** Returns false only when the text is empty or the selected language is unsupported. */
    fun speak(text: String): Boolean {
        if (text.isBlank()) return false
        if (!ready) {
            pendingText = text
            return true
        }
        return speakNow(text)
    }

    private fun speakNow(text: String): Boolean {
        val tts = engine ?: return false
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NOVA_TTS")
        return result != TextToSpeech.ERROR
    }

    fun stop() {
        pendingText = null
        engine?.stop()
    }

    fun shutdown() {
        pendingText = null
        ready = false
        engine?.stop()
        engine?.shutdown()
        engine = null
    }
}
