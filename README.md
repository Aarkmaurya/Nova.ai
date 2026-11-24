# NOVA AI — modularization patch (Phase 1)

## Apply

Copy the `app/src/main/java/com/aark/novaai/` directory from this ZIP into the same path in your Android project, replacing the old `MainActivity.kt` and adding the new package files.

No Gradle dependency changes are required. This patch uses the AndroidX Security Crypto dependency already present in the project configuration you supplied.

## What moved out of MainActivity

- `core/NovaPalette.kt`: shared UI colors.
- `core/NovaConfig.kt`: Gemini model endpoint and timeout configuration.
- `core/NovaIdentity.kt`, `core/NovaConstants.kt`: app identity and preference keys.
- `security/ApiKeyManager.kt`: encrypted API-key storage.
- `chat/ChatManager.kt`: in-memory Gemini conversation history.
- `chat/ChatMessage.kt`: chat domain model for the next UI extraction stage.
- `ai/GeminiRequest.kt`: Gemini request-body builder.
- `ai/GeminiStreamingClient.kt`: SSE networking, cancellation, error mapping and streaming callbacks.

`MainActivity.kt` still owns the current Android Views screens and navigation. This is intentionally a behavior-preserving first extraction; the dashboard/chat/settings view code has not yet been moved into separate screen classes.

## Important

- The Gemini model ID and endpoint were preserved from the source file you supplied. If that model ID is unavailable for your API key, the app may return an API/model error; this patch does not silently change it.
- API keys remain encrypted in local app preferences. Do not commit a real key to GitHub.
- Voice assistant and screen vision remain unimplemented, as they were in the supplied source.
- The ZIP is a source patch, not a verified APK. The full Android project/Gradle wrapper was not included with the uploaded `MainActivity.kt`, so an APK build could not be run here.
- Keep your existing `memory1.md` through `memory6.md`; they were not part of the uploaded file and are not replaced by this patch. `memory7.md` records this refactor stage.
