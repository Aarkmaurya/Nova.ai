package com.aark.novaai.chat

import org.json.JSONArray
import org.json.JSONObject

/** Owns the in-memory Gemini conversation history for this app session. */
class ChatManager {
    val history = JSONArray()

    fun addUserMessage(text: String) {
        history.put(message("user", text))
    }

    fun addModelMessage(text: String) {
        history.put(message("model", text))
    }

    fun messageAt(index: Int): JSONObject? = history.optJSONObject(index)

    fun size(): Int = history.length()

    private fun message(role: String, text: String): JSONObject =
        JSONObject().put("role", role)
            .put("parts", JSONArray().put(JSONObject().put("text", text)))
}
