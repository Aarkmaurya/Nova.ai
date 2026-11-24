# NOVA AI — Memory 2 (Build Fixes)

- The user reported the first GitHub Actions build failed because Gradle could not find `assembleDebug`; the Android module Gradle file was missing from the repository at that point.
- Added/created `app/build.gradle.kts` with the Android application and Kotlin Android plugins, namespace/application ID `com.aark.novaai`, compile SDK 35, min SDK 26, target SDK 35, and AndroidX dependencies.
- The next build reached Kotlin compilation but failed because Java compilation targeted JVM 1.8 while Kotlin targeted JVM 17.
- Updated the app Gradle configuration to set Java source/target compatibility and Kotlin JVM target to 17.
- The user subsequently reported that the APK built successfully. This is user-reported; the assistant did not independently download, install, or test that APK.
