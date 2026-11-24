# NOVA AI — Android Starter

A minimal Kotlin Android starter for the NOVA AI project.

## Included in this first stage
- Native Android app shell with a dark NOVA-style home screen.
- Gemini API key settings screen.
- Encrypted local preference storage using Android Keystore-backed AndroidX Security Crypto.
- GitHub Actions workflow to build and upload a debug APK.

## Not implemented yet
- Gemini network requests / chat.
- Speech-to-text and text-to-speech.
- Wake word / always-listening.
- Speaker verification.
- Screen Vision / MediaProjection.
- Accessibility Service and phone actions.

## Build
Requires JDK 17 and Gradle 8.9:
```bash
gradle assembleDebug
```
APK output:
`app/build/outputs/apk/debug/app-debug.apk`

Or push the project to GitHub and run **Actions → Build NOVA AI APK**.

## Important security note
Encrypted local storage protects the saved value at rest, but any API key embedded in a client app can potentially be extracted or abused. Never commit a real key to GitHub. A secured backend is preferable for a public release.

## Project identity
Application ID: `com.aark.novaai`
Version: `0.1.0`
Min Android API: 26
Target SDK: 35
