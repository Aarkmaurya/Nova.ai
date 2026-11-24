# NOVA AI — Memory 1
_Last updated: 24 November 2026_

## 1. Project identity
- Project: **NOVA AI**
- Platform: Android
- Language: Kotlin
- Application ID: `com.aark.novaai`
- Starter version: `0.1.0`
- Minimum Android API: 26
- Target SDK: 35
- AI provider planned: Google Gemini
- Preferred workflow: deliver project updates as ZIP archives and maintain numbered memory files (`memory1.md`, `memory2.md`, etc.) so progress can be resumed.

## 2. User's project goal
Build a personal Android AI assistant supporting voice/text interaction, Gemini-powered responses, and user-approved phone assistance. The user prefers practical, complete project ZIPs and simple Hindi/Hinglish instructions.

## 3. Work completed in this session
Created a starter project archive named `NOVA_AI_Android_Starter.zip`.

Included project files:
- `settings.gradle.kts`
- Root `build.gradle.kts`
- `gradle.properties`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/aark/novaai/MainActivity.kt`
- `.github/workflows/android.yml`
- `README.md`

Implemented in starter:
- Basic native Android activity and a dark NOVA-style home screen.
- NOVA AI title and simple central star/orb-like visual.
- API Key Settings screen.
- Gemini API key entry field.
- Save and delete controls for the key.
- AndroidX Security Crypto encrypted preferences using a MasterKey (AES-256-GCM) and encrypted preferences.
- GitHub Actions workflow intended to build a debug APK and upload it as an artifact.
- README describing current scope, build command, and security caveat.

## 4. Important implementation notes
- MainActivity uses Android `Activity` (not AppCompatActivity).
- API key is stored locally through AndroidX EncryptedSharedPreferences.
- Encrypted storage improves protection at rest but does not make a client-side API key completely secret.
- Never commit a real Gemini API key to GitHub.
- A secured backend is preferable for a public production release.
- The starter ZIP was generated, but **a successful Gradle build, APK installation, and runtime test have not been verified**.
- The GitHub Actions workflow is present but has not been run or verified.
- The project currently has no Gradle wrapper; the workflow installs Gradle 8.9 using `gradle/actions/setup-gradle@v4`.

## 5. Not implemented yet
- Actual Gemini API requests/chat.
- Speech-to-text.
- Text-to-speech.
- Wake word / always-listening.
- Speaker verification / voice authentication.
- Screen Vision / MediaProjection.
- Accessibility Service.
- Phone/app navigation actions.
- Calling and messaging workflows.
- Floating assistant control.
- Incoming-call assistant.

Treat all these as pending until source code and device tests prove otherwise.

## 6. Next recommended development steps
1. User downloads and uploads the starter ZIP contents to a GitHub repository named `NOVA-AI` (or opens it in Android Studio).
2. Run the GitHub Actions workflow and inspect build results; fix any build errors before adding features.
3. Verify the app installs and opens on the user's Android phone.
4. Add actual Gemini API integration, reading the key from secure local settings.
5. Add speech-to-text and text-to-speech with clear microphone permission and activity indicators.
6. Continue with optional wake-word, Screen Vision, and user-enabled Accessibility features, each with transparent consent and controls.

## 7. Project delivery convention from now on
- Deliver each meaningful project update as a ZIP archive.
- Maintain sequential handover notes: `memory1.md`, `memory2.md`, `memory3.md`, and so on.
- Each new memory file should record verified work, changed files, pending items, build/test status, and the next step.
- Include the latest memory file inside each future project ZIP so the project and its progress notes travel together.
- Do not claim build success unless an actual build has succeeded.

## 8. Current checkpoint
**Checkpoint: Starter project created; awaiting user-side GitHub upload or Android Studio build.**

The next conversation should resume from this checkpoint, not restart the project from scratch.
