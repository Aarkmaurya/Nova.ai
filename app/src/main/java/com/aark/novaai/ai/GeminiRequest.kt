package com.aark.novaai.ai

import org.json.JSONArray
import org.json.JSONObject

/** Builds the request body expected by Gemini generateContent/streamGenerateContent. */
data class GeminiRequest(val contents: JSONArray) {
    fun toJson(): JSONObject = JSONObject().put("contents", contents)
}
