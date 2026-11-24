package com.aark.novaai.voice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/** One-shot Android speech-to-text input. Requires RECORD_AUDIO runtime permission. */
class NovaSpeechRecognizer(
    private val activity: Activity,
    private val listener: Listener
) {
    interface Listener {
        fun onReady()
        fun onPartialResult(text: String)
        fun onResult(text: String)
        fun onError(message: String)
    }

    private var recognizer: SpeechRecognizer? = null

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            listener.onError("Speech recognition is not available on this device.")
            return
        }
        stopAndRelease()
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
        recognizer = speechRecognizer
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = listener.onReady()
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                if (text.isNotBlank()) listener.onPartialResult(text)
            }

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                if (text.isBlank()) listener.onError("No speech was recognized. Please try again.")
                else listener.onResult(text)
                stopAndRelease()
            }

            override fun onError(error: Int) {
                listener.onError(errorMessage(error))
                stopAndRelease()
            }
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to NOVA")
        }
        try {
            speechRecognizer.startListening(intent)
        } catch (error: Exception) {
            stopAndRelease()
            listener.onError(error.localizedMessage ?: "Could not start speech recognition.")
        }
    }

    fun stop() {
        try { recognizer?.stopListening() } catch (_: Exception) { }
        stopAndRelease()
    }

    fun destroy() = stopAndRelease()

    private fun stopAndRelease() {
        val current = recognizer ?: return
        recognizer = null
        try { current.cancel() } catch (_: Exception) { }
        try { current.destroy() } catch (_: Exception) { }
    }

    private fun errorMessage(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> "Microphone audio error. Please try again."
        SpeechRecognizer.ERROR_CLIENT -> "Speech recognition was interrupted."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network issue. Check your connection."
        SpeechRecognizer.ERROR_NO_MATCH -> "I could not understand that. Please try again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition is busy. Try again in a moment."
        SpeechRecognizer.ERROR_SERVER -> "Speech recognition service error."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap MIC and try again."
        else -> "Speech recognition failed (code $code)."
    }
}
