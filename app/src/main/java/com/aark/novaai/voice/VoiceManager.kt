package com.aark.novaai.voice

import android.app.Activity

/** Coordinates explicit speech input and user-triggered spoken responses. */
class VoiceManager(activity: Activity, private val listener: Listener) {
    interface Listener {
        fun onListeningReady()
        fun onPartialTranscript(text: String)
        fun onTranscript(text: String)
        fun onVoiceError(message: String)
    }

    private val speechRecognizer = NovaSpeechRecognizer(activity, object : NovaSpeechRecognizer.Listener {
        override fun onReady() = listener.onListeningReady()
        override fun onPartialResult(text: String) = listener.onPartialTranscript(text)
        override fun onResult(text: String) = listener.onTranscript(text)
        override fun onError(message: String) = listener.onVoiceError(message)
    })
    private val textToSpeech = TextToSpeechManager(activity)

    fun startListening() = speechRecognizer.start()
    fun stopListening() = speechRecognizer.stop()
    fun speak(text: String): Boolean = textToSpeech.speak(text)
    fun speak(text: String, onDone: (() -> Unit)?): Boolean = textToSpeech.speak(text, onDone)
    fun stopSpeaking() = textToSpeech.stop()
    fun shutdown() {
        speechRecognizer.destroy()
        textToSpeech.shutdown()
    }
}
