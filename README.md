# Au10tix Android Integration Sample

A reference Android app demonstrating integration of the [Au10tix](https://www.au10tix.com) identity verification SDK (v4.7.0). It covers all major SDK features — Smart Document Capture, Passive Face Liveness, Proof of Address, NFC, Voice Consent, Video Session, and ID Thickness — with a configurable UI for each.

## Prerequisites

- **Android Studio** Ladybug or newer
- **Android device or emulator** running API 26+
- **Au10tix SDK access** — GitHub Packages credentials for `https://maven.pkg.github.com/au10tixmobile/android_artifacts`
- A valid **Au10tix workflow JSON response** (the full JSON object from the Au10tix workflow API — obtain from your backend)

## Setup

### 1. Configure SDK credentials

The SDK is hosted on GitHub Packages. Replace the placeholder in `settings.gradle.kts` with your token:

```kotlin
credentials {
    username = ""
    password = "***CONTACT_SUPPORT_FOR_ACCESS***"
}
```

Contact Au10tix support to obtain a valid token.

### 2. Build

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and run it directly on a connected device.

## Running the App

1. **Launch** the app — the main screen shows the SDK status and a grid of features.
2. **Paste a JWT** — tap "Paste" or type your workflow token into the input field, then tap "Initialize".
3. Once initialized, the session ID appears at the top and all feature cards become active.
4. **Tap a feature card** to open its configuration sheet.
5. Adjust settings and tap **Launch** (or **Send** for Backend Send).

### Feature overview

| Feature | Description |
|---|---|
| Smart Document Capture (SDC) | Captures front/back of an ID document |
| Passive Face Liveness (PFL) | Liveness check via selfie camera |
| Proof of Address (POA) | Captures a utility bill or similar document |
| NFC | Reads chip data from an NFC-enabled passport or ID |
| Voice Consent | Records a spoken consent phrase |
| Video Session | Combined voice consent + document capture video |
| ID Thickness | 3D liveness check using multi-angle document scan |
| Backend Send | Submits captured data to the Au10tix backend |

## Project Structure

```
app/src/main/java/com/au10tix/integration/sample/
├── MainActivity.kt           — Compose entry point, SDK init
├── FeatureActivity.kt        — Hosts SDK UI fragments
├── features/
│   ├── FeatureType.kt        — Enum of all features (title, icon, description)
│   └── FeatureConfig.kt      — Sealed class of per-feature configuration
├── sdk/
│   └── Au10tixSdkManager.kt  — Singleton; wraps Au10xCore, exposes StateFlow
└── ui/
    ├── theme/                — Material3 dark color scheme
    ├── components/           — Shared Compose components
    └── screens/
        ├── MainScreen.kt     — Feature grid + token input
        └── FeatureConfigSheet.kt — Per-feature config sheets + backend send
```

## SDK Integration Notes

- `Au10xCore.prepare(context, jwt, callback)` must succeed before any feature can launch.
- Each feature runs in `FeatureActivity`, which hosts the SDK-provided fragment.
- The SDK fragment is obtained via `Au10UIManager.generateFragment()` — the app requests the necessary runtime permissions before calling this.
- `Au10Backend.sendRequest()` runs inline in the config sheet (no activity needed).

## AI-Assisted Integration

This project includes an AI integration reference file (`ai-integration-reference.md`) that enables AI coding assistants (such as Claude Code) to generate a complete, working Au10tix SDK integration from a short natural-language prompt.

### How it works

The reference file contains SDK facts, Gradle dependencies, permissions, API usage patterns, and code templates for every integration path and feature. When you point an AI assistant at it, it can produce a full integration — Gradle config, permissions, initialization, feature managers, and result handling — without hallucinating APIs.

### Usage

Include the following in your prompt:

```
integrate au10tix sdk to the app.
see ai-integration-reference.md for details.
```

Then describe what you need. See the example prompts below.

### Example prompts

**Native SDK — pre-built UI (SDC, PFL, Voice Session)**

Quickest path to a working integration. The SDK provides its own camera UI and flow for each feature.

```
Integrate the Au10tix Android SDK into this app.
Use ai-integration-reference.md for all SDK details, APIs, and dependencies.

Requirements:
- Integration path: native
- UI mode: ui (use the SDK's built-in fragments)
- Features: sdc, pfl

MainActivity layout:
- A multiline text field for the user to paste the full workflow JSON response (the raw JSON object returned by the Au10tix workflow API)
- 4 buttons: Init, Start SDC, Start PFL, Send to Backend
- The 2 feature buttons are disabled until Init succeeds

Behavior:
- Init: parse the pasted text as a JSONObject and pass it to Au10xCore.prepare(); show the session ID on success or an error message on failure
- Start SDC / Start PFL: launch the corresponding SDK fragment in a new Activity; show a toast with the result or error when it finishes
- Send to Backend: call Au10Backend.sendRequest(); show the request ID on success or the error message on failure

Implement all button click handlers. Include all required Gradle dependencies, manifest permissions, and Kotlin imports.
```

---

**Native SDK — custom UI (SDC, PFL, Voice Session)**

Full control over the camera view and capture buttons. The SDK delivers raw frames and callbacks; your UI handles everything else.

```
Integrate the Au10tix Android SDK into this app.
Use ai-integration-reference.md for all SDK details, APIs, and dependencies.

Requirements:
- Integration path: native
- UI mode: no-ui (custom camera UI, raw SDK callbacks — no SDK fragments)
- Features: sdc, pfl

For each feature, create a dedicated Fragment with:
- A CameraX PreviewView filling the full width at a 3:4 aspect ratio
- A "Capture" button and an "Upload" button below the preview
- The capture button triggers the SDK's capture call; the upload button submits the result
- Display capture result status (success / error message) below the buttons

MainActivity layout:
- A multiline text field for the user to paste the full workflow JSON response (the raw JSON object returned by the Au10tix workflow API)
- 4 buttons: Init, Start SDC, Start PFL, Send to Backend
- The 2 feature buttons are disabled until Init succeeds

Implement all Gradle dependencies, manifest permissions, and Kotlin imports.
```

---

**SecureMe — fully managed session (SDC, PFL, Voice Session)**

The SDK orchestrates the entire flow based on remote configuration. Minimal integration code required.

```
Integrate the Au10tix Android SDK into this app.
Use ai-integration-reference.md for all SDK details, APIs, and dependencies.

Requirements:
- Integration path: secureme
- Features included in the flow: sdc, pfl

MainActivity layout:
- A multiline text field for the user to paste the full workflow JSON response (the raw JSON object returned by the Au10tix workflow API)
- A single "Start Session" button

Behavior:
- Tapping "Start Session" parses the pasted text as a JSONObject, initializes SecureMe with it, and immediately launches the managed flow
- Show a success message with the session result when the flow completes
- Show an error message if initialization or any step fails

Include all required Gradle dependencies (secure-me artifact plus one dependency per feature), manifest permissions, and Kotlin imports.
```

## Removing Features

See [FEATURE_MAPPING.md](FEATURE_MAPPING.md) for step-by-step instructions on removing individual features (code, dependencies, permissions) to produce a minimal integration.
