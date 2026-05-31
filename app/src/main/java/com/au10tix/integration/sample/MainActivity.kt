package com.au10tix.integration.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.au10tix.integration.sample.features.FeatureConfig
import com.au10tix.integration.sample.features.FeatureType
import com.au10tix.integration.sample.sdk.Au10tixSdkManager
import com.au10tix.integration.sample.ui.screens.MainScreen
import com.au10tix.integration.sample.ui.theme.Au10tixTheme
import com.au10tix.sdk.core.Au10xCore

class MainActivity : ComponentActivity() {

    private val runtimePermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* results ignored — each feature re-checks before use */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Au10xCore.getInstance(applicationContext)

        val missing = runtimePermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissionsLauncher.launch(missing.toTypedArray())
        }

        setContent {
            Au10tixTheme {
                MainScreen(
                    sdkManager = Au10tixSdkManager,
                    onFeatureLaunched = ::launchFeature
                )
            }
        }
    }

    private fun launchFeature(feature: FeatureType, config: FeatureConfig) {
        val intent = Intent(this, FeatureActivity::class.java).apply {
            putExtra(FeatureActivity.EXTRA_FEATURE_TYPE, feature.name)
            when (config) {
                is FeatureConfig.Sdc -> {
                    putExtra(FeatureActivity.EXTRA_SDC_FRONT_SIDE, config.isFrontSide)
                    putExtra(FeatureActivity.EXTRA_SDC_SHOW_INTRO, config.showIntroScreen)
                    putExtra(FeatureActivity.EXTRA_SDC_UPLOAD, config.uploadResult)
                    putExtra(FeatureActivity.EXTRA_SDC_USE_LOCAL, config.useLocalSdc)
                    putExtra(FeatureActivity.EXTRA_USE_CUSTOM_UI, config.useCustomUi)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_CLOSE, config.showCloseButton)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_PRIMARY, config.showCaptureButton)
                    putExtra(FeatureActivity.EXTRA_UI_CAN_UPLOAD, config.canUploadFromGallery)
                }
                is FeatureConfig.Ocr -> {
                    putExtra(FeatureActivity.EXTRA_OCR_SHOW_INTRO, config.showIntroScreen)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_CLOSE, config.showCloseButton)
                }
                is FeatureConfig.Fec -> {
                    putExtra(FeatureActivity.EXTRA_FEC_SHOW_INTRO, config.showIntroScreen)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_CLOSE, config.showCloseButton)
                }
                is FeatureConfig.SecureMe -> {
                    putExtra(FeatureActivity.EXTRA_SM_FRONT_SDC, config.runFrontSdc)
                    putExtra(FeatureActivity.EXTRA_SM_BACK_SDC, config.runBackSdc)
                    putExtra(FeatureActivity.EXTRA_SM_PFL, config.runPfl)
                    putExtra(FeatureActivity.EXTRA_SM_POA, config.runPoa)
                    putExtra(FeatureActivity.EXTRA_SM_NFC, config.runNfc)
                }
                is FeatureConfig.Pfl -> {
                    putExtra(FeatureActivity.EXTRA_PFL_DELAY_SECS, config.pflDelaySecs)
                    putExtra(FeatureActivity.EXTRA_PFL_RECORD_AUDIO, config.recordAudio)
                    putExtra(FeatureActivity.EXTRA_USE_CUSTOM_UI, config.useCustomUi)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_CLOSE, config.showCloseButton)
                }
                is FeatureConfig.Poa -> {
                    putExtra(FeatureActivity.EXTRA_POA_SHOW_INTRO, config.showIntroScreen)
                    putExtra(FeatureActivity.EXTRA_USE_CUSTOM_UI, config.useCustomUi)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_CLOSE, config.showCloseButton)
                }
                is FeatureConfig.Nfc -> {
                    putExtra(FeatureActivity.EXTRA_NFC_IS_ID, config.isId)
                    putExtra(FeatureActivity.EXTRA_NFC_SHOW_INTRO, config.showIntroScreen)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_CLOSE, config.showCloseButton)
                }
                is FeatureConfig.Vc -> {
                    putExtra(FeatureActivity.EXTRA_VC_TEXT, config.text)
                    putExtra(FeatureActivity.EXTRA_VC_MAX_SESSION, config.maxSessionTime)
                    putExtra(FeatureActivity.EXTRA_VC_SHOW_INTRO, config.showIntroScreen)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_CLOSE, config.showCloseButton)
                }
                is FeatureConfig.Vs -> {
                    putExtra(FeatureActivity.EXTRA_VS_TEXT, config.text)
                    putExtra(FeatureActivity.EXTRA_VS_VC_TIME, config.vcTime)
                    putExtra(FeatureActivity.EXTRA_VS_ID_TIME, config.idTime)
                    putExtra(FeatureActivity.EXTRA_VS_ASK_CONSENT, config.askUserConsent)
                    putExtra(FeatureActivity.EXTRA_VS_SHOW_INTRO, config.showIntroScreen)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_CLOSE, config.showCloseButton)
                }
                is FeatureConfig.IdThickness -> {
                    putExtra(FeatureActivity.EXTRA_IDT_BREAK_TIME, config.breakTime)
                    putExtra(FeatureActivity.EXTRA_IDT_FRONT_TIME, config.frontTime)
                    putExtra(FeatureActivity.EXTRA_IDT_ANGLE_TIME, config.angleTime)
                    putExtra(FeatureActivity.EXTRA_IDT_BACK_TIME, config.backTime)
                    putExtra(FeatureActivity.EXTRA_IDT_ASK_CONSENT, config.askUserConsent)
                    putExtra(FeatureActivity.EXTRA_IDT_SHOW_INTRO, config.showIntroScreen)
                    putExtra(FeatureActivity.EXTRA_UI_SHOW_CLOSE, config.showCloseButton)
                }
            }
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            Au10xCore.getInstance(applicationContext).stopSession()
            Au10tixSdkManager.reset()
        }
    }
}
