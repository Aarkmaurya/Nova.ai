# NOVA AI — Voice Foundation (Phase 3)

## What this phase adds
- Explicit tap-to-start speech input using Android SpeechRecognizer.
- Partial transcription is shown in the chat input; the final transcript remains editable.
- The user must tap SEND to submit recognized text to Gemini. Voice recognition never sends a prompt automatically.
- A READ button speaks the latest completed NOVA response using Android TextToSpeech.
- Speech output is user-triggered; no always-listening/background microphone behavior is added.
- Voice components are separated into `voice/VoiceManager.kt`, `voice/SpeechRecognizer.kt`, and `voice/TextToSpeechManager.kt`.

## Apply
1. Back up/commit the working Phase 2 project.
2. Copy `app/src/main/java/com/aark/novaai/` from this ZIP into the same project path, replacing the existing package folder.
3. Open your existing `app/src/main/AndroidManifest.xml`. Add the permission and `<queries>` entries from `AndroidManifest_permissions_snippet.xml` directly under the root `<manifest>` element (not inside `<application>`). Keep your existing application/activity declarations.
4. No new Gradle dependency is required; SpeechRecognizer and TextToSpeech are Android platform APIs. Keep existing Gradle configuration.
5. Run GitHub Actions/build.

## Test on the phone
1. Open NOVA → Voice assistant → chat.
2. Tap MIC and grant microphone permission when Android asks.
3. Speak a short prompt. The recognized text appears in the input field.
4. Review/edit it and tap SEND.
5. After NOVA completes its reply, tap READ to hear the latest completed reply.
6. Tap MIC while listening to stop voice input.

## Notes / limitations
- Recognition availability and language support depend on the Android speech service installed on the device and its network requirements.
- Text-to-speech uses the device's default locale/installed voice. Install the relevant Android TTS voice data in system settings if needed.
- This phase does not add wake-word detection, background listening, incoming-call access, or continuous conversation.
- Microphone access is requested only after the user taps MIC. No audio recording is saved by this app code.
- The source ZIP does not include the full Gradle project/manifest; the manifest snippet must be merged manually.
- No APK build or on-device test has been performed by the patch author.
