# NOVA AI — memory7

## Modularization update

Date: 2026-11-24

The user uploaded the complete 557-line `MainActivity.kt`. A first behavior-preserving modularization patch was prepared from that exact source.

### Extracted files
- `core/NovaPalette.kt` — shared NOVA colors.
- `core/NovaConfig.kt` — Gemini endpoint and timeout constants.
- `core/NovaIdentity.kt` — app identity strings.
- `core/NovaConstants.kt` — secure preference identifiers.
- `security/ApiKeyManager.kt` — encrypted local Gemini API-key storage.
- `chat/ChatManager.kt` — in-memory Gemini conversation history.
- `chat/ChatMessage.kt` — chat domain model for later UI work.
- `ai/GeminiRequest.kt` — Gemini JSON request body.
- `ai/GeminiStreamingClient.kt` — SSE request, incremental text callbacks, cancellation and API error mapping.

### MainActivity status
`MainActivity.kt` now delegates API-key storage, chat history, palette/configuration, and Gemini streaming to separate classes. The Android Views for dashboard, settings and chat remain in MainActivity for now; moving those screens into independent UI classes is the next refactor stage. No Compose migration was made.

### Behavior intended to remain unchanged
- Dark NOVA dashboard and existing action tiles.
- Gemini API key save/delete through encrypted preferences.
- Existing Gemini SSE endpoint/model string and request timeouts.
- Incremental streaming bubble and first-token/total timing display.
- Stop/cancel control and existing error messages.
- In-memory chat history during the app session.

### Verification status
This is a source-level patch only. The uploaded source did not include the full Android project or Gradle wrapper, so no APK build or device runtime test was performed. The user must apply the patch and run the project's GitHub Actions/Android build. Preserve existing `memory1.md`–`memory6.md`; they were not supplied with this upload.
