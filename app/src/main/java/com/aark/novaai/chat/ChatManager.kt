package com.aark.novaai.chat

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Stores multiple NOVA conversations locally. The active conversation remains
 * compatible with Gemini's JSONArray request format.
 */
class ChatManager(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val sessions = mutableListOf<JSONObject>()
    private var activeSessionId: String = ""

    init {
        loadSessions()
        if (sessions.isEmpty()) sessions.add(createSession())
        val savedActiveId = preferences.getString(KEY_ACTIVE_SESSION, null)
        activeSessionId = sessions.firstOrNull { it.optString("id") == savedActiveId }
            ?.optString("id") ?: sessions.last().optString("id")
        persist()
    }

    /** Current conversation in the exact format expected by GeminiStreamingClient. */
    val history: JSONArray
        get() {
            val session = activeSession()
            var messages = session.optJSONArray("history")
            if (messages == null) {
                messages = JSONArray()
                session.put("history", messages)
            }
            return messages
        }

    fun addUserMessage(text: String) {
        history.put(message("user", text))
        updateSessionAfterMessage(text)
    }

    fun addModelMessage(text: String) {
        history.put(message("model", text))
        updateSessionAfterMessage(null)
    }

    /** Starts a fresh conversation while retaining every previous conversation. */
    fun createNewChat(): Boolean {
        if (history.length() == 0) return false
        val newSession = createSession()
        sessions.add(newSession)
        activeSessionId = newSession.optString("id")
        persist()
        return true
    }

    fun messageAt(index: Int): JSONObject? = history.optJSONObject(index)

    fun size(): Int = history.length()

    private fun updateSessionAfterMessage(firstUserText: String?) {
        val session = activeSession()
        if (!firstUserText.isNullOrBlank() && session.optString("title") == DEFAULT_TITLE) {
            session.put("title", firstUserText.trim().replace(Regex("\\s+"), " ").take(TITLE_MAX_LENGTH))
        }
        session.put("updatedAt", System.currentTimeMillis())
        persist()
    }

    private fun activeSession(): JSONObject {
        val existing = sessions.firstOrNull { it.optString("id") == activeSessionId }
        if (existing != null) return existing
        val created = createSession()
        sessions.add(created)
        activeSessionId = created.optString("id")
        return created
    }

    private fun createSession(): JSONObject = JSONObject()
        .put("id", UUID.randomUUID().toString())
        .put("title", DEFAULT_TITLE)
        .put("createdAt", System.currentTimeMillis())
        .put("updatedAt", System.currentTimeMillis())
        .put("history", JSONArray())

    private fun loadSessions() {
        val saved = preferences.getString(KEY_SESSIONS, null) ?: return
        try {
            val stored = JSONObject(saved).optJSONArray("sessions") ?: return
            for (index in 0 until stored.length()) {
                val session = stored.optJSONObject(index) ?: continue
                if (session.optString("id").isNotBlank()) sessions.add(session)
            }
        } catch (_: Exception) {
            // If stored data is damaged, start a clean session rather than crash the app.
            sessions.clear()
        }
    }

    private fun persist() {
        val stored = JSONArray()
        sessions.forEach { stored.put(it) }
        val root = JSONObject().put("sessions", stored)
        preferences.edit()
            .putString(KEY_SESSIONS, root.toString())
            .putString(KEY_ACTIVE_SESSION, activeSessionId)
            .apply()
    }

    private fun message(role: String, text: String): JSONObject = JSONObject()
        .put("role", role)
        .put("parts", JSONArray().put(JSONObject().put("text", text)))

    companion object {
        private const val PREFERENCES_NAME = "nova_chat_sessions"
        private const val KEY_SESSIONS = "sessions_json"
        private const val KEY_ACTIVE_SESSION = "active_session_id"
        private const val DEFAULT_TITLE = "New Chat"
        private const val TITLE_MAX_LENGTH = 36
    }
}
