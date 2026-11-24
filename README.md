# NOVA AI — Gemini Chat patch

This is a **patch** for the existing NOVA AI Android Studio repository, not a complete standalone Android Studio project.

## Replace these files

1. `app/src/main/java/com/aark/novaai/MainActivity.kt`
2. `app/build.gradle.kts`
3. `app/src/main/AndroidManifest.xml`

The supplied `AndroidManifest.xml` is at the ZIP root to make it easy to identify; copy it to `app/src/main/AndroidManifest.xml` in the repository.

Keep your existing root `settings.gradle.kts`, root `build.gradle.kts`, `gradle.properties`, and GitHub Actions workflow.

## Added in this patch

- Gemini text chat screen with user and assistant message bubbles.
- Multi-turn conversation context kept in memory while the app process is alive.
- Background network request (does not run on the UI thread).
- Send and Stop controls, loading/status text, and basic HTTP/network error messages.
- Uses the API key already saved in the app's encrypted local preferences; the key is sent in the `x-goog-api-key` request header and is not displayed in chat.
- Adds Android `INTERNET` permission.
- Uses Gemini REST `generateContent` with model name `gemini-3.5-flash`.

## Important limitations and security

- Chat history is currently memory-only. It is cleared when the app process is ended; it is not saved to disk.
- The API key remains a client-side key. Encrypted local storage protects it at rest, but an app distributed to others cannot keep a shared API key fully secret. Do not commit a real key to GitHub or share it.
- The app sends the text you type to Google's Gemini API. Do not enter passwords, OTPs, or other sensitive personal information.
- Stop disconnects the active HTTP request; the server may already have received the prompt before cancellation.
- Voice, wake word, screen vision, accessibility automation, and phone actions are not implemented by this patch.
- The model name may depend on model availability for the API project/key. The app displays an API error if Google rejects the model or request.

## Build and test

1. Replace the three app files listed above.
2. Commit and push to your existing GitHub repository.
3. Run the existing GitHub Actions workflow.
4. Install the resulting debug APK, save your own Gemini API key in NOVA's API Key Settings, then open Gemini Chat and send a simple test message.
5. If Actions fails, share the complete build log. If chat fails, share the visible error text, but never share the API key.

No Gradle build, APK build, installation, or live API request has been run in this environment.
