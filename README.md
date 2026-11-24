# NOVA AI 0.3.0 — Gemini Streaming Patch

## What changed
- Switched the Gemini REST request from `generateContent` to `streamGenerateContent?alt=sse`.
- Reads Server-Sent Events one `data:` line at a time and appends each text chunk to the live assistant bubble.
- Displays the first-text latency and total request duration when the stream completes.
- Keeps the existing saved API key, chat transcript, Stop control, and error handling.
- Bumped the app module version to 0.3.0 (versionCode 3).

## Apply
Merge the `app` directory into the existing Android project, preserving the exact paths. Keep the existing root Gradle files and GitHub Actions workflow. Do not copy the README or memory files into the Android source directories.

## Important
- Streaming improves perceived responsiveness by showing partial output; it does not guarantee the model starts generating sooner or reduce total generation time.
- The endpoint uses the same model ID already present in the user's working non-streaming source. If that model is unavailable for the API project, check the API error and model availability rather than changing keys or sharing them.
- API keys must remain local and must never be committed to GitHub or pasted into chat.

## Verification status
- Source edited and inspected; ZIP integrity checked after packaging.
- No Android Gradle build or live Gemini API call was run in this environment. Run `gradle assembleDebug` in the user's project and test a short prompt on-device.
