# NOVA AI — Memory 3 (UI Refresh)

Date: 24 November 2026

## User feedback
- The user installed/opened the generated APK and shared a screenshot of the home screen.
- The user reported that button text was unclear/clipped and did not like the basic/simple appearance.
- Requested a more attractive interface combining Bento UI, Neumorphism, Claymorphism, and Glassmorphism influences.
- User explicitly clarified that they want a real Android UI/code update, not a generated concept image.

## Repository inspection
- Public repository: https://github.com/Aarkmaurya/Nova.ai
- Inspected `app/build.gradle.kts` and `app/src/main/java/com/aark/novaai/MainActivity.kt`.
- Existing screen used native Android `Button` widgets with `setBackgroundColor`, fixed 58dp heights, and default button styling. This plausibly explains the poor label appearance in the screenshot; exact device font/rendering behavior was not independently tested.
- Existing API-key settings used AndroidX Security Crypto encrypted preferences and a Gemini key field. This behavior is retained in the replacement activity.
- The repository README still identifies Gemini chat, voice, screen vision, and accessibility/phone actions as not implemented.

## UI changes in this patch
- Replaced default Android `Button` widgets with custom clickable `TextView`-based tiles to avoid default all-caps transformation and button font padding/clipping.
- Added a scrollable responsive layout, Bento-style workspace cards, layered gradients, rounded surfaces, soft elevation, cyan/purple accents, and clearer labels.
- Retained API-key save/delete actions and the existing encrypted preferences approach.
- Added clear status copy that voice and screen-vision features are not yet implemented.

## Files in this patch ZIP
- `app/src/main/java/com/aark/novaai/MainActivity.kt`
- `app/build.gradle.kts`
- `memory1.md`, `memory2.md`, `memory3.md`
- `README_UI_REFRESH.md`

## Verification status
- The UI code was authored from the currently visible public repository source.
- No Android Gradle build was run in this environment.
- No APK was produced, installed, or visually tested from this patch.
- Existing repository files such as the manifest, root Gradle files, settings, and workflow are not bundled; this is a patch ZIP intended to be merged into the existing repository, not a full standalone Android Studio project.

## Next step
- Replace the two app files in the existing GitHub repository with the matching files from this ZIP, commit, run Actions, then install and inspect the resulting APK. If build errors occur, share the complete error log before further changes.
