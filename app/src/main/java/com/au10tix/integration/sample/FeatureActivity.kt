package com.au10tix.integration.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.au10tix.integration.sample.features.FeatureType
import com.au10tix.faceliveness.FaceLivenessFeatureManager
import com.au10tix.localinfer.utils.LocalSdcManager
import com.au10tix.nfc.NFCFeatureManager
import com.au10tix.poa.PoaFeatureManager
import com.au10tix.sdk.abstractions.FeatureManager
import com.au10tix.sdk.commons.ImageRepresentation
import com.au10tix.sdk.core.Au10xCore
import com.au10tix.sdk.core.comm.SessionCallback
import com.au10tix.sdk.protocol.Au10Update
import com.au10tix.sdk.protocol.FeatureSessionError
import com.au10tix.sdk.protocol.FeatureSessionResult
import com.au10tix.sdk.ui.Au10UIManager
import com.au10tix.sdk.ui.UICallback
import com.au10tix.secureMe.SecureMe
import com.au10tix.secureMe.callback.SecureMeCallback
import com.au10tix.secureMe.callback.SecureMePrepareCallback
import com.au10tix.secureMe.configurations.SMConfig
import com.au10tix.secureMe.configurations.SMFlow
import com.au10tix.smartDocument.FECCallback
import com.au10tix.smartDocument.FECResult
import com.au10tix.smartDocument.SmartDocumentFeatureManager
import com.au10tix.smartDocument.SmartDocumentFeatureSessionFrame
import com.au10tix.smartDocument.ocr.OCRCallback
import com.au10tix.voiceconsent.IDLivenessConfig
import com.au10tix.voiceconsent.IDLivenessFeatureManager
import com.au10tix.voiceconsent.VideoSessionConfig
import com.au10tix.voiceconsent.VideoSessionFeatureManager
import com.au10tix.voiceconsent.VoiceConsentConfig
import com.au10tix.voiceconsent.VoiceConsentFeatureManager

class FeatureActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FEATURE_TYPE = "extra_feature_type"
        const val EXTRA_SDC_FRONT_SIDE = "extra_sdc_front_side"
        const val EXTRA_SDC_SHOW_INTRO = "extra_sdc_show_intro"
        const val EXTRA_SDC_UPLOAD = "extra_sdc_upload"
        const val EXTRA_SDC_USE_LOCAL = "extra_sdc_use_local"
        const val EXTRA_PFL_DELAY_SECS = "extra_pfl_delay_secs"
        const val EXTRA_PFL_RECORD_AUDIO = "extra_pfl_record_audio"
        const val EXTRA_POA_SHOW_INTRO = "extra_poa_show_intro"
        const val EXTRA_NFC_IS_ID = "extra_nfc_is_id"
        const val EXTRA_NFC_SHOW_INTRO = "extra_nfc_show_intro"
        const val EXTRA_VC_TEXT = "extra_vc_text"
        const val EXTRA_VC_MAX_SESSION = "extra_vc_max_session"
        const val EXTRA_VS_TEXT = "extra_vs_text"
        const val EXTRA_VS_VC_TIME = "extra_vs_vc_time"
        const val EXTRA_VS_ID_TIME = "extra_vs_id_time"
        const val EXTRA_VS_ASK_CONSENT = "extra_vs_ask_consent"
        const val EXTRA_IDT_BREAK_TIME = "extra_idt_break_time"
        const val EXTRA_IDT_FRONT_TIME = "extra_idt_front_time"
        const val EXTRA_IDT_ANGLE_TIME = "extra_idt_angle_time"
        const val EXTRA_IDT_BACK_TIME = "extra_idt_back_time"
        const val EXTRA_IDT_ASK_CONSENT = "extra_idt_ask_consent"
        const val EXTRA_UI_SHOW_CLOSE = "extra_ui_show_close"
        const val EXTRA_UI_SHOW_PRIMARY = "extra_ui_show_primary"
        const val EXTRA_UI_CAN_UPLOAD = "extra_ui_can_upload"
        const val EXTRA_USE_CUSTOM_UI = "extra_use_custom_ui"
        const val EXTRA_OCR_SHOW_INTRO = "extra_ocr_show_intro"
        const val EXTRA_FEC_SHOW_INTRO = "extra_fec_show_intro"
        const val EXTRA_SM_FRONT_SDC = "extra_sm_front_sdc"
        const val EXTRA_SM_BACK_SDC = "extra_sm_back_sdc"
        const val EXTRA_SM_PFL = "extra_sm_pfl"
        const val EXTRA_SM_POA = "extra_sm_poa"
        const val EXTRA_SM_NFC = "extra_sm_nfc"
    }

    private var featureManager: FeatureManager? = null
    private lateinit var feature: FeatureType
    private var useCustomUi: Boolean = false

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            startFlow()
        } else {
            Toast.makeText(this, "Required permissions were denied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val featureName = intent.getStringExtra(EXTRA_FEATURE_TYPE) ?: run {
            finish()
            return
        }
        feature = try {
            FeatureType.valueOf(featureName)
        } catch (e: IllegalArgumentException) {
            finish()
            return
        }

        useCustomUi = intent.getBooleanExtra(EXTRA_USE_CUSTOM_UI, false) &&
            feature in setOf(
                FeatureType.SMART_DOCUMENT,
                FeatureType.PASSIVE_FACE_LIVENESS,
                FeatureType.PROOF_OF_ADDRESS
            )
        if (useCustomUi) {
            setContentView(R.layout.activity_feature_custom_ui)
            findViewById<TextView>(R.id.custom_ui_title)?.text = "${feature.title} (Custom UI)"
        } else {
            setContentView(R.layout.activity_feature)
        }

        val missing = requiredPermissions(feature).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            startFlow()
        } else {
            requestPermissionsLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startFlow() {
        if (useCustomUi) {
            launchCustomUi(feature)
            return
        }
        launchSdkUi(feature)
    }

    private fun requiredPermissions(feature: FeatureType): List<String> = when (feature) {
        FeatureType.SMART_DOCUMENT -> listOf(Manifest.permission.CAMERA)
        FeatureType.PASSIVE_FACE_LIVENESS -> buildList {
            add(Manifest.permission.CAMERA)
            if (intent.getBooleanExtra(EXTRA_PFL_RECORD_AUDIO, false)) {
                add(Manifest.permission.RECORD_AUDIO)
            }
        }
        FeatureType.PROOF_OF_ADDRESS -> if (useCustomUi) listOf(Manifest.permission.CAMERA) else emptyList()
        FeatureType.NFC -> emptyList()
        FeatureType.VOICE_CONSENT -> listOf(Manifest.permission.RECORD_AUDIO)
        FeatureType.VIDEO_SESSION -> listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        FeatureType.ID_THICKNESS -> listOf(Manifest.permission.CAMERA)
        FeatureType.OCR -> listOf(Manifest.permission.CAMERA)
        FeatureType.FEC -> listOf(Manifest.permission.CAMERA)
        FeatureType.SECURE_ME -> buildList {
            if (intent.getBooleanExtra(EXTRA_SM_FRONT_SDC, true) ||
                intent.getBooleanExtra(EXTRA_SM_BACK_SDC, false) ||
                intent.getBooleanExtra(EXTRA_SM_PFL, true) ||
                intent.getBooleanExtra(EXTRA_SM_POA, false)
            ) add(Manifest.permission.CAMERA)
            // PFL inside Secure.me may opt into screen recording (OCS-controlled) which needs audio.
            // Request up-front; granular control is not exposed on FeatureConfig.SecureMe.
            if (intent.getBooleanExtra(EXTRA_SM_PFL, true)) {
                add(Manifest.permission.RECORD_AUDIO)
            }
        }
        else -> emptyList()
    }

    private fun buildManagerForFeature(feature: FeatureType): FeatureManager? = when (feature) {
        FeatureType.SMART_DOCUMENT -> SmartDocumentFeatureManager(this, this).also { mgr ->
            mgr.isFrontSide = intent.getBooleanExtra(EXTRA_SDC_FRONT_SIDE, true)
            mgr.isShowIntroScreen = intent.getBooleanExtra(EXTRA_SDC_SHOW_INTRO, true)
            mgr.setUploadResult(intent.getBooleanExtra(EXTRA_SDC_UPLOAD, true))
        }
        FeatureType.OCR -> SmartDocumentFeatureManager(this, this).also { mgr ->
            mgr.isFrontSide = true
            mgr.isShowIntroScreen = intent.getBooleanExtra(EXTRA_OCR_SHOW_INTRO, true)
            mgr.setUploadResult(false)
        }
        FeatureType.FEC -> SmartDocumentFeatureManager(this, this).also { mgr ->
            mgr.isFrontSide = true
            mgr.isShowIntroScreen = intent.getBooleanExtra(EXTRA_FEC_SHOW_INTRO, true)
            mgr.setUploadResult(false)
        }
        FeatureType.PASSIVE_FACE_LIVENESS -> FaceLivenessFeatureManager(this, this).also { mgr ->
            val delaySecs = intent.getIntExtra(EXTRA_PFL_DELAY_SECS, 0)
            if (delaySecs > 0) mgr.setPflDelaySecs(delaySecs)
            mgr.shouldRecordAudio(intent.getBooleanExtra(EXTRA_PFL_RECORD_AUDIO, false))
        }
        FeatureType.PROOF_OF_ADDRESS -> PoaFeatureManager(this, this).also { mgr ->
            mgr.isShowIntroScreen = intent.getBooleanExtra(EXTRA_POA_SHOW_INTRO, true)
        }
        FeatureType.NFC -> NFCFeatureManager(this, this).also { mgr ->
            mgr.setID(intent.getBooleanExtra(EXTRA_NFC_IS_ID, false))
            mgr.isShowIntroScreen = intent.getBooleanExtra(EXTRA_NFC_SHOW_INTRO, true)
        }
        FeatureType.VOICE_CONSENT -> VoiceConsentFeatureManager(this, this).also { mgr ->
            val text = intent.getStringExtra(EXTRA_VC_TEXT)
                ?: "I consent to this identity verification process."
            mgr.setConfig(VoiceConsentConfig(text, intent.getIntExtra(EXTRA_VC_MAX_SESSION, 20)))
        }
        FeatureType.VIDEO_SESSION -> VideoSessionFeatureManager(this, this).also { mgr ->
            val text = intent.getStringExtra(EXTRA_VS_TEXT)
                ?: "I consent to this identity verification process."
            mgr.setConfig(
                VideoSessionConfig(
                    text,
                    intent.getIntExtra(EXTRA_VS_VC_TIME, 7),
                    intent.getIntExtra(EXTRA_VS_ID_TIME, 5),
                    intent.getBooleanExtra(EXTRA_VS_ASK_CONSENT, false)
                )
            )
        }
        FeatureType.ID_THICKNESS -> IDLivenessFeatureManager(this, this).also { mgr ->
            mgr.setConfig(
                IDLivenessConfig(
                    intent.getIntExtra(EXTRA_IDT_BREAK_TIME, 3),
                    intent.getIntExtra(EXTRA_IDT_FRONT_TIME, 8),
                    intent.getIntExtra(EXTRA_IDT_ANGLE_TIME, 8),
                    intent.getIntExtra(EXTRA_IDT_BACK_TIME, 8),
                    intent.getBooleanExtra(EXTRA_IDT_ASK_CONSENT, false)
                )
            )
        }
        FeatureType.SECURE_ME -> null
        FeatureType.BACKEND_SEND -> null
    }

    private fun launchSdkUi(feature: FeatureType) {
        if (feature == FeatureType.SECURE_ME) {
            launchSecureMe()
            return
        }
        LocalSdcManager.useLocally = feature == FeatureType.SMART_DOCUMENT &&
            intent.getBooleanExtra(EXTRA_SDC_USE_LOCAL, false)

        val manager = buildManagerForFeature(feature) ?: return
        featureManager = manager

        val uiManager = Au10UIManager.Builder(
            this,
            manager,
            object : UICallback() {
                override fun onSessionResult(sessionResult: FeatureSessionResult) {
                    when (feature) {
                        FeatureType.OCR -> runOcrOnResult(sessionResult)
                        FeatureType.FEC -> runFecOnResult(sessionResult)
                        FeatureType.SMART_DOCUMENT -> {
                            reportDocReplayVerdict(sessionResult)
                            finish()
                        }
                        else -> finish()
                    }
                }

                override fun onSessionError(sessionError: FeatureSessionError) {
                    Toast.makeText(
                        this@FeatureActivity,
                        sessionError.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    if (sessionError.severity != FeatureSessionError.SEVERITY_WARNING) {
                        finish()
                    }
                }

                override fun onSessionUpdate(captureFrameUpdate: Au10Update) {}

                override fun onFail(result: FeatureSessionResult) {
                    finish()
                }
            }
        ).apply {
            showCloseButton(intent.getBooleanExtra(EXTRA_UI_SHOW_CLOSE, true))
            if (feature == FeatureType.SMART_DOCUMENT) {
                showPrimaryButton(intent.getBooleanExtra(EXTRA_UI_SHOW_PRIMARY, true))
                canUpload(intent.getBooleanExtra(EXTRA_UI_CAN_UPLOAD, true))
            }
        }.build()

        val fragment = uiManager.generateFragment()
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.feature_container, fragment)
                .commit()
        } else {
            Toast.makeText(this, "Failed to start feature — SDK not ready", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Real Custom UI path — follows ai-integration-reference.md "Integration Path C — Custom UI".
     * Drives the feature manager directly through Au10xCore.startSession with a FrameLayout
     * preview container. No Au10UIManager / no SDK-owned fragment. Supports SDC, PFL, POA.
     */
    private fun launchCustomUi(feature: FeatureType) {
        LocalSdcManager.useLocally = feature == FeatureType.SMART_DOCUMENT &&
            intent.getBooleanExtra(EXTRA_SDC_USE_LOCAL, false)

        val manager = buildManagerForFeature(feature) ?: run {
            finish()
            return
        }
        featureManager = manager

        val preview = findViewById<FrameLayout>(R.id.previewContainer)
        val status = findViewById<TextView>(R.id.custom_ui_status)
        val captureBtn = findViewById<Button>(R.id.custom_ui_capture)
        val closeBtn = findViewById<Button>(R.id.custom_ui_close)

        // SDC is the only feature with a meaningful manual capture button in this sample
        captureBtn.visibility =
            if (feature == FeatureType.SMART_DOCUMENT) View.VISIBLE else View.GONE
        captureBtn.setOnClickListener {
            try {
                Au10xCore.getInstance(this).captureStillImage()
            } catch (e: IllegalStateException) {
                Toast.makeText(this, e.message ?: "Capture failed", Toast.LENGTH_SHORT).show()
            }
        }
        closeBtn.setOnClickListener {
            Au10xCore.getInstance(this).stopSession()
            finish()
        }

        val callback = object : SessionCallback {
            override fun onSessionResult(sessionResult: FeatureSessionResult) {
                if (feature == FeatureType.SMART_DOCUMENT) {
                    reportDocReplayVerdict(sessionResult)
                }
                Au10xCore.getInstance(this@FeatureActivity).stopSession()
                finish()
            }

            override fun onSessionError(sessionError: FeatureSessionError) {
                Toast.makeText(
                    this@FeatureActivity,
                    sessionError.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
                if (sessionError.severity != FeatureSessionError.SEVERITY_WARNING) {
                    Au10xCore.getInstance(this@FeatureActivity).stopSession()
                    finish()
                }
            }

            override fun onSessionUpdate(captureFrameUpdate: Au10Update) {
                status.text = describeUpdate(feature, captureFrameUpdate)
            }
        }

        // The SDK needs the preview container laid out before it can attach the camera surface.
        // Posting defers startSession until after the first layout pass.
        preview.post {
            Au10xCore.getInstance(this).startSession(manager, preview, callback)
        }
    }

    private fun describeUpdate(feature: FeatureType, update: Au10Update): String = when (feature) {
        FeatureType.SMART_DOCUMENT -> {
            val frame = update as? SmartDocumentFeatureSessionFrame
            if (frame != null) {
                "SDC status=${frame.status} idStatus=${frame.idStatus}"
            } else {
                "Ready…"
            }
        }
        FeatureType.PASSIVE_FACE_LIVENESS -> "PFL streaming — hold steady"
        FeatureType.PROOF_OF_ADDRESS -> "POA streaming — align document"
        else -> "Streaming…"
    }

    private fun reportDocReplayVerdict(sessionResult: FeatureSessionResult) {
        val frame = sessionResult.frameData as? SmartDocumentFeatureSessionFrame ?: return
        val isAlive = frame.isAlive
        val isPaper = frame.paper
        val isScreen = frame.screen
        if (isAlive == null && isPaper == null && isScreen == null) return
        val verdict = buildString {
            append("Doc Replay — ")
            append("alive=${isAlive ?: "?"}, ")
            append("paper=${isPaper ?: "?"}, ")
            append("screen=${isScreen ?: "?"}")
        }
        Toast.makeText(this, verdict, Toast.LENGTH_LONG).show()
    }

    private fun runOcrOnResult(sessionResult: FeatureSessionResult) {
        val image: ImageRepresentation? = sessionResult.imageRepresentation
        if (image == null) {
            Toast.makeText(this, "OCR: no image in result", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        SmartDocumentFeatureManager.getOCR(image, null, object : OCRCallback {
            override fun onResult(result: String) {
                Toast.makeText(this@FeatureActivity, "OCR: $result", Toast.LENGTH_LONG).show()
                finish()
            }

            override fun onError(errorCode: Int, errorDescription: String?) {
                Toast.makeText(
                    this@FeatureActivity,
                    "OCR error ($errorCode): ${errorDescription ?: "unknown"}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        })
    }

    private fun runFecOnResult(sessionResult: FeatureSessionResult) {
        val image: ImageRepresentation? = sessionResult.imageRepresentation
        if (image == null) {
            Toast.makeText(this, "FEC: no image in result", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        SmartDocumentFeatureManager.getClassification(image, object : FECCallback {
            override fun onResult(result: FECResult) {
                Toast.makeText(
                    this@FeatureActivity,
                    "FEC: ${result.classificationResult ?: "no result"}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }

            override fun onError(errorCode: Int, errorDescription: String?) {
                Toast.makeText(
                    this@FeatureActivity,
                    "FEC error ($errorCode): ${errorDescription ?: "unknown"}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        })
    }

    private fun launchSecureMe() {
        val flow = SMFlow().apply {
            withFrontSDC(intent.getBooleanExtra(EXTRA_SM_FRONT_SDC, true))
            withBackSDC(intent.getBooleanExtra(EXTRA_SM_BACK_SDC, false))
            withPFL(intent.getBooleanExtra(EXTRA_SM_PFL, true))
            withPOA(intent.getBooleanExtra(EXTRA_SM_POA, false))
            withNFC(intent.getBooleanExtra(EXTRA_SM_NFC, false))
        }
        val config = SMConfig()
        SecureMe(this, null, null, null, object : SecureMePrepareCallback {
            override fun onPrepared(secureMe: SecureMe) {
                val ui = secureMe.createUI(flow, config, object : SecureMeCallback {
                    override fun onComplete(result: com.au10tix.secureMe.SecureMeResult?) {
                        Toast.makeText(
                            this@FeatureActivity,
                            "Secure.me complete: ${result?.requestId ?: "no id"}",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }

                    override fun onUpdate(event: String?, result: FeatureSessionResult?) {}

                    override fun onError(error: FeatureSessionError?) {
                        Toast.makeText(
                            this@FeatureActivity,
                            "Secure.me error: ${error?.errorMessage ?: "unknown"}",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                })
                if (ui != null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.feature_container, ui)
                        .commit()
                } else {
                    Toast.makeText(this@FeatureActivity, "Secure.me not ready", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onError(error: com.au10tix.sdk.commons.Au10Error?) {
                Toast.makeText(
                    this@FeatureActivity,
                    "Secure.me prepare error: ${error?.toString() ?: "unknown"}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        featureManager?.destroy()
        featureManager = null
    }
}
