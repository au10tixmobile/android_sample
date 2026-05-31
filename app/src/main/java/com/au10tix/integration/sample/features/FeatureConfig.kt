package com.au10tix.integration.sample.features

sealed class FeatureConfig {

    data class Sdc(
        val isFrontSide: Boolean = true,
        val showIntroScreen: Boolean = true,
        val uploadResult: Boolean = true,
        val showCloseButton: Boolean = true,
        val showCaptureButton: Boolean = true,
        val canUploadFromGallery: Boolean = true,
        val useLocalSdc: Boolean = false,
        val useCustomUi: Boolean = false
    ) : FeatureConfig()

    data class Ocr(
        val showIntroScreen: Boolean = true,
        val showCloseButton: Boolean = true
    ) : FeatureConfig()

    data class Fec(
        val showIntroScreen: Boolean = true,
        val showCloseButton: Boolean = true
    ) : FeatureConfig()

    data class SecureMe(
        val runFrontSdc: Boolean = true,
        val runBackSdc: Boolean = false,
        val runPfl: Boolean = true,
        val runPoa: Boolean = false,
        val runNfc: Boolean = false
    ) : FeatureConfig()

    data class Pfl(
        val pflDelaySecs: Int = 0,
        val recordAudio: Boolean = false,
        val showCloseButton: Boolean = true,
        val useCustomUi: Boolean = false
    ) : FeatureConfig()

    data class Poa(
        val showIntroScreen: Boolean = true,
        val showCloseButton: Boolean = true,
        val useCustomUi: Boolean = false
    ) : FeatureConfig()

    data class Nfc(
        val isId: Boolean = false,
        val showIntroScreen: Boolean = true,
        val showCloseButton: Boolean = true
    ) : FeatureConfig()

    data class Vc(
        val text: String = "I consent to this identity verification process.",
        val maxSessionTime: Int = 20,
        val showIntroScreen: Boolean = true,
        val showCloseButton: Boolean = true
    ) : FeatureConfig()

    data class Vs(
        val text: String = "I consent to this identity verification process.",
        val vcTime: Int = 7,
        val idTime: Int = 5,
        val askUserConsent: Boolean = false,
        val showIntroScreen: Boolean = true,
        val showCloseButton: Boolean = true
    ) : FeatureConfig()

    data class IdThickness(
        val breakTime: Int = 3,
        val frontTime: Int = 8,
        val angleTime: Int = 8,
        val backTime: Int = 8,
        val askUserConsent: Boolean = false,
        val showIntroScreen: Boolean = true,
        val showCloseButton: Boolean = true
    ) : FeatureConfig()

}
