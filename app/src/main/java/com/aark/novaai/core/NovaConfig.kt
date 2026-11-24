package com.aark.novaai.core

object NovaConfig {
    const val GEMINI_MODEL = "gemini-3.5-flash"
    const val GEMINI_STREAM_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:streamGenerateContent?alt=sse"
    const val CONNECT_TIMEOUT_MS = 20_000
    const val READ_TIMEOUT_MS = 60_000
}
