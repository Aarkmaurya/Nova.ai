package com.aark.novaai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/** Text-to-speech output with optional completion callback for explicit voice conversations. */
class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var supportedLanguage = false
    private var pendingText: String? = null
    private var pendingDone: (() -> Unit)? = null
    private val ids = AtomicLong(0)
    private val callbacks = HashMap<String, () -> Unit>()

    override fun onInit(status: Int) {
        val tts = engine ?: return
        if (status != TextToSpeech.SUCCESS) { ready = false; supportedLanguage = false; pendingText = null; pendingDone = null; return }
        ready = true
        supportedLanguage = configureLanguage(tts)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onError(utteranceId: String?) { utteranceId?.let { callbacks.remove(it) } }
            override fun onDone(utteranceId: String?) {
                val callback = utteranceId?.let { synchronized(callbacks) { callbacks.remove(it) } }
                callback?.let { android.os.Handler(android.os.Looper.getMainLooper()).post(it) }
            }
        })
        if (!supportedLanguage) { pendingText = null; pendingDone = null; return }
        pendingText?.let { text -> val done = pendingDone; pendingText = null; pendingDone = null; speakNow(text, done) }
    }

    private fun configureLanguage(tts: TextToSpeech): Boolean {
        val candidates = listOf(Locale.getDefault(), Locale("hi", "IN"), Locale.US).distinctBy { it.toLanguageTag() }
        for (locale in candidates) try {
            val result = tts.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED && result >= TextToSpeech.LANG_AVAILABLE) return true
        } catch (_: Exception) { }
        return false
    }

    fun speak(text: String): Boolean = speak(text, null)

    fun speak(text: String, onDone: (() -> Unit)?): Boolean {
        if (text.isBlank()) return false
        if (!ready) { pendingText = text; pendingDone = onDone; return true }
        if (!supportedLanguage) return false
        return speakNow(text, onDone)
    }

    private fun speakNow(text: String, onDone: (() -> Unit)?): Boolean {
        val tts = engine ?: return false
        val prefersHindi = text.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.DEVANAGARI }
        val preferred = if (prefersHindi) listOf(Locale("hi", "IN"), Locale.getDefault(), Locale.US) else listOf(Locale.getDefault(), Locale.US, Locale("hi", "IN"))
        var selected = false
        for (locale in preferred.distinctBy { it.toLanguageTag() }) try {
            val result = tts.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED && result >= TextToSpeech.LANG_AVAILABLE) { selected = true; break }
        } catch (_: Exception) { }
        if (!selected) return false
        val id = "NOVA_TTS_${ids.incrementAndGet()}"
        if (onDone != null) synchronized(callbacks) { callbacks[id] = onDone }
        return try {
            val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
            if (result == TextToSpeech.ERROR) { synchronized(callbacks) { callbacks.remove(id) }; false } else true
        } catch (_: Exception) { synchronized(callbacks) { callbacks.remove(id) }; false }
    }

    fun stop() { pendingText = null; pendingDone = null; synchronized(callbacks) { callbacks.clear() }; try { engine?.stop() } catch (_: Exception) { } }
    fun shutdown() { stop(); ready = false; supportedLanguage = false; try { engine?.shutdown() } catch (_: Exception) { }; engine = null }
}
