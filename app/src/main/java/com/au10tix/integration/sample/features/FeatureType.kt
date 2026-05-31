package com.au10tix.integration.sample.features

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

enum class FeatureType(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    SMART_DOCUMENT(
        title = "Smart Document Capture",
        description = "Capture and verify ID documents",
        icon = Icons.Default.AccountBox
    ),
    PASSIVE_FACE_LIVENESS(
        title = "Passive Face Liveness",
        description = "Liveness detection without user action",
        icon = Icons.Default.Face
    ),
    PROOF_OF_ADDRESS(
        title = "Proof of Address",
        description = "Capture utility bills and bank statements",
        icon = Icons.Default.Home
    ),
    NFC(
        title = "NFC",
        description = "Read chip data from NFC-enabled documents",
        icon = Icons.Default.Nfc
    ),
    VOICE_CONSENT(
        title = "Voice Consent",
        description = "Record spoken consent from the user",
        icon = Icons.Default.Mic
    ),
    VIDEO_SESSION(
        title = "Video Session",
        description = "Record voice consent and ID presentation",
        icon = Icons.Default.Videocam
    ),
    ID_THICKNESS(
        title = "ID Thickness",
        description = "Verify document depth via motion capture",
        icon = Icons.Default.CreditCard
    ),
    OCR(
        title = "OCR",
        description = "Read text fields from a captured ID",
        icon = Icons.Default.TextFields
    ),
    FEC(
        title = "Front End Classification",
        description = "Classify the document type of a captured image",
        icon = Icons.Default.Category
    ),
    SECURE_ME(
        title = "Secure.me Wrapper",
        description = "Run an orchestrated multi-feature flow",
        icon = Icons.Default.Shield
    ),
    BACKEND_SEND(
        title = "Send to Backend",
        description = "Upload captured data for processing",
        icon = Icons.Default.Upload
    )
}
