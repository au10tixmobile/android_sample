# Feature Mapping Guide

Quick reference for removing features when generating custom projects.

Package: `com.au10tix.integration.sample`
SDK version: defined in `gradle/libs.versions.toml` → `au10tixSdk` (line 10)

---

## Smart Document Capture

**To Remove:**
1. Remove import `Icons.Default.AccountBox` from `features/FeatureType.kt` (line 4) — only if no other feature uses it
2. Delete `SMART_DOCUMENT` enum entry from `features/FeatureType.kt` (lines 22–26)
3. Delete `FeatureConfig.Sdc` class from `features/FeatureConfig.kt` (lines 5–14)
4. Remove `FeatureType.SMART_DOCUMENT` branch from `when` in `ui/screens/FeatureConfigSheet.kt` (line 127)
5. Delete `SdcConfigContent()` composable from `ui/screens/FeatureConfigSheet.kt` (lines 288–376)
6. Remove import `SmartDocumentFeatureManager` from `FeatureActivity.kt` (line 35)
7. Delete `FeatureType.SMART_DOCUMENT` branch from `when` in `FeatureActivity.kt` (lines 176–180)
8. Remove from `app/build.gradle.kts`:
   - `libs.au10tix.smart.document` (line 85) — **only if POA is also removed** (both share this artifact)
   - `libs.au10tix.local.infer` (line 86) — **only if POA is also removed**
   - ML Kit face detection (line 76) — **only if PFL is also removed**
   - ML Kit text recognition (line 77)
   - TFLite java/support (lines 78–79) — **only if PFL is also removed**
9. Remove `android.permission.CAMERA` from `AndroidManifest.xml` — **only if PFL is also removed**
10. Remove `android.hardware.camera` `<uses-feature>` from `AndroidManifest.xml` — **only if PFL is also removed**

---

## Passive Face Liveness

**To Remove:**
1. Remove import `Icons.Default.Face` from `features/FeatureType.kt` (line 7) — only if no other feature uses it
2. Delete `PASSIVE_FACE_LIVENESS` enum entry from `features/FeatureType.kt` (lines 27–31)
3. Delete `FeatureConfig.Pfl` class from `features/FeatureConfig.kt` (lines 34–39)
4. Remove `FeatureType.PASSIVE_FACE_LIVENESS` branch from `when` in `ui/screens/FeatureConfigSheet.kt` (line 128)
5. Delete `PflConfigContent()` composable from `ui/screens/FeatureConfigSheet.kt` (lines 458–509)
6. Remove import `FaceLivenessFeatureManager` from `FeatureActivity.kt` (line 15)
7. Delete `FeatureType.PASSIVE_FACE_LIVENESS` branch from `when` in `FeatureActivity.kt` (lines 191–195)
8. Remove from `app/build.gradle.kts`:
   - `libs.au10tix.face.liveness` (line 84)
   - ML Kit face detection (line 76)
   - TFLite java/support (lines 78–79) — **only if SDC is also removed**
9. Remove `android.permission.CAMERA` from `AndroidManifest.xml` — **only if SDC is also removed**
10. Remove `android.hardware.camera` `<uses-feature>` from `AndroidManifest.xml` — **only if SDC is also removed**
11. Remove `android.permission.RECORD_AUDIO` from `AndroidManifest.xml` — **only if VC and VS are also removed**

---

## Proof of Address

**To Remove:**
1. Remove import `Icons.Default.Home` from `features/FeatureType.kt` (line 8) — only if no other feature uses it
2. Delete `PROOF_OF_ADDRESS` enum entry from `features/FeatureType.kt` (lines 32–36)
3. Delete `FeatureConfig.Poa` class from `features/FeatureConfig.kt` (lines 41–45)
4. Remove `FeatureType.PROOF_OF_ADDRESS` branch from `when` in `ui/screens/FeatureConfigSheet.kt` (line 129)
5. Delete `PoaConfigContent()` composable from `ui/screens/FeatureConfigSheet.kt` (lines 512–536)
6. Remove import `PoaFeatureManager` from `FeatureActivity.kt` (line 18)
7. Delete `FeatureType.PROOF_OF_ADDRESS` branch from `when` in `FeatureActivity.kt` (lines 196–198)
8. Remove from `app/build.gradle.kts`:
   - `libs.au10tix.smart.document` (line 85) — **only if SDC is also removed** (PoaFeatureManager ships inside this artifact)
   - `libs.au10tix.local.infer` (line 86) — **only if SDC is also removed**

---

## NFC

**To Remove:**
1. Remove import `Icons.Default.Nfc` from `features/FeatureType.kt` (line 10)
2. Delete `NFC` enum entry from `features/FeatureType.kt` (lines 37–41)
3. Delete `FeatureConfig.Nfc` class from `features/FeatureConfig.kt` (lines 47–51)
4. Remove `FeatureType.NFC` branch from `when` in `ui/screens/FeatureConfigSheet.kt` (line 130)
5. Delete `NfcConfigContent()` composable from `ui/screens/FeatureConfigSheet.kt` (lines 539–599)
6. Remove import `NFCFeatureManager` from `FeatureActivity.kt` (line 17)
7. Delete `FeatureType.NFC` branch from `when` in `FeatureActivity.kt` (lines 199–202)
8. Remove extras `EXTRA_NFC_IS_ID`, `EXTRA_NFC_SHOW_INTRO` from `FeatureActivity.kt` companion (lines 56–57)
9. Remove `is FeatureConfig.Nfc` branch from `when` in `MainActivity.kt`
10. Remove local AAR references from `app/build.gradle.kts` (lines 50–52): `libs/NFC-release.aar` and `libs/NFC-UI-release.aar`
11. Remove `android.permission.NFC` from `AndroidManifest.xml`

---

## Voice Consent

**Artifact:** `au10tix-voice-consent` (`com.au10tix.sdk:voice-consent`) — **shared with Video Session and ID Thickness**

**To Remove:**
1. Remove import `Icons.Default.Mic` from `features/FeatureType.kt` (line 9) — only if no other feature uses it
2. Delete `VOICE_CONSENT` enum entry from `features/FeatureType.kt` (lines 42–46)
3. Delete `FeatureConfig.Vc` class from `features/FeatureConfig.kt` (lines 53–58)
4. Remove `FeatureType.VOICE_CONSENT` branch from `when` in `ui/screens/FeatureConfigSheet.kt` (line 131)
5. Delete `VcConfigContent()` composable from `ui/screens/FeatureConfigSheet.kt` (lines 602–670)
6. Remove imports `VoiceConsentFeatureManager`, `VoiceConsentConfig` from `FeatureActivity.kt` (lines 42–43)
7. Delete `FeatureType.VOICE_CONSENT` branch from `when` in `FeatureActivity.kt` (lines 203–208)
8. Remove extras `EXTRA_VC_TEXT`, `EXTRA_VC_MAX_SESSION`, `EXTRA_VC_SHOW_INTRO` from `FeatureActivity.kt` companion (lines 58–60)
9. Remove `is FeatureConfig.Vc` branch from `when` in `MainActivity.kt`
10. Remove `libs.au10tix.voice.consent` (line 87) from `app/build.gradle.kts` — **only if VS and ID Thickness are also removed**
11. Remove `au10tix-voice-consent` entry from `gradle/libs.versions.toml` — **only if VS and ID Thickness are also removed**
12. Remove `android.permission.RECORD_AUDIO` from `AndroidManifest.xml` — **only if PFL and VS are also removed**

---

## Video Session

**Artifact:** `au10tix-voice-consent` (`com.au10tix.sdk:voice-consent`) — **shared with Voice Consent and ID Thickness**

**To Remove:**
1. Remove import `Icons.Default.Videocam` from `features/FeatureType.kt` (line 14) — only if no other feature uses it
2. Delete `VIDEO_SESSION` enum entry from `features/FeatureType.kt` (lines 47–51)
3. Delete `FeatureConfig.Vs` class from `features/FeatureConfig.kt` (lines 60–67)
4. Remove `FeatureType.VIDEO_SESSION` branch from `when` in `ui/screens/FeatureConfigSheet.kt` (line 132)
5. Delete `VsConfigContent()` composable from `ui/screens/FeatureConfigSheet.kt` (lines 673–772)
6. Remove imports `VideoSessionFeatureManager`, `VideoSessionConfig` from `FeatureActivity.kt` (lines 40–41)
7. Delete `FeatureType.VIDEO_SESSION` branch from `when` in `FeatureActivity.kt` (lines 209–221)
8. Remove extras `EXTRA_VS_TEXT`, `EXTRA_VS_VC_TIME`, `EXTRA_VS_ID_TIME`, `EXTRA_VS_ASK_CONSENT`, `EXTRA_VS_SHOW_INTRO` from `FeatureActivity.kt` companion (lines 61–65)
9. Remove `is FeatureConfig.Vs` branch from `when` in `MainActivity.kt`
10. Remove `libs.au10tix.voice.consent` (line 87) from `app/build.gradle.kts` — **only if VC and ID Thickness are also removed**
11. Remove `au10tix-voice-consent` entry from `gradle/libs.versions.toml` — **only if VC and ID Thickness are also removed**

---

## ID Thickness

**Artifact:** `au10tix-voice-consent` (`com.au10tix.sdk:voice-consent`) — **shared with Voice Consent and Video Session**

**To Remove:**
1. Remove import `Icons.Default.CreditCard` from `features/FeatureType.kt` (line 6) — only if no other feature uses it
2. Delete `ID_THICKNESS` enum entry from `features/FeatureType.kt` (lines 52–56)
3. Delete `FeatureConfig.IdThickness` class from `features/FeatureConfig.kt` (lines 69–77)
4. Remove `FeatureType.ID_THICKNESS` branch from `when` in `ui/screens/FeatureConfigSheet.kt` (line 133)
5. Delete `IdThicknessConfigContent()` composable from `ui/screens/FeatureConfigSheet.kt` (lines 775–907)
6. Remove imports `IDLivenessFeatureManager`, `IDLivenessConfig` from `FeatureActivity.kt` (lines 38–39)
7. Delete `FeatureType.ID_THICKNESS` branch from `when` in `FeatureActivity.kt` (lines 222–233)
8. Remove extras `EXTRA_IDT_BREAK_TIME`, `EXTRA_IDT_FRONT_TIME`, `EXTRA_IDT_ANGLE_TIME`, `EXTRA_IDT_BACK_TIME`, `EXTRA_IDT_ASK_CONSENT`, `EXTRA_IDT_SHOW_INTRO` from `FeatureActivity.kt` companion (lines 66–71)
9. Remove `is FeatureConfig.IdThickness` branch from `when` in `MainActivity.kt`
10. Remove `libs.au10tix.voice.consent` (line 87) from `app/build.gradle.kts` — **only if VC and VS are also removed**
11. Remove `au10tix-voice-consent` entry from `gradle/libs.versions.toml` — **only if VC and VS are also removed**

---

## Send to Backend

**To Remove:**
1. Remove import `Icons.Default.Upload` from `features/FeatureType.kt` (line 13) — only if no other feature uses it
2. Delete `BACKEND_SEND` enum entry from `features/FeatureType.kt` (lines 72–76)
3. Remove `FeatureType.BACKEND_SEND` branch from `when` in `ui/screens/FeatureConfigSheet.kt` (line 137)
4. Delete `BackendSendContent()` and `AnimatedCheckmark()` composables from `ui/screens/FeatureConfigSheet.kt` (lines 144–285)
5. Remove imports `Au10Backend`, `BackendCallback`, `FeatureSessionError` from `ui/screens/FeatureConfigSheet.kt` (lines 57–59)
6. No `FeatureActivity` changes needed — Backend Send runs entirely inside the bottom sheet
7. No Gradle dependency changes needed — `Au10Backend` ships inside `libs.au10tix.core`

Note: `FeatureConfig` has no `BackendSend` subclass — Backend Send uses no config parameters.

---

## Deletion Rules

**ALWAYS REQUIRED (Never delete):**
- `libs.au10tix.core` (`app/build.gradle.kts` line 83) — required by all features
- `sdk/Au10tixSdkManager.kt` — core SDK initialization
- `AndroidManifest.xml` `INTERNET` permission — required for all network operations

**Shared Artifacts — only delete if ALL dependent features are removed:**
- `au10tix-smart-document` — required by SDC, POA
- `au10tix-local-infer` — required by SDC, POA
- `au10tix-face-liveness` — required by PFL only
- NFC local AARs (`libs/NFC-release.aar`, `libs/NFC-UI-release.aar`) — required by NFC only
- `au10tix-voice-consent` — required by VC, VS, ID Thickness

**Shared ML Kit Dependencies (`app/build.gradle.kts` lines 76–79):**
- `libs.play.services.mlkit.face.detection` (line 76) — required by SDC, PFL
- `libs.play.services.mlkit.text.recognition` (line 77) — required by SDC only
- `libs.play.services.tflite.java` (line 78) — required by SDC, PFL
- `libs.play.services.tflite.support` (line 79) — required by SDC, PFL

**Shared Permissions (`AndroidManifest.xml`):**
- `CAMERA` — required by SDC, PFL
- `NFC` — required by NFC only
- `RECORD_AUDIO` — required by PFL (audio recording option), VC, VS
- `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` — required by POA
- `android.hardware.camera` (`<uses-feature>`) — required by SDC, PFL

**Note on PoaFeatureManager:** Ships inside the `smart-document` artifact (`com.au10tix.sdk:smart-document`). Remove `au10tix-smart-document` only when both SDC and POA are removed.

**Note on Voice Consent / Video Session / ID Thickness:** All three feature managers (`VoiceConsentFeatureManager`, `VideoSessionFeatureManager`, `IDLivenessFeatureManager`) ship inside the single `voice-consent` artifact (`com.au10tix.sdk:voice-consent`). Remove `au10tix-voice-consent` only when all three are removed.

**Note on NFC:** NFC ships as local AAR files (`libs/NFC-release.aar`, `libs/NFC-UI-release.aar`), not via Maven.
