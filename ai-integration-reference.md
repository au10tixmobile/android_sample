# AU10TIX Android SDK — AI Integration Reference

## How to Use This File

You are generating a complete AU10TIX Android SDK integration. The user will tell you:
1. **Integration path**: `native` or `secureme`
2. **UI mode**: `ui` (use pre-built UI fragments) or `no-ui` (custom UI, raw callbacks) — **only applies to `native` path; `secureme` is always UI-only**
3. **Features**: one or more of `sdc`, `pfl`, `poa`, `vc`, `vs`, `id-liveness`, `afl`, `nfc`

From those three inputs, generate the complete working integration:
- All Gradle dependencies
- All permissions
- Core preparation
- Feature manager setup
- Session handling
- Results and error handling

Use **Kotlin** for all code examples. Always include imports for classes you reference.

---

## SDK Facts

- **Current version**: `4.7.0`
- **Min SDK**: API 26 (Android 8.0 Oreo)
- **Host Activity**: Must extend `FragmentActivity` (e.g., `AppCompatActivity`)
- **Language**: Kotlin (Java interop supported)
- **Maven group**: `com.au10tix.sdk`
- **Sample project**: https://github.com/au10tixmobile/android_sample

---

## Step 1 — Maven Repository (always required)

In `settings.gradle.kts`, inside `dependencyResolutionManagement.repositories`:

```kotlin
maven {
    url = uri("https://maven.pkg.github.com/au10tixmobile/android_artifacts")
    credentials {
        username = ""
        password = "***CONTACT_SUPPORT_FOR_PASSWORD***"
    }
}
```

---

## Step 2 — Core Dependencies (always required)

In the **app module** `build.gradle.kts`:

```kotlin
val au10Version = "4.7.0"
val cameraxVersion = "1.5.2"

dependencies {
    implementation("com.au10tix.sdk:au10tix:$au10Version")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("com.google.android.gms:play-services-base:18.5.0")
}
```

---

## Step 3 — Feature Dependencies

Add the dependency for each requested feature. Use the `ui` variant unless `no-ui` was requested.

| Feature | UI variant | No-UI (base) variant |
|---|---|---|
| SDC | `com.au10tix.sdk:smart-document:$au10Version` | `com.au10tix.sdk:smart-document-base:$au10Version` |
| PFL | `com.au10tix.sdk:passive-face-liveness:$au10Version` | `com.au10tix.sdk:passive-face-liveness-base:$au10Version` |
| POA | `com.au10tix.sdk:smart-document:$au10Version` | `com.au10tix.sdk:smart-document-base:$au10Version` (same as SDC — do not duplicate) |
| VC | `com.au10tix.sdk:voice-consent:$au10Version` | `com.au10tix.sdk:voice-consent-base:$au10Version` |
| VS | `com.au10tix.sdk:voice-consent:$au10Version` | `com.au10tix.sdk:voice-consent-base:$au10Version` (same as VC — do not duplicate) |
| ID Liveness | `com.au10tix.sdk:voice-consent:$au10Version` | UI only — no base variant |
| AFL | `com.au10tix.sdk:active-face-liveness:$au10Version` + `com.google.android.gms:play-services-mlkit-face-detection:17.1.0` | `com.au10tix.sdk:active-face-liveness-base:$au10Version` + same mlkit dep |
| PFL (mlkit) | also requires `com.google.android.gms:play-services-mlkit-face-detection:17.1.0` | same |
| NFC | AAR files from support (see NFC section) | — |
| SecureMe | `com.au10tix.sdk:secure-me:$au10Version` | — |

If using **UI components**, also add:
```kotlin
implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
implementation("androidx.constraintlayout:constraintlayout:2.2.1")
implementation("androidx.recyclerview:recyclerview:1.4.0")
implementation("androidx.appcompat:appcompat:1.7.0")
implementation("com.google.android.material:material:1.12.0")
```

---

## Step 4 — Permissions

Add to `AndroidManifest.xml`. Permissions are merged automatically — no need to add them manually.
Your app **must** request permissions at runtime (Android 6.0+). The SDK will not prompt — it returns an error if permissions are missing.

| Permission | When required |
|---|---|
| `android.permission.CAMERA` | All features (required) |
| `android.permission.ACCESS_COARSE_LOCATION` | Optional — adds GPS metadata to captures |
| `android.permission.ACCESS_FINE_LOCATION` | Optional — adds GPS metadata to captures |
| `android.permission.NFC` | NFC feature |
| `android.permission.RECORD_AUDIO` | PFL screen recording, AFL, VC, VS, ID Liveness |

---

## Step 4.1 — Theme Requirements (always required for UI)

Your app's theme **must** inherit from a `Theme.AppCompat` or `Theme.MaterialComponents` variant (e.g., `Theme.MaterialComponents.DayNight.NoActionBar`). Using a standard `android:Theme.Material` theme will cause the SDK UI fragments to crash.

---

## Step 5 — Core Preparation (always required)

Pass the full workflow API response as a `JSONObject` directly to `prepare()`. The SDK extracts `accessToken`, `session`, and `assets` internally.

The input is always the raw JSON string from the AU10TIX workflow API, parsed into a `JSONObject`:

```kotlin
// workflowResponseString: the raw JSON string from the AU10TIX workflow API
val workflowResponse = JSONObject(workflowResponseString.trim())

Au10xCore.prepare(
    requireActivity(),
    workflowResponse,
    object : OnPrepareCallback {
        override fun onPrepared(sessionId: String) {
            // SDK ready. Store sessionId if needed for backend correlation.
            val coreManager = Au10xCore.getInstance(requireContext())
        }
        override fun onPrepareError(error: Au10Error?) {
            // Use error?.message for the error description
        }
    }
)
```

The SDK expects this structure inside `workflowResponse`:
```json
{
  "response": {
    "accessToken": "...",
    "session": "...",
    "assets": [...]
  }
}
```

Never decompose the JSON into individual token fields or use placeholders like `YOUR_ACCESS_TOKEN_HERE`. Always pass the full `JSONObject`.

---

## Integration Path A — SecureMe (native=false, path=secureme)

Use when: the user wants a fully managed flow with no per-feature orchestration code.

> **UI mode note**: SecureMe is **UI-only**. There is no `no-ui` / custom-UI variant. The `createUI()` call always returns the SDK's pre-built `SecureMeUI` fragment. If the user requests SecureMe with `no-ui`, clarify this constraint and fall back to `ui` mode.

### Required dependencies

SecureMe requires the `secure-me` artifact **plus a dependency for every feature used in the flow**. Missing a feature dependency causes a `MissingPackage` error at runtime.

```kotlin
// Core SecureMe
implementation("com.au10tix.sdk:secure-me:$au10Version")

// Feature modules — add only the ones your flow uses (UI variant)
implementation("com.au10tix.sdk:smart-document:$au10Version")          // SDC + POA
implementation("com.au10tix.sdk:passive-face-liveness:$au10Version")  // PFL
implementation("com.au10tix.sdk:active-face-liveness:$au10Version")   // AFL
implementation("com.au10tix.sdk:voice-consent:$au10Version")          // VC + VS + ID Liveness
implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0") // required by PFL + AFL

// NFC — AAR files from AU10TIX Support, place in app/libs/
implementation(files("libs/NFC-release.aar"))
implementation(files("libs/NFC-UI-release.aar"))
implementation("org.jmrtd:jmrtd:0.8.5")
implementation("net.sf.scuba:scuba-sc-android:0.0.26")
implementation("com.madgag.spongycastle:prov:1.58.0.0")
```

### Token scope required
```
ocs/scope:secureme
```

### Imports

```kotlin
import com.au10tix.sdk.commons.Au10Error
import com.au10tix.sdk.protocol.FeatureSessionError
import com.au10tix.sdk.protocol.FeatureSessionResult
import com.au10tix.secureMe.SecureMe
import com.au10tix.secureMe.SecureMeResult
import com.au10tix.secureMe.callback.SecureMeCallback
import com.au10tix.secureMe.callback.SecureMePrepareCallback
```

### Full integration

```kotlin
// 1. Initialize — pass the full workflow API response JSONObject directly
val secureMe = SecureMe(
    activity,
    workflowResponse,
    object : SecureMePrepareCallback {
        override fun onPrepared(secureMe: SecureMe) {
            // 2. Create UI
            val flow = null    // null = use OCS remote config (recommended)
            // flow type is SMFlow? — see "Local flow config" below
            val config = null  // null = use defaults
            val secureMeUI = secureMe.createUI(flow, config, secureMeCallback)
            // 3. Add SecureMeUI to your activity
        }
        override fun onError(error: Au10Error?) { }
    }
)

// Callback
val secureMeCallback = object : SecureMeCallback {
    override fun onUpdate(update: String, featureResult: FeatureSessionResult?) {
        // Individual feature started/finished
    }
    override fun onComplete(result: SecureMeResult) {
        // result.sdcFrontResult, result.sdcBackResult, result.pflResult, etc.
        val requestId = result.requestId
    }
    override fun onError(error: FeatureSessionError) { }
}

// 4. Cleanup when done
secureMe.destroy()
```

### Local flow config (optional, if not using OCS)

```kotlin
// SMFlow is NOT a builder — methods do not return self. Call each separately.
// Each method has different optional parameters — only pass what you need.
val flow = SMFlow()

// withFrontSDC(enabled, showIntro = true, enableFileUpload = true)
flow.withFrontSDC(enabled = true)

// withBackSDC(enabled, enableFileUpload = true, sendFeatureResult = true)
flow.withBackSDC(enabled = true)

// withPFL(enabled, showIntro = true, sendFeatureResult = true)
flow.withPFL(enabled = true)

// withPOA(enabled, showIntro = true, enableFileUpload = true, sendFeatureResult = true)
flow.withPOA(enabled = false)

// withVoiceConsent(enabled, showIntro = true, sendFeatureResult = true)
flow.withVoiceConsent(enabled = false)

// withVideoSession(enabled, showIntro = true, sendFeatureResult = true)
flow.withVideoSession(enabled = false)

// withAFL(enabled, showIntro = true, enableFileUpload = true, sendFeatureResult = true)
flow.withAFL(enabled = false)

// withNFC(enabled, enableFileUpload = true, sendFeatureResult = true)  // no showIntro param
flow.withNFC(enabled = false)

val config = SMConfig(
    withPflDetectionDelay = false,
    pflDelaySecs = 0,
    sendResults = true,
    voiceConsentText = "I consent to this identity verification.",
    voiceConsentSessionTime = 20
)
```

### SecureMe errors

| Error | Cause | Fix |
|---|---|---|
| `MissingPackage` | Required SDK module not imported | Import all feature dependencies |
| `sdkNotPrepared` | `createUI()` called before `onPrepared()` | Wait for `onPrepared()` |
| `aflPflConflict` | Both AFL and PFL enabled | Disable one |
| `missingScope` | Token missing `ocs/scope:secureme` | Add scope to token |

---

## Integration Path B — SDK UI Components (ui=true, native)

Use when: the user wants pre-built capture UI per feature but manages the flow themselves.

### Pattern (same for all features)

```kotlin
// 1. Prepare core (see Step 5)
val coreManager = Au10xCore.getInstance(requireContext())

// 2. Initialize the feature manager (see per-feature sections below)
val featureManager = <FeatureManager>(baseContext, this)

// 3. Build the UI
val builder = Au10UIManager.Builder(fragmentActivity, featureManager, uiCallback)

// Optional builder config:
builder.setUIMode(AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) // MODE_NIGHT_YES / MODE_NIGHT_NO
builder.showCloseButton(true)    // default: true
builder.canUpload(true)          // default: true (SDC/POA only)
builder.showPrimaryButton(true)  // default: true

val au10UIManager = builder.build()
val fragment = au10UIManager?.generateFragment()
// fragment == null means permissions are missing

// 4. Add fragment fullscreen (covers the entire activity window)
// Always use android.R.id.content as the container — never a nested FrameLayout
```

### UICallback

```kotlin
val uiCallback = object : UICallback() {
    override fun onSessionResult(sessionResult: FeatureSessionResult) {
        // Cast: e.g. sessionResult as SmartDocumentResult
    }
    override fun onSessionError(sessionError: FeatureSessionError) {
        if (sessionError.severity != FeatureSessionError.SEVERITY_ERROR) {
            // Only a warning — log/show but don't close the session
        } else {
            // Fatal error — close the session
        }
    }
    override fun onSessionUpdate(captureFrameUpdate: Au10Update) { }
    override fun onFail(result: FeatureSessionResult?) { }  // PFL only
}
```

### Disable intro screen

```kotlin
featureManager.isShowIntroScreen = false
```

---

## Integration Path C — Custom UI (ui=false, native)

Use when: the user builds their own camera UI.

### Layout requirement

Add a `FrameLayout` to your layout for the camera preview. Recommended aspect ratio: **3:4**.

```xml
<FrameLayout
    android:id="@+id/previewContainer"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    app:layout_constraintDimensionRatio="3:4" />
```

### Session pattern (all features)

```kotlin
// 1. Prepare core (see Step 5)
val coreManager = Au10xCore.getInstance(requireContext())

// 2. Initialize feature manager (see per-feature sections)
val featureManager = <FeatureManager>(baseContext, this)

// 3. Start session
val previewView = binding.previewContainer  // your FrameLayout
coreManager.startSession(featureManager, previewView, sessionCallback)

// 4. Handle callbacks
val sessionCallback = object : SessionCallback {
    override fun onSessionResult(sessionResult: FeatureSessionResult) {
        // Cast to feature-specific type
    }
    override fun onSessionError(sessionError: FeatureSessionError) {
        // Check severity before failing the session.
        // FeatureSessionError.SEVERITY_WARNING = non-fatal; log/show but keep session alive.
        // Any other severity = fatal; stop the session.
        if (sessionError.severity != FeatureSessionError.SEVERITY_WARNING) {
            // fail the session
        } else {
            // show warning, continue
        }
    }
    override fun onSessionUpdate(captureFrameUpdate: Au10Update) { }
}

// Manual capture (any feature)
coreManager.captureStillImage()

// Upload image instead of camera capture
coreManager.detectObjectInImage(featureManager, imageUri, sessionCallback)

// Stop session
coreManager.stopSession()
```

---

## Feature: SDC (Smart Document Capture)

**Purpose**: Captures ID document images (front and/or back) at optimal quality.

### Manager

```kotlin
val sdcFeatureManager = SmartDocumentFeatureManager(baseContext, this)

// Capture back side (default is front)
sdcFeatureManager.isFrontSide = false

// Restrict detection to a specific area
sdcFeatureManager.setRectOfInterest(rect)  // Rect relative to camera view
```

### Updates (no-ui only)

Cast `Au10Update` to `SmartDocumentFeatureSessionFrame`:

```kotlin
override fun onSessionUpdate(au10Update: Au10Update) {
    val frame = au10Update as? SmartDocumentFeatureSessionFrame ?: return
    val status = frame.status    // SDC Status (see table) — constant on SmartDocumentFeatureManager
    if (status == SmartDocumentFeatureManager.STATUS_OK) {
        val idStatus = frame.idStatus  // SDC ID Status — constant on SmartDocumentFeatureSessionFrame
    }
    // Additional feedback when idStatus == BAD_IMAGE_QUALITY:
    when (frame.additionalFeedback) {
        SmartDocumentFeatureSessionFrame.IMAGE_TOO_DARK -> { }
        SmartDocumentFeatureSessionFrame.IMAGE_TOO_BLURRY -> { }
        SmartDocumentFeatureSessionFrame.IMAGE_HAS_REFLECTION -> { }
        SmartDocumentFeatureSessionFrame.IMAGE_TOO_BRIGHT -> { }
    }
}
```

### Result (no-ui only)

```kotlin
override fun onSessionResult(sessionResult: FeatureSessionResult) {
    val result = sessionResult as SmartDocumentResult
    val bitmap = result.imageFile.getBitmap()
    val croppedBitmap = result.croppedImageFile?.getBitmap()
}
```

### SDC Status codes

| Value | Constant | Meaning |
|---|---|---|
| 0 | `STATUS_OK` | No issues — check idStatus |
| 1 | `STATUS_UNSTABLE` | Device moving — can't capture |
| 2 | `STATUS_SLOW_NETWORK` | Poor network — affects server analysis |
| 3 | `IMAGE_CAPTURED` | Capture in progress |
| 8 | `STATUS_NOTWORK_ERROR` | Network error |

### SDC ID Status codes

| Value | Constant | Meaning |
|---|---|---|
| 0 | `BAD_IMAGE_QUALITY` | Blurry, reflection, or dark |
| 1 | `GOOD_IMAGE_QUALITY` | Acceptable |
| 2 | `NO_ID_DETECTED` | No document in frame |
| 10 | `IMAGE_TOO_FAR` | Camera too far |
| 11 | `IMAGE_TOO_CLOSE` | Camera too close |

### Cleanup

```kotlin
sdcFeatureManager.destroy()
```

### BE Kit side selection

```kotlin
sdcFeatureManager.isFrontSide = true   // front (default)
sdcFeatureManager.isFrontSide = false  // back
```

---

## Feature: PFL (Passive Face Liveness)

**Purpose**: Captures a selfie and performs a passive liveness check. Also used for face comparison.

### Manifest (add for auto ML model download)

```xml
<application>
    <meta-data
        android:name="com.google.mlkit.vision.DEPENDENCIES"
        android:value="face" />
</application>
```

### Manager

```kotlin
val pflFeatureManager = FaceLivenessFeatureManager(baseContext, this)

// Optional config
pflFeatureManager.setPflDelaySecs(3)      // delay detection start by N seconds
pflFeatureManager.setIsSelfieOnly(true)   // skip liveness — capture selfie only
pflFeatureManager.setF2F(true)            // set for Face-to-Face flow (not IDV)
```

### Updates (no-ui only)

Cast to `FaceLivenessUpdate`. Update codes are constants in `PFLConsts`.

```kotlin
override fun onSessionUpdate(au10Update: Au10Update) {
    val update = au10Update as? FaceLivenessUpdate ?: return
    val code = update.statusCode  // compare against PFLConsts constants
}
```

### Result (no-ui only)

```kotlin
override fun onSessionResult(sessionResult: FeatureSessionResult) {
    val bitmap = sessionResult.frameData.getBitmap()
}
```

### Liveness validation (no-ui only)

After capture:
```kotlin
pflFeatureManager.validateLiveness(faceLivenessResult, object : LivenessCallback {
    override fun onSuccess(result: FaceLivenessResult) { }
    override fun onFail(result: FaceLivenessResult) {
        if (pflFeatureManager.canRetry()) {
            coreManager.startSession(pflFeatureManager, previewView, sessionCallback)
        } else {
            coreManager.stopSession()
        }
    }
    override fun onError(error: FeatureSessionError) { }
})
```

**Important — `sessionCallback` must be `lateinit var`, not `val`.**
`validateLiveness` is called from inside `onSessionResult`, which is itself part of the `sessionCallback` object expression. The nested `LivenessCallback.onFail` then references `sessionCallback` again to restart the session. If `sessionCallback` is declared as a `val` initialized by an anonymous object, the Kotlin compiler treats this as a self-reference during initialization and will fail to compile.

Declare it as a class member and initialize it before first use:
```kotlin
private lateinit var sessionCallback: SessionCallback
// ...
// inside onViewCreated (or equivalent), before calling startSession:
sessionCallback = object : SessionCallback { ... }
```

Limits: 6 second timeout per request. Retry once on failure. Limited attempts per token (configured in OCS).

### Cleanup

```kotlin
pflFeatureManager.destroy()
```

### Screen recording (optional, OCS must enable it)

```kotlin
pflFeatureManager.verifyFeatureRequirementsFulfilled(activity) { mayRecord ->
    if (mayRecord) startSession() else handlePermissionDenied()
}
```

### PFLConsts update codes

| Code | Constant | Meaning |
|---|---|---|
| 9 | `RECORDING_STARTED` | Screen recording started |
| 12 | `USER_INTERRUPTED` | Session interrupted |
| 13 | `RECORDING_ENDED` | Recording ended |
| 200 | `HOLD_STEADY` | Good frames — keep still |
| 300 | `ERROR_INTERNAL` | Internal error |
| 301 | `ERROR_HOLD_DEVICE_STRAIGHT` | Hold device vertically |
| 302 | `ERROR_NO_FACE_DETECTED` | No face |
| 303 | `ERROR_MULTIPLE_FACES_DETECTED` | Multiple faces |
| 304 | `ERROR_FACE_TOO_FAR` | Move closer |
| 305 | `ERROR_FACE_TOO_CLOSE` | Too close |
| 306 | `ERROR_HOLD_DEVICE_STEADY` | Device unstable |
| 307 | `ERROR_FAILED_ALL_RETRIES` | Retry limit reached |
| 309–313 | `ERROR_FACE_TOO_CLOSE_TO_*` | Face position off-center |
| 314 | `ERROR_FACE_CROPPED` | Face partially out of frame |
| 315 | `ERROR_FACE_ANGLE_TOO_LARGE` | Angle too extreme |
| 316 | `ERROR_FACE_IS_OCCLUDED` | Face covered |
| 333 | `MISSING_PLAY_SERVICES` | Play Services unavailable |

---

## Feature: POA (Proof of Address)

**Purpose**: Captures proof-of-address documents. Subset of SDC — same dependency, same status codes.

### Manager

```kotlin
val poaManager = PoaFeatureManager(baseContext, this)

// Upload without POA processing
poaManager.setUploadOnly(true)
```

### Updates and results

Same as SDC. Cast updates to `SmartDocumentFeatureSessionFrame`, results to `SmartDocumentResult`.

### Cleanup

```kotlin
poaManager.destroy()
```

---

## Feature: VC (Voice Consent)

**Purpose**: Records a video of the user reading consent text aloud.

### Manager

```kotlin
val vcManager = VoiceConsentFeatureManager(baseContext, this)

val vcConfig = VoiceConsentConfig(
    "I consent to this identity verification.",  // required, max 170 chars
    20                                 // 5–30 seconds
)
vcManager.setConfig(vcConfig)
```

### Recording control (no-ui only)

```kotlin
vcManager.startRecording()
vcManager.endRecording()
// result → onSessionResult
```

### Cleanup

```kotlin
vcManager.destroy()
```

---

## Feature: VS (Video Session)

**Purpose**: Two-phase video — voice consent + ID presentation.

### Manager

```kotlin
val vsManager = VideoSessionFeatureManager(baseContext, this)

val vsConfig = VideoSessionConfig(
    "I consent to this identity verification.",  // consent text, max 170 chars
    7,              // voice consent phase: 4–30 seconds
    5,              // ID presentation phase: 4–30 seconds
    false   // require explicit user agreement before start
)
vsManager.setConfig(vsConfig)  // mandatory — missing config causes missingConfigurations error
```

### Recording control (no-ui only)

```kotlin
vsManager.startRecording()
vsManager.endRecording()
```

### Cleanup

```kotlin
vsManager.destroy()
```

---

## Feature: ID Liveness

**Purpose**: Records video of ID shown at 3 angles (front, 45° angle, back). **UI component only.**

### Manager

```kotlin
val idLivenessManager = IDLivenessFeatureManager(baseContext, this)

// Config is optional — defaults used if not set
val idLivenessConfig = IDLivenessConfig(
    3,         // countdown before each phase: 1–6 seconds
    8,         // front side: 1–15 seconds
    8,         // angled front: 1–15 seconds
    8,          // back side: 1–15 seconds
    false // require consent before start
)
idLivenessManager.setConfig(idLivenessConfig)
```

Must be used via UI component — pass `idLivenessManager` to `Au10UIManager.Builder`.

### Cleanup

```kotlin
idLivenessManager.destroy()
```

---

## Feature: AFL (Active Face Liveness)

**Purpose**: Gesture-based liveness — PFL + head-turn gesture challenges. Screen recording required.

### Manager

```kotlin
val aflFeatureManager = ActiveFaceLivenessFeatureManager(requireActivity(), this)
```

### Permission — must request before startSession

```kotlin
aflFeatureManager.requestScreenRecordingPermission(requireActivity()) { mayRecord ->
    if (mayRecord) {
        coreManager.startSession(aflFeatureManager, previewView, sessionCallback)
    } else {
        // prompt user to allow recording
    }
}
```

### Updates (no-ui only)

```kotlin
override fun onSessionUpdate(au10Update: Au10Update) {
    when (au10Update) {
        is FaceLivenessUpdate -> {
            // PFL phase feedback — see PFLConsts above
        }
        is AFLUpdate -> {
            when (au10Update.updateType) {
                AFLConsts.NEW_GESTURE -> {
                    val gesture = au10Update.challenge
                    // AFLConsts gesture values:
                    // GESTURE_CHALLENGE_CENTER (0), GESTURE_CHALLENGE_LEFT (1), GESTURE_CHALLENGE_RIGHT (2)
                }
                AFLConsts.GESTURE_PASSED -> { }
                AFLConsts.GESTURE_TIMEOUT -> { }
                AFLConsts.GESTURE_TIMEOUT_NO_ATTEMPTS_LEFT -> { }
                AFLConsts.WRONG_GESTURE -> { }
                AFLConsts.WAITING_FOR_USER -> {
                    // Flow paused — call when ready to continue:
                    aflFeatureManager.proceedToNextStep()
                    // Reason: au10Update.waitingReason
                    // START_PFL(1), START_AFL(2), RETRY_PFL(3), RETRY_AFL(4)
                }
                AFLConsts.WAITING_FOR_PFL -> { /* awaiting server */ }
                AFLConsts.RECORDING_STARTED -> { }
            }
        }
    }
}
```

### Result (no-ui only)

```kotlin
override fun onSessionResult(sessionResult: FeatureSessionResult) {
    val result = sessionResult as AFLResult
    val selfie = result.frameData.getBitmap()
    val gestureJson = result.gestureJson           // JSONObject
    val recordingUri = result.sessionRecordingURI  // String
}
```

### Cleanup

```kotlin
aflFeatureManager.userInterrupted()  // call if user navigates away (costs one attempt)
aflFeatureManager.destroy()
```

---

## Feature: Local SDC

**Purpose**: On-device document analysis using a downloaded ML model — no internet connection required for inference.

### Dependency

```gradle
implementation "com.au10tix.sdk:local-infer:$au10Version"

// TFLite runtime (required)
implementation 'org.tensorflow:tensorflow-lite:2.9.0'
implementation 'org.tensorflow:tensorflow-lite-support:0.4.2'
```

### Manager

`LocalSdcManager` is a Kotlin singleton object — no instantiation needed, access methods directly.

`OperationStatus` is a functional interface with a single method:
```kotlin
LocalSdcManager.OperationStatus { operationSuccessful: Boolean, err: FeatureSessionError? -> }
```

### Setup Flow

#### 1. Initialize

```kotlin
LocalSdcManager.initialize(context, LocalSdcManager.OperationStatus { success, err ->
    if (!success) { /* handle err */ }
})
```

#### 2. Download Model Files

Downloads the ML model to the device. Call once; files persist across sessions.

```kotlin
LocalSdcManager.downloadLocalSdcFiles(context, LocalSdcManager.OperationStatus { success, err ->
    if (!success) { /* handle err */ }
})
```

Check availability before downloading:

```kotlin
val filesExist: Boolean = LocalSdcManager.localSdcFilesExist()
LocalSdcManager.latestLocalSdcFilesAvailable(context, LocalSdcManager.OperationStatus { isLatest, err ->
    if (isLatest) { /* up to date */ } else { /* download */ }
})
```

#### 3. Warm Up (Optional — reduces first-inference latency)

```kotlin
LocalSdcManager.warmup(context, LocalSdcManager.OperationStatus { success, err ->
    if (!success) { /* handle err */ }
})
```

#### 4. Configure the SDC Feature Manager

```kotlin
val sdcFeatureManager = SmartDocumentFeatureManager(baseContext, this)
LocalSdcManager.useLocally = true            // use on-device model
LocalSdcManager.withAdditionalFeedback = true // optional: richer frame feedback
```

#### 5. Start Session (same as standard SDC)

No-UI:

```kotlin
coreManager.startSession(sdcFeatureManager, previewFrameLayout, sessionCallback)
```

UI:

```kotlin
val builder = Au10UIManager.Builder(fragmentActivity, sdcFeatureManager, uiCallback)
val au10UIManager = builder.build()
val fragment = au10UIManager?.generateFragment()
```

### Result

```kotlin
override fun onSessionResult(sessionResult: FeatureSessionResult) {
    val result = sessionResult as SmartDocumentResult
    val bitmap = result.frameData.getBitmap()
}
```

### Delete Model Files (Optional)

```kotlin
val deleted: Boolean = LocalSdcManager.deleteLocalSdcFiles()
```

### Cleanup

```kotlin
sdcFeatureManager.destroy()
```

---

## Feature: NFC

**Purpose**: Extracts data from the NFC chip in a passport or ID card (2-phase: MRZ scan → chip tap).

### Dependency — AAR files (contact AU10TIX Support)

Place the AAR files in `app/libs/`, then in `app/build.gradle.kts`:

```kotlin
// Project-level: add flatDir repo in settings.gradle.kts repositories block
// flatDir { dirs("libs") }

implementation(files("libs/NFC-release.aar"))
implementation(files("libs/NFC-UI-release.aar"))
implementation("org.jmrtd:jmrtd:0.8.5")
implementation("net.sf.scuba:scuba-sc-android:0.0.26")
implementation("com.madgag.spongycastle:prov:1.58.0.0")
```

### Manifest

```xml
<application>
    <meta-data
        android:name="com.google.mlkit.vision.DEPENDENCIES"
        android:value="ocr" />
</application>
```

### Manager — choose one

```kotlin
// Known document type
val nfcManager = PassportFeatureManager(requireActivity(), this)
nfcManager.setID(true)   // true = ID card, false = passport

// Unknown type — user selects on intro screen (UI only)
val nfcManager = NFCFeatureManager(requireActivity(), this)
```

### Session flow (no-ui only)

```kotlin
coreManager.startSession(nfcManager, previewView, sessionCallback)

override fun onSessionResult(sessionResult: FeatureSessionResult) {
    val nfcResult = sessionResult as NFCSessionResult
    if (!nfcResult.isNfcScanned) {
        // Phase 1 done (MRZ) — start chip extraction
        nfcManager.resumeNfcScan()
    } else {
        // Phase 2 done — extract chip data
        val dg1 = nfcResult.getDataByKey(NFCSessionResult.DG1) as? ByteArray  // document data
        val dg2 = nfcResult.getDataByKey(NFCSessionResult.DG2) as? ByteArray  // face image
        val dg7 = nfcResult.getDataByKey(NFCSessionResult.DG7) as? ByteArray  // signature
        // Also: DG14, DG11, DG12, DG13 (Workflow300)
    }
}
```

### Skip MRZ scan (if MRZ data already available)

```kotlin
val bacKey = BACKey(documentNumber, dateOfBirth, dateOfExpiry)
nfcManager.setBACKey(bacKey)
// Then call startSession() — MRZ phase is skipped
```

### Local chip authentication

```kotlin
when (nfcResult.getDataByKey(NFCSessionResult.AUTHENTICATED)) {
    CAStatus.NOT_EXISTS -> { /* cannot validate */ }
    CAStatus.VALID -> { /* chip authentic */ }
    CAStatus.NOT_VALID -> { /* chip invalid */ }
}
```

### Cleanup

```kotlin
nfcFeatureManager.destroy()
```

### NFC session updates

Cast to `Au10NFCUpdate` in `onSessionUpdate`:

| Code | Constant | Meaning |
|---|---|---|
| 0 | `STATUS_NONE` | No status |
| 1 | `STATUS_SEARCHING_MRZ` | Looking for MRZ — point camera at document |
| 2 | `STATUS_SEARCHING_NFC` | MRZ found — tap device to chip |
| 3 | `STATUS_AUTHENTICATING` | Connecting to chip |
| 4 | `STATUS_EXTRACTING_DG1` | Reading document data |
| 5 | `STATUS_EXTRACTING_DG2` | Reading face image |
| 6 | `STATUS_EXTRACTING_DG11` | Reading additional personal data |
| 7 | `STATUS_EXTRACTING_DG12` | Reading additional personal details |
| 8 | `STATUS_EXTRACTING_DG13` | Reading optional details |
| 9 | `STATUS_EXTRACTING_DG7` | Reading signature |
| 10 | `STATUS_CHIP_AUTHENTICATION` | Verifying chip authenticity |
| 11 | `STATUS_EXTRACTING_DG15` | Reading public key (active auth) |

---

## Backend — Sending Results to AU10TIX

### Preload media (optional)

Call before `sendRequest()` to upload media early. If not called, media is uploaded automatically during `sendRequest()`.

```kotlin
Au10Backend.uploadMedia(Au10Backend.MEDIA_ID_FRONT)
```

| Constant | Value |
|---|---|
| `MEDIA_ID_FRONT` | ID front side |
| `MEDIA_ID_BACK` | ID back side |
| `MEDIA_POA` | Proof of address |
| `MEDIA_PFL` | Passive face liveness selfie |
| `MEDIA_F2F` | Face-to-face selfie |
| `MEDIA_AFL` | Active face liveness |
| `MEDIA_NFC` | NFC chip data |
| `MEDIA_VOICE_CONSENT` | Voice consent recording |
| `MEDIA_VIDEO_SESSION` | Video session recording |
| `MEDIA_ID_LIVENESS_SESSION` | ID liveness recording |

### Send request

```kotlin
Au10Backend.sendRequest(object : BackendCallback {
    override fun onSuccess(requestID: String) {
        // Use requestID to retrieve verification result
    }
    override fun onError(error: FeatureSessionError) {
        // Check error.message for details
    }
})
```

If this fails: the session token is likely missing `media` or `beginProcessing` scope.

---

## Advanced: Suspicious Behavior Detection

Detects spoofing, replay attacks, and injected frames. Available for SDC, PFL, and SecureMe.

```kotlin
// SuspiciousConfig property names may be stripped by ProGuard in the SDK AAR.
// Use SuspiciousConfig() for all defaults, or set properties if your ProGuard rules keep them.
// Properties and their defaults:
//   checkInterval        = 4       — frames between checks
//   stillDeviceThreshold = 0.08F   — max gyro rate to count as still
//   preFilterThreshold   = 0.02F   — max RGB delta between frames
//   fastFilterThreshold  = 0.9F    — max hash delta between frames
//   pixelPerfectThreshold = 0.4F   — max pixel-level delta
featureManager.suspiciousConfig = SuspiciousConfig()
```

Must be set before `startSession`. If not set, detection is disabled.

Result in callback:
```kotlin
val report = (result as SmartDocumentResult).suspicionReport
// or: (result as FaceLivenessResult).suspicionReport
report.isSuspicious()     // Boolean — true when score >= 0.5
report.suspiciousScore()  // Float — 0.0 (clean) to 1.0 (suspicious)
```

---

## Advanced: FEC (Front End Classification)

Classifies the captured ID type. Requires `fec` token scope.

```kotlin
SmartDocumentFeatureManager.getClassification(
    imageFile,   // ImageRepresentation
    object : FECCallback {
        override fun onResult(fecResult: FECResult) {
            val result = fecResult.classificationResult  // JSON string with "result" field
            // result values: UNRECOGNIZED_DOCUMENT | UNSUPPORTED_DOCUMENT |
            //                REQUIRED_BOTH_SIDES | NOT_REQUIRED_BOTH_SIDES
            val isBlocked = fecResult.isIdBlocked          // Boolean
            val needs2Sides = fecResult.is2ndSideMandatory // Boolean
            val sideCode = fecResult.documentSideCode
            // sideCode: FRONT | BACK | FRONT_AND_BACK | DUAL | DATA | UNDEFINED
        }
        override fun onError(errorCode: Int, errorDescription: String?) { }
    }
)
```

---

## Kotlin Import Reference (verified against SDK 4.7.0)

Use these exact package paths. All others are incorrect guesses.

### Core

```kotlin
import com.au10tix.sdk.core.Au10xCore
import com.au10tix.sdk.core.OnPrepareCallback
import com.au10tix.sdk.commons.Au10Error
import com.au10tix.sdk.core.comm.SessionCallback
import com.au10tix.sdk.protocol.Au10Update
import com.au10tix.sdk.protocol.FeatureSessionError
import com.au10tix.sdk.protocol.FeatureSessionResult
```

### Backend

```kotlin
import com.au10tix.sdk.backend.Au10Backend
import com.au10tix.sdk.backend.BackendCallback
```

### SDC (Smart Document Capture)

```kotlin
import com.au10tix.smartDocument.SmartDocumentFeatureManager       // note capital D
import com.au10tix.smartDocument.SmartDocumentFeatureSessionFrame
import com.au10tix.smartDocument.SmartDocumentResult
```

Status/ID-status constants live on `SmartDocumentFeatureSessionFrame`, not the manager:
- `SmartDocumentFeatureSessionFrame.BAD_IMAGE_QUALITY`
- `SmartDocumentFeatureSessionFrame.GOOD_IMAGE_QUALITY`
- `SmartDocumentFeatureSessionFrame.NO_ID_DETECTED`
- `SmartDocumentFeatureSessionFrame.IMAGE_TOO_FAR`
- `SmartDocumentFeatureSessionFrame.IMAGE_TOO_CLOSE`

Status constants on manager:
- `SmartDocumentFeatureManager.STATUS_OK`
- `SmartDocumentFeatureManager.STATUS_UNSTABLE`
- `SmartDocumentFeatureManager.STATUS_SLOW_NETWORK`
- `SmartDocumentFeatureManager.STATUS_NOTWORK_ERROR`

### POA (Proof of Address)

```kotlin
import com.au10tix.poa.PoaFeatureManager
```

### PFL (Passive Face Liveness)

```kotlin
import com.au10tix.faceliveness.FaceLivenessFeatureManager
import com.au10tix.faceliveness.FaceLivenessResult
import com.au10tix.faceliveness.FaceLivenessUpdate
import com.au10tix.faceliveness.LivenessCallback
import com.au10tix.faceliveness.PFLConsts
```

### VC / VS / ID Liveness

All three live in the same package:

```kotlin
import com.au10tix.voiceconsent.VoiceConsentFeatureManager
import com.au10tix.voiceconsent.VoiceConsentConfig          // positional ctor: (text, maxSessionTime)
import com.au10tix.voiceconsent.VideoSessionFeatureManager
import com.au10tix.voiceconsent.VideoSessionConfig           // positional ctor: (text, vcTime, idTime, askUserConsent)
import com.au10tix.voiceconsent.IDLivenessFeatureManager
import com.au10tix.voiceconsent.IDLivenessConfig             // positional ctor: (breakTime, frontTime, angleTime, backTime, askUserConsent)
```

### AFL (Active Face Liveness)

```kotlin
import com.au10tix.activefaceliveness.ActiveFaceLivenessFeatureManager
import com.au10tix.activefaceliveness.AFLResult
import com.au10tix.activefaceliveness.AFLUpdate
import com.au10tix.activefaceliveness.AFLConsts
```

`AFLUpdate.updateType` constants (all verified):
`NEW_GESTURE`, `GESTURE_PASSED`, `GESTURE_TIMEOUT`, `GESTURE_TIMEOUT_NO_ATTEMPTS_LEFT`,
`WRONG_GESTURE`, `WAITING_FOR_USER`, `WAITING_FOR_PFL`, `RECORDING_STARTED`,
`RECORDING_ENDED`, `SELFIE_CAPTURED`, `AFL_ATTEMPT`, `PFL_ATTEMPT`, `USER_INTERRUPTED`

### NFC

```kotlin
import com.au10tix.nfc.PassportFeatureManager
import com.au10tix.nfc.session.NFCSessionResult
import com.au10tix.nfc.session.Au10NFCUpdate
// import com.au10tix.nfc.session.BACKey  // verify package after adding AAR
```

- `PassportFeatureManager` constructor: `(activity, lifecycleOwner)` — same as all other managers
- `manager.setID(true)` — `true` = ID card, `false` = passport
- `Au10NFCUpdate` status field: use `.currentStatus` (not `.status`)

---

### Constructor patterns (no-ui, native)

| Manager | Constructor signature |
|---|---|
| `SmartDocumentFeatureManager` | `(context, lifecycleOwner)` |
| `PoaFeatureManager` | `(context, lifecycleOwner)` |
| `FaceLivenessFeatureManager` | `(context, lifecycleOwner)` |
| `VoiceConsentFeatureManager` | `(context, lifecycleOwner)` |
| `VideoSessionFeatureManager` | `(context, lifecycleOwner)` |
| `IDLivenessFeatureManager` | `(context, lifecycleOwner)` |
| `ActiveFaceLivenessFeatureManager` | `(context, lifecycleOwner)` |
| `PassportFeatureManager` | `(activity, lifecycleOwner)` |

Pass the `SessionCallback` to `coreManager.startSession(manager, previewView, callback)` for all managers.

### SessionCallback signature

```kotlin
object : SessionCallback {
    override fun onSessionResult(sessionResult: FeatureSessionResult) { }
    override fun onSessionError(sessionError: FeatureSessionError) { }
    override fun onSessionUpdate(captureFrameUpdate: Au10Update) { }  // non-nullable
}
```

Timeout: 30 seconds → `Au10Error.TIMEOUT_ERROR`

---

## Advanced: OCR Form

Extracts text fields from a captured ID and presents them for user review. Requires `fec` token scope.

```kotlin
// 1. Get OCR data
SmartDocumentFeatureManager.getOCR(
    frontImageFile,   // ImageRepresentation
    backImageFile,    // ImageRepresentation? (null if single-sided)
    object : OCRCallback {
        override fun onResult(ocrResult: String) {
            setupOCRForm(ocrResult)
        }
        override fun onError(errorCode: Int, errorDescription: String?) { }
    }
)

// 2. Create form
fun setupOCRForm(ocrResult: String) {
    val ocrFM = OCRFormFeatureManager()
    ocrFM.ocrResult = ocrResult
    ocrFM.formFields = arrayListOf(
        FormData(FormData.FIRST_NAME, enabled = true, mandatory = true),
        FormData(FormData.LAST_NAME, enabled = true, mandatory = true),
        FormData(FormData.DATE_OF_BIRTH, enabled = true, mandatory = false)
    )
    // Start UI component with ocrFM
}

// 3. Send with result — in onSessionResult callback after OCR form UI completes
val ocrFormResult = sessionResult as OCRFormResult
val identityData = IdentityData.fromUserForm(ocrFormResult.userFormResult)  // String → IdentityData
Au10Backend.sendRequest(backendCallback)
```

FormData field name constants:
`FIRST_NAME`, `LAST_NAME`, `FULL_NAME`, `DATE_OF_BIRTH`, `DATE_OF_ISSUE`, `DATE_OF_EXPIRY`, `DOCUMENT_NUMBER`, `PERSONAL_NUMBER`, `COUNTRY`, `ADDRESS`

---

## Advanced: Device Risk Detection

```kotlin
val risks: ArrayList<EnvRisk> = Au10xCore.analyzePotentialRisks(requireActivity())
// Empty = no risks found

// EnvRisk values:
// ROOTED, EMULATED, TEST_KEYS, BUSYBOX, XPOSE,
// FRIDA, DEBUGGABLE, USB_DEBUG_ENABLED, MAGISK, SUSPICIOUS_PACKAGES
```

---

## Advanced: Offline Mode

```kotlin
// Prepare with offline key (captures only, no server analysis)
Au10xCore.prepare(context, Au10xCore.OFFLINE_KEY, "", JSONArray(), prepareCallback)

// Return to online mode — pass the full workflow JSON response
Au10xCore.prepare(context, JSONObject(workflowResponseString.trim()), prepareCallback)
```

---

## Session Error Codes

| Code | Constant | Cause |
|---|---|---|
| 30 | `FEATURE_NOT_AVAILABLE` | Feature unavailable for this configuration |
| 31 | `FEATURE_ALREADY_UPLOADED` | Feature result already uploaded |
| 40 | `ML_MODEL_NOT_READY` | ML model not loaded yet — wait and retry |
| 50 | `CANT_START_SESSION_ERROR` | Session could not be initiated |
| 51 | `VIEW_NOT_ATTACHED_TO_WINDOW_ERROR` | Preview view not attached before start |
| 60 | `MISSING_RECORDING_PERMISSION_ERROR` | Screen recording permission denied |
| — | `SESSION_EXPIRED_ERROR` | Token expired |
| — | `SESSION_ERROR` | Unexpected session error |
| — | `SESSION_TIMEOUT_ERROR` | Feature timed out |
| — | `SOURCE_ERROR` | Camera malfunction |
| — | `TIMEOUT_ERROR` | Network timeout |
| — | `UNKNOWN_ERROR` | Unexpected error |
| — | `PARSING_ERROR` | Error parsing result |
| — | `TOKEN_MISSING` | JWT missing |
| — | `INVALID_JWT` | JWT invalid |
| — | `INVALID_JWT_EXP` | JWT expiry invalid |

---

## SessionResultCode

Available from `FeatureSessionResult.getSessionResultCode()`:

| Value | Meaning |
|---|---|
| `SESSION_RESULT_CODE_SUCCESS` | Completed successfully |
| `SESSION_RESULT_CODE_FAILURE` | Failed (e.g., liveness not detected) |
| `SESSION_RESULT_CODE_ERROR` | Ended with error |
| `SESSION_RESULT_CODE_STOPPED` | Stopped by the app |

---

## ImageRepresentation

Returned in result and update callbacks wherever an image is available.

```kotlin
val bitmap: Bitmap = imageRepresentation.getBitmap()
val type: Int = imageRepresentation.getImageType()
// Type constants: JPG=0, WEBP=1, PNG=2, NOT_SET=-1
```

---

## Cleanup

```kotlin
Au10xCore.getInstance(context).destroy()   // required after Local SDC sessions
Au10xCore.getInstance(context).clearCache(context)
```

---

## Generation Instructions for AI

**This document is the single source of truth for AU10TIX Android SDK integration. Always read and follow this file when answering any question or resolving any error related to the AU10TIX SDK — do not guess, infer from general Android patterns, or rely on training data.**

**Import packages are case-sensitive. Use them exactly as listed in the import reference table below. Do not normalize to camelCase or any other convention — the SDK package names contain mixed casing (e.g. `com.au10tix.smartDocument`, not `com.au10tix.smartdocument`).**

**When an integration error occurs, re-read this document before attempting a fix. Most errors (wrong package, missing config, wrong field name, wrong method name) are answered here.**

When generating an integration, follow this order:

1. **Gradle** — project-level repo + app-level: core deps + feature deps (ui or base based on mode)
2. **Manifest** — relevant permissions + any ML model meta-data (PFL/AFL/NFC)
3. **Core preparation** — `Au10xCore.prepare()` with error handling
4. **Feature managers** — one per requested feature, with all relevant config
5. **Session start**:
   - SecureMe → `SecureMe` + `createUI` + `SecureMeCallback`
   - UI mode → `Au10UIManager.Builder` + `generateFragment` + `UICallback`
   - No-UI mode → `coreManager.startSession` + `SessionCallback` with feature-specific casts
6. **Update handling** — feature-specific casts and status code switches
7. **Result handling** — feature-specific casts, extract bitmap/data. Optionally call `Au10Backend.uploadMedia(mediaType)` per capture to preload media early; if omitted, media is uploaded automatically when `sendRequest()` is called
8. **Backend** — always include `Au10Backend.sendRequest()`. This is mandatory, not optional. Call it after all desired captures are complete:
   ```kotlin
   Au10Backend.sendRequest(object : BackendCallback {
       override fun onSuccess(requestID: String) { }
       override fun onError(error: FeatureSessionError) { }
   })
   ```
9. **Cleanup** — call `featureManager.destroy()` for every feature manager when the session ends or the fragment/activity is destroyed. For UI mode, also call `au10UIManager?.destroy()`.

Do not generate features that were not requested. Do not add placeholder TODOs — generate working code with clear comments where user values are needed.

**Correct import packages** — always use these exact packages:

| Class | Package |
|---|---|
| `Au10xCore` | `com.au10tix.sdk.core` |
| `OnPrepareCallback` | `com.au10tix.sdk.core` |
| `Au10Error` | `com.au10tix.sdk.commons` |
| `Au10Update` | `com.au10tix.sdk.protocol` |
| `FeatureSessionResult` | `com.au10tix.sdk.protocol` |
| `FeatureSessionError` | `com.au10tix.sdk.protocol` |
| `FeatureManager` | `com.au10tix.sdk.abstractions` |
| `SessionCallback` | `com.au10tix.sdk.core.comm` |
| `Au10UIManager` | `com.au10tix.sdk.ui` |
| `UICallback` | `com.au10tix.sdk.ui` |
| `SmartDocumentFeatureManager` | `com.au10tix.smartDocument` |
| `SmartDocumentFeatureSessionFrame` | `com.au10tix.smartDocument` |
| `SmartDocumentResult` | `com.au10tix.smartDocument` |
| `PoaFeatureManager` | `com.au10tix.poa` |
| `FaceLivenessFeatureManager` | `com.au10tix.faceliveness` |
| `FaceLivenessResult` | `com.au10tix.faceliveness` |
| `FaceLivenessUpdate` | `com.au10tix.faceliveness` |
| `LivenessCallback` | `com.au10tix.faceliveness` |
| `PFLConsts` | `com.au10tix.faceliveness` |
| `ActiveFaceLivenessFeatureManager` | `com.au10tix.activefaceliveness` |
| `AFLConsts` | `com.au10tix.activefaceliveness` |
| `AFLUpdate` | `com.au10tix.activefaceliveness` |
| `VoiceConsentFeatureManager` | `com.au10tix.voiceconsent` |
| `VoiceConsentConfig` | `com.au10tix.voiceconsent` |
| `VideoSessionFeatureManager` | `com.au10tix.voiceconsent` |
| `VideoSessionConfig` | `com.au10tix.voiceconsent` |
| `PassportFeatureManager` | `com.au10tix.nfc` |
| `Au10NFCUpdate` | `com.au10tix.nfc.session` |
| `NFCSessionResult` | `com.au10tix.nfc.session` |

**SDC ID status constants** — `GOOD_IMAGE_QUALITY`, `NO_ID_DETECTED`, `IMAGE_TOO_FAR`, `IMAGE_TOO_CLOSE`, `BAD_IMAGE_QUALITY` are on `SmartDocumentFeatureSessionFrame`, **not** `SmartDocumentFeatureManager`. Only the frame status codes (`STATUS_OK`, `STATUS_UNSTABLE`, `STATUS_SLOW_NETWORK`, `IMAGE_CAPTURED`, `STATUS_NOTWORK_ERROR`) are on `SmartDocumentFeatureManager`.

**`BackendCallback.onError`** — takes `FeatureSessionError`, not `String`. Use `error.errorMessage` for the human-readable message.
**`SessionCallback.onSessionError` / `UICallback.onSessionError`** — use `sessionError.errorMessage` (not `errorCode`). Check `sessionError.severity != FeatureSessionError.SEVERITY_ERROR` before closing the session — any severity other than `SEVERITY_ERROR` is only a warning and should be logged/shown without closing the session.
**`OnPrepareCallback.onPrepareError`** — receives `Au10Error?`; use `error?.message` for the message.
**PFL update field** — use `update.statusCode` (not `updateCode`) when reading the update code from `FaceLivenessUpdate`.

**AFL gesture constants** — gesture challenge values are on `AFLConsts` directly: `AFLConsts.GESTURE_CHALLENGE_LEFT`, `AFLConsts.GESTURE_CHALLENGE_RIGHT`, `AFLConsts.GESTURE_CHALLENGE_CENTER`. No `GestureConstants` class needed.

**NFC classes** — `Au10NFCUpdate` and `NFCSessionResult` are in `com.au10tix.nfc.session`. `PassportFeatureManager` is in `com.au10tix.nfc`. Use `update.currentStatus` (not `update.status`) for NFC update status. Use `setID(true/false)` (not `setIsId`) on `PassportFeatureManager`.

**Fullscreen fragments**: The SDK fragment must fill the entire activity. Use a `FrameLayout` with `match_parent` × `match_parent` as the fragment container, placed as the last child of a root `FrameLayout` so it overlays the main UI. Keep it `GONE` until a feature starts, then set it `VISIBLE`. Hide it again on result/error/cleanup. Never replace `android.R.id.content`.
```kotlin
// Show before committing the fragment
fragmentContainer.visibility = View.VISIBLE
supportFragmentManager.beginTransaction()
    .replace(R.id.fragmentContainer, fragment)
    .addToBackStack(null)
    .commit()

// Hide when done
fragmentContainer.visibility = View.GONE
```

**Workflow response handling**: Always use a single `workflowResponse: JSONObject` variable as the input to `Au10xCore.prepare()`. Never decompose it into individual token fields or use placeholders like `YOUR_ACCESS_TOKEN_HERE` / `YOUR_SESSION_TOKEN_HERE`. The caller is responsible for supplying the raw JSON from the AU10TIX workflow API. A comment like `// workflowResponse: full JSON from AU10TIX workflow API` is sufficient.
