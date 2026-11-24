package com.aark.novaai.ai

import android.os.Handler
import android.os.Looper
import com.aark.novaai.core.NovaConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/** Performs Gemini SSE requests. UI updates are delivered on the main thread. */
class GeminiStreamingClient {
    interface Listener {
        fun onStarted()
        fun onChunk(text: String)
        fun onCompleted(answer: String, firstTokenSeconds: Double, totalSeconds: Double)
        fun onFailed(message: String, partialAnswer: String)
        fun onCancelled()
    }

    @Volatile private var activeConnection: HttpURLConnection? = null
    @Volatile private var cancelled = false
    private val mainHandler = Handler(Looper.getMainLooper())

    fun resetCancellation() { cancelled = false }

    fun cancel() {
        cancelled = true
        activeConnection?.disconnect()
    }

    fun execute(apiKey: String, history: JSONArray, listener: Listener) {
        var connection: HttpURLConnection? = null
        val startedAt = System.currentTimeMillis()
        val fullAnswer = StringBuilder()
        var firstTokenMillis: Long? = null
        try {
            if (cancelled) {
                post { listener.onCancelled() }
                return
            }
            val url = URL(NovaConfig.GEMINI_STREAM_ENDPOINT)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = NovaConfig.CONNECT_TIMEOUT_MS
                readTimeout = NovaConfig.READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "text/event-stream")
                setRequestProperty("x-goog-api-key", apiKey)
            }
            activeConnection = connection
            val body = GeminiRequest(history).toJson()
            connection.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
            val code = connection.responseCode
            if (code !in 200..299) {
                val errorText = connection.errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
                val detail = try { JSONObject(errorText).optJSONObject("error")?.optString("message") } catch (_: Exception) { null }
                throw GeminiHttpException(code, detail?.takeIf { it.isNotBlank() } ?: "The API request was rejected.")
            }
            post { if (!cancelled) listener.onStarted() }
            connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                while (!cancelled) {
                    val line = reader.readLine() ?: break
                    if (!line.startsWith("data:")) continue
                    val data = line.removePrefix("data:").trim()
                    if (data.isEmpty() || data == "[DONE]") continue
                    val event = try { JSONObject(data) } catch (_: Exception) { continue }
                    val candidates = event.optJSONArray("candidates") ?: continue
                    val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: continue
                    val parts = content.optJSONArray("parts") ?: continue
                    val chunk = buildString {
                        for (i in 0 until parts.length()) {
                            val part = parts.optJSONObject(i) ?: continue
                            val piece = part.optString("text")
                            if (piece.isNotEmpty()) append(piece)
                        }
                    }
                    if (chunk.isNotEmpty()) {
                        if (firstTokenMillis == null) firstTokenMillis = System.currentTimeMillis() - startedAt
                        fullAnswer.append(chunk)
                        val snapshot = fullAnswer.toString()
                        post { if (!cancelled) listener.onChunk(snapshot) }
                    }
                }
            }
            if (cancelled) {
                post { listener.onCancelled() }
                return
            }
            val answer = fullAnswer.toString().trim()
            if (answer.isBlank()) throw IllegalStateException("Gemini returned no text. Try rephrasing your message.")
            val elapsed = (System.currentTimeMillis() - startedAt) / 1000.0
            val firstToken = (firstTokenMillis ?: 0L) / 1000.0
            post { if (!cancelled) listener.onCompleted(answer, firstToken, elapsed) }
        } catch (e: Exception) {
            if (cancelled) {
                post { listener.onCancelled() }
                return
            }
            val message = when (e) {
                is GeminiHttpException -> when (e.code) {
                    400 -> "Request error (400): ${e.message}"
                    401, 403 -> "API key rejected or permission missing (${e.code}). Check your key and API access."
                    404 -> "Streaming model or endpoint not found (404). Check model availability."
                    429 -> "Rate limit or quota reached (429). Check your Gemini usage limits."
                    else -> "Gemini API error (${e.code}): ${e.message}"
                }
                else -> if (Thread.currentThread().isInterrupted) "Request stopped." else
                    "Connection error: ${e.localizedMessage ?: "Please check your internet connection and try again."}"
            }
            val partial = fullAnswer.toString()
            post { if (!cancelled) listener.onFailed(message, partial) }
        } finally {
            activeConnection = null
            connection?.disconnect()
        }
    }

    private fun post(action: () -> Unit) = mainHandler.post { action() }

    private class GeminiHttpException(val code: Int, override val message: String) : Exception(message)
}
