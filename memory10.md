# NOVA AI — Memory 10
## Phase 3.1 Voice and Chat UI patch (2026-11-26)

### Changes in this source patch
- `ui/ChatScreen.kt`
  - Retains the existing MIC and READ controls.
  - Adds an AUTO ON/OFF control. Its initial state is supplied by `MainActivity`; the default preference is OFF.
  - Adds a per-assistant-message SPEAK control, including for restored chat history.
  - Adds a speaker control to completed streaming responses through `attachSpeakerToBubble`.
- `MainActivity.kt`
  - Persists the Auto Speak preference in `nova_voice_settings`.
  - Speaks completed Gemini responses only when Auto Speak is enabled.
  - Routes READ and per-message SPEAK through a shared `speakText` method.
  - Applies system-bar insets to the chat root so the composer has bottom safe-area padding.
  - Preserves the existing Gemini request, streaming chunk, cancellation, chat history, microphone permission, and API-key paths.
- `voice/TextToSpeechManager.kt`
  - Handles TTS initialization and language failures defensively.
  - Tries the device locale, Hindi (India), and US English.
  - For Devanagari text, prefers Hindi (India) when available; otherwise it tries supported fallbacks.

### Important verification boundary
- Source-level edits and ZIP integrity are checked.
- The uploaded project does not include a Gradle wrapper, and this environment does not have Gradle or the Android SDK installed. Therefore this patch has NOT been Android-built or runtime-tested.
- Build using the existing GitHub Actions workflow (`gradle assembleDebug`). Then test MIC permission/input, READ, each message's SPEAK control, AUTO OFF/ON, and the chat composer around the Android navigation bar.
- APK compilation alone does not verify microphone or TTS runtime behavior.
