# NOVA AI — Memory 9 (2026-11-24)

## Modularization Phase 3: Voice foundation
- User reported that the Phase 2 APK built successfully. This is user-reported, not independently verified.
- Phase 3 adds a tap-to-start Android SpeechRecognizer path and Android TextToSpeech output.
- New classes: `voice/VoiceManager.kt`, `voice/SpeechRecognizer.kt` (class `NovaSpeechRecognizer`), and `voice/TextToSpeechManager.kt`.
- Chat screen gains MIC and READ controls. Partial speech recognition fills the input field; the final transcript remains editable and the user must tap SEND to submit it to Gemini.
- READ speaks the latest completed model response only after the user taps it.
- Microphone runtime permission is requested on first MIC tap. Manifest requires `RECORD_AUDIO` and an Android 11+ `<queries>` declaration for RecognitionService; a snippet is included. No new Gradle dependency is needed.
- No always-on microphone, wake word, background listening, call access, or audio persistence is implemented.
- Phase 3 is a source patch only and has not been built or tested on device by the assistant.
- Preserve memory1.md through memory8.md in the user's project; this patch carries memory7.md and memory8.md forward and adds memory9.md.
