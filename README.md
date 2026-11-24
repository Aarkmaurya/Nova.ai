# NOVA AI — Modularization Phase 2

## Apply
Copy the `app/src/main/java/com/aark/novaai/` directory from this ZIP into the same path in your Android project, replacing the Phase 1 files. Keep your existing Gradle configuration and dependencies.

## What changed
- `ui/HomeDashboardScreen.kt`: dashboard UI and navigation callbacks.
- `ui/ApiSettingsScreen.kt`: API key settings UI.
- `ui/ChatScreen.kt`: chat transcript, composer, bubbles, and send/stop controls.
- `ui/components/NovaUi.kt`: shared Android View styling helpers.
- `MainActivity.kt`: reduced to screen navigation, chat coordination, and Gemini request lifecycle.
- Existing `core/`, `ai/`, `chat/`, and `security/` classes are carried forward from Phase 1 unchanged.

## Preserved behavior
Gemini SSE streaming callbacks, first-token/total timing, STOP cancellation, chat history, API-key save/delete, and the current Android Views UI are retained. Voice assistant and screen vision remain placeholders, not implemented features.

## Build note
This is a source patch, not a verified APK. The full Android project and Gradle wrapper were not available in the uploaded source bundle, so run your usual GitHub Actions build after applying it.

## Apply safely
1. Commit or back up your working Phase 1 project.
2. Copy this ZIP's `app/src/main/java/com/aark/novaai/` folder into the matching project path.
3. Run GitHub Actions. If it fails, send the complete build log; do not manually mix Phase 1 and Phase 2 files.
4. Test Home → API Settings → save/delete key → Chat → streaming response → STOP.
