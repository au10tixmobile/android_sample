package com.au10tix.integration.sample.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.au10tix.sdk.backend.Au10Backend
import com.au10tix.sdk.backend.BackendCallback
import com.au10tix.sdk.protocol.FeatureSessionError
import com.au10tix.integration.sample.features.FeatureConfig
import com.au10tix.integration.sample.features.FeatureType
import com.au10tix.integration.sample.ui.components.BannerType
import com.au10tix.integration.sample.ui.components.InfoBanner
import com.au10tix.integration.sample.ui.components.LoadingOverlay
import com.au10tix.integration.sample.ui.components.TagBadge
import com.au10tix.integration.sample.ui.theme.Au10tixBlue
import com.au10tix.integration.sample.ui.theme.BackgroundCard
import com.au10tix.integration.sample.ui.theme.BackgroundDark
import com.au10tix.integration.sample.ui.theme.GreenReady
import com.au10tix.integration.sample.ui.theme.Separator
import com.au10tix.integration.sample.ui.theme.TextPrimary
import com.au10tix.integration.sample.ui.theme.TextSecondary
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureConfigSheet(
    feature: FeatureType,
    onLaunch: (FeatureConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun launchAndDismiss(config: FeatureConfig) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onLaunch(config) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundCard,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Separator) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            when (feature) {
                FeatureType.SMART_DOCUMENT -> SdcConfigContent(::launchAndDismiss)
                FeatureType.PASSIVE_FACE_LIVENESS -> PflConfigContent(::launchAndDismiss)
                FeatureType.PROOF_OF_ADDRESS -> PoaConfigContent(::launchAndDismiss)
                FeatureType.NFC -> NfcConfigContent(::launchAndDismiss)
                FeatureType.VOICE_CONSENT -> VcConfigContent(::launchAndDismiss)
                FeatureType.VIDEO_SESSION -> VsConfigContent(::launchAndDismiss)
                FeatureType.ID_THICKNESS -> IdThicknessConfigContent(::launchAndDismiss)
                FeatureType.OCR -> OcrConfigContent(::launchAndDismiss)
                FeatureType.FEC -> FecConfigContent(::launchAndDismiss)
                FeatureType.SECURE_ME -> SecureMeConfigContent(::launchAndDismiss)
                FeatureType.BACKEND_SEND -> BackendSendContent()
            }
        }
    }
}

@Composable
private fun BackendSendContent() {
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var requestId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val callback = remember {
        object : BackendCallback {
            override fun onSuccess(id: String?) {
                mainHandler.post {
                    isLoading = false
                    isSuccess = true
                    requestId = id
                }
            }

            override fun onError(error: FeatureSessionError?) {
                mainHandler.post {
                    isLoading = false
                    errorMessage = error?.errorMessage ?: "An error occurred"
                }
            }
        }
    }

    fun onSendClicked() {
        isLoading = true
        errorMessage = null
        Au10Backend.sendRequest(callback)
    }

    fun onCancelClicked() {
        Au10Backend.cancelRequest()
        isLoading = false
    }

    if (isSuccess) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedCheckmark()
            Text(
                text = "Sent Successfully",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = GreenReady,
                    fontWeight = FontWeight.SemiBold
                )
            )
            requestId?.let { id ->
                Text(
                    text = "Request ID: ${id.take(28)}${if (id.length > 28) "…" else ""}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    } else {
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            InfoBanner(message = msg, type = BannerType.ERROR)
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            LoadingOverlay(message = "Sending data…")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Button(
                onClick = ::onCancelClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Separator)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        } else {
            Button(
                onClick = ::onSendClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Au10tixBlue)
            ) {
                Text(
                    text = "Send",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun AnimatedCheckmark() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(GreenReady),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

@Composable
private fun SdcConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var isFrontSide by remember { mutableStateOf(true) }
    var showIntroScreen by remember { mutableStateOf(true) }
    var uploadResult by remember { mutableStateOf(true) }
    var showCloseButton by remember { mutableStateOf(true) }
    var showCaptureButton by remember { mutableStateOf(true) }
    var canUploadFromGallery by remember { mutableStateOf(true) }
    var useLocalSdc by remember { mutableStateOf(false) }
    var useCustomUi by remember { mutableStateOf(false) }

    ConfigSection(title = "Document Side") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isFrontSide,
                onClick = { isFrontSide = true },
                label = { Text("Front") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Au10tixBlue.copy(alpha = 0.2f),
                    selectedLabelColor = Au10tixBlue,
                    containerColor = BackgroundDark,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isFrontSide,
                    selectedBorderColor = Au10tixBlue,
                    borderColor = Separator
                )
            )
            FilterChip(
                selected = !isFrontSide,
                onClick = { isFrontSide = false },
                label = { Text("Back") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Au10tixBlue.copy(alpha = 0.2f),
                    selectedLabelColor = Au10tixBlue,
                    containerColor = BackgroundDark,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = !isFrontSide,
                    selectedBorderColor = Au10tixBlue,
                    borderColor = Separator
                )
            )
        }
    }

    ConfigSection(title = "Options") {
        ConfigRow(label = "Show Intro Screen", checked = showIntroScreen) { showIntroScreen = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Upload Result", checked = uploadResult) { uploadResult = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Local Classification", checked = useLocalSdc) { useLocalSdc = it }
    }

    ConfigSection(title = "UI") {
        ConfigRow(label = "Show Close Button", checked = showCloseButton) { showCloseButton = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Show Capture Button", checked = showCaptureButton) { showCaptureButton = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Upload from Gallery", checked = canUploadFromGallery) { canUploadFromGallery = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Custom UI Wrapper", checked = useCustomUi) { useCustomUi = it }
    }

    LaunchButton {
        onLaunch(
            FeatureConfig.Sdc(
                isFrontSide = isFrontSide,
                showIntroScreen = showIntroScreen,
                uploadResult = uploadResult,
                showCloseButton = showCloseButton,
                showCaptureButton = showCaptureButton,
                canUploadFromGallery = canUploadFromGallery,
                useLocalSdc = useLocalSdc,
                useCustomUi = useCustomUi
            )
        )
    }
}

@Composable
private fun OcrConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var showIntroScreen by remember { mutableStateOf(true) }
    var showCloseButton by remember { mutableStateOf(true) }

    ConfigSection(title = "Options") {
        ConfigRow(label = "Show Intro Screen", checked = showIntroScreen) { showIntroScreen = it }
    }

    ConfigSection(title = "UI") {
        ConfigRow(label = "Show Close Button", checked = showCloseButton) { showCloseButton = it }
    }

    LaunchButton {
        onLaunch(
            FeatureConfig.Ocr(
                showIntroScreen = showIntroScreen,
                showCloseButton = showCloseButton
            )
        )
    }
}

@Composable
private fun FecConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var showIntroScreen by remember { mutableStateOf(true) }
    var showCloseButton by remember { mutableStateOf(true) }

    ConfigSection(title = "Options") {
        ConfigRow(label = "Show Intro Screen", checked = showIntroScreen) { showIntroScreen = it }
    }

    ConfigSection(title = "UI") {
        ConfigRow(label = "Show Close Button", checked = showCloseButton) { showCloseButton = it }
    }

    LaunchButton {
        onLaunch(
            FeatureConfig.Fec(
                showIntroScreen = showIntroScreen,
                showCloseButton = showCloseButton
            )
        )
    }
}

@Composable
private fun SecureMeConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var runFrontSdc by remember { mutableStateOf(true) }
    var runBackSdc by remember { mutableStateOf(false) }
    var runPfl by remember { mutableStateOf(true) }
    var runPoa by remember { mutableStateOf(false) }
    var runNfc by remember { mutableStateOf(false) }

    ConfigSection(title = "Flow") {
        ConfigRow(label = "Front SDC", checked = runFrontSdc) { runFrontSdc = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Back SDC", checked = runBackSdc) { runBackSdc = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Passive Face Liveness", checked = runPfl) { runPfl = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Proof of Address", checked = runPoa) { runPoa = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "NFC", checked = runNfc) { runNfc = it }
    }

    LaunchButton {
        onLaunch(
            FeatureConfig.SecureMe(
                runFrontSdc = runFrontSdc,
                runBackSdc = runBackSdc,
                runPfl = runPfl,
                runPoa = runPoa,
                runNfc = runNfc
            )
        )
    }
}

@Composable
private fun PflConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var delaySecs by remember { mutableIntStateOf(0) }
    var recordAudio by remember { mutableStateOf(false) }
    var showCloseButton by remember { mutableStateOf(true) }
    var useCustomUi by remember { mutableStateOf(false) }

    ConfigSection(title = "Detection") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Detection Delay",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
            TagBadge(text = "${delaySecs}s")
        }
        Slider(
            value = delaySecs.toFloat(),
            onValueChange = { delaySecs = it.toInt() },
            valueRange = 0f..5f,
            steps = 4,
            colors = SliderDefaults.colors(
                thumbColor = Au10tixBlue,
                activeTrackColor = Au10tixBlue,
                inactiveTrackColor = Separator
            )
        )
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Record Audio (when screen recording enabled)", checked = recordAudio) { recordAudio = it }
    }

    ConfigSection(title = "UI") {
        ConfigRow(label = "Show Close Button", checked = showCloseButton) { showCloseButton = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Custom UI Wrapper", checked = useCustomUi) { useCustomUi = it }
    }

    LaunchButton {
        onLaunch(
            FeatureConfig.Pfl(
                pflDelaySecs = delaySecs,
                recordAudio = recordAudio,
                showCloseButton = showCloseButton,
                useCustomUi = useCustomUi
            )
        )
    }
}

@Composable
private fun PoaConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var showIntroScreen by remember { mutableStateOf(true) }
    var showCloseButton by remember { mutableStateOf(true) }
    var useCustomUi by remember { mutableStateOf(false) }

    ConfigSection(title = "Options") {
        ConfigRow(label = "Show Intro Screen", checked = showIntroScreen) { showIntroScreen = it }
    }

    ConfigSection(title = "UI") {
        ConfigRow(label = "Show Close Button", checked = showCloseButton) { showCloseButton = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Custom UI Wrapper", checked = useCustomUi) { useCustomUi = it }
    }

    LaunchButton {
        onLaunch(
            FeatureConfig.Poa(
                showIntroScreen = showIntroScreen,
                showCloseButton = showCloseButton,
                useCustomUi = useCustomUi
            )
        )
    }
}

@Composable
private fun NfcConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var isId by remember { mutableStateOf(false) }
    var showIntroScreen by remember { mutableStateOf(true) }
    var showCloseButton by remember { mutableStateOf(true) }

    ConfigSection(title = "Document Type") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isId,
                onClick = { isId = false },
                label = { Text("Passport") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Au10tixBlue.copy(alpha = 0.2f),
                    selectedLabelColor = Au10tixBlue,
                    containerColor = BackgroundDark,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = !isId,
                    selectedBorderColor = Au10tixBlue,
                    borderColor = Separator
                )
            )
            FilterChip(
                selected = isId,
                onClick = { isId = true },
                label = { Text("ID Card") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Au10tixBlue.copy(alpha = 0.2f),
                    selectedLabelColor = Au10tixBlue,
                    containerColor = BackgroundDark,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isId,
                    selectedBorderColor = Au10tixBlue,
                    borderColor = Separator
                )
            )
        }
    }

    ConfigSection(title = "Options") {
        ConfigRow(label = "Show Intro Screen", checked = showIntroScreen) { showIntroScreen = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Show Close Button", checked = showCloseButton) { showCloseButton = it }
    }

    LaunchButton {
        onLaunch(FeatureConfig.Nfc(isId = isId, showIntroScreen = showIntroScreen, showCloseButton = showCloseButton))
    }
}

@Composable
private fun VcConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var text by remember { mutableStateOf("I consent to this identity verification process.") }
    var maxSessionTime by remember { mutableIntStateOf(20) }
    var showIntroScreen by remember { mutableStateOf(true) }
    var showCloseButton by remember { mutableStateOf(true) }

    ConfigSection(title = "Consent Text") {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            minLines = 2,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = Au10tixBlue,
                unfocusedBorderColor = Separator,
                cursorColor = Au10tixBlue
            )
        )
    }

    ConfigSection(title = "Recording") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Max Session Time",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
            TagBadge(text = "${maxSessionTime}s")
        }
        Slider(
            value = maxSessionTime.toFloat(),
            onValueChange = { maxSessionTime = it.toInt() },
            valueRange = 5f..30f,
            steps = 24,
            colors = SliderDefaults.colors(
                thumbColor = Au10tixBlue,
                activeTrackColor = Au10tixBlue,
                inactiveTrackColor = Separator
            )
        )
    }

    ConfigSection(title = "Options") {
        ConfigRow(label = "Show Intro Screen", checked = showIntroScreen) { showIntroScreen = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Show Close Button", checked = showCloseButton) { showCloseButton = it }
    }

    LaunchButton {
        onLaunch(
            FeatureConfig.Vc(
                text = text,
                maxSessionTime = maxSessionTime,
                showIntroScreen = showIntroScreen,
                showCloseButton = showCloseButton
            )
        )
    }
}

@Composable
private fun VsConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var text by remember { mutableStateOf("I consent to this identity verification process.") }
    var vcTime by remember { mutableIntStateOf(7) }
    var idTime by remember { mutableIntStateOf(5) }
    var askUserConsent by remember { mutableStateOf(false) }
    var showIntroScreen by remember { mutableStateOf(true) }
    var showCloseButton by remember { mutableStateOf(true) }

    ConfigSection(title = "Consent Text") {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            minLines = 2,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = Au10tixBlue,
                unfocusedBorderColor = Separator,
                cursorColor = Au10tixBlue
            )
        )
    }

    ConfigSection(title = "Timing") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Voice Consent Duration",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
            TagBadge(text = "${vcTime}s")
        }
        Slider(
            value = vcTime.toFloat(),
            onValueChange = { vcTime = it.toInt() },
            valueRange = 4f..30f,
            steps = 25,
            colors = SliderDefaults.colors(
                thumbColor = Au10tixBlue,
                activeTrackColor = Au10tixBlue,
                inactiveTrackColor = Separator
            )
        )
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ID Presentation Duration",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
            TagBadge(text = "${idTime}s")
        }
        Slider(
            value = idTime.toFloat(),
            onValueChange = { idTime = it.toInt() },
            valueRange = 4f..30f,
            steps = 25,
            colors = SliderDefaults.colors(
                thumbColor = Au10tixBlue,
                activeTrackColor = Au10tixBlue,
                inactiveTrackColor = Separator
            )
        )
    }

    ConfigSection(title = "Options") {
        ConfigRow(label = "Ask User Consent", checked = askUserConsent) { askUserConsent = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Show Intro Screen", checked = showIntroScreen) { showIntroScreen = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Show Close Button", checked = showCloseButton) { showCloseButton = it }
    }

    LaunchButton {
        onLaunch(
            FeatureConfig.Vs(
                text = text,
                vcTime = vcTime,
                idTime = idTime,
                askUserConsent = askUserConsent,
                showIntroScreen = showIntroScreen,
                showCloseButton = showCloseButton
            )
        )
    }
}

@Composable
private fun IdThicknessConfigContent(onLaunch: (FeatureConfig) -> Unit) {
    var breakTime by remember { mutableIntStateOf(3) }
    var frontTime by remember { mutableIntStateOf(8) }
    var angleTime by remember { mutableIntStateOf(8) }
    var backTime by remember { mutableIntStateOf(8) }
    var askUserConsent by remember { mutableStateOf(false) }
    var showIntroScreen by remember { mutableStateOf(true) }
    var showCloseButton by remember { mutableStateOf(true) }

    ConfigSection(title = "Timing") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Break Time",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
            TagBadge(text = "${breakTime}s")
        }
        Slider(
            value = breakTime.toFloat(),
            onValueChange = { breakTime = it.toInt() },
            valueRange = 1f..6f,
            steps = 4,
            colors = SliderDefaults.colors(
                thumbColor = Au10tixBlue,
                activeTrackColor = Au10tixBlue,
                inactiveTrackColor = Separator
            )
        )
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Front Side Duration",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
            TagBadge(text = "${frontTime}s")
        }
        Slider(
            value = frontTime.toFloat(),
            onValueChange = { frontTime = it.toInt() },
            valueRange = 1f..15f,
            steps = 13,
            colors = SliderDefaults.colors(
                thumbColor = Au10tixBlue,
                activeTrackColor = Au10tixBlue,
                inactiveTrackColor = Separator
            )
        )
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Angle Side Duration",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
            TagBadge(text = "${angleTime}s")
        }
        Slider(
            value = angleTime.toFloat(),
            onValueChange = { angleTime = it.toInt() },
            valueRange = 1f..15f,
            steps = 13,
            colors = SliderDefaults.colors(
                thumbColor = Au10tixBlue,
                activeTrackColor = Au10tixBlue,
                inactiveTrackColor = Separator
            )
        )
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Back Side Duration",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
            TagBadge(text = "${backTime}s")
        }
        Slider(
            value = backTime.toFloat(),
            onValueChange = { backTime = it.toInt() },
            valueRange = 1f..15f,
            steps = 13,
            colors = SliderDefaults.colors(
                thumbColor = Au10tixBlue,
                activeTrackColor = Au10tixBlue,
                inactiveTrackColor = Separator
            )
        )
    }

    ConfigSection(title = "Options") {
        ConfigRow(label = "Ask User Consent", checked = askUserConsent) { askUserConsent = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Show Intro Screen", checked = showIntroScreen) { showIntroScreen = it }
        HorizontalDivider(color = Separator.copy(alpha = 0.3f))
        ConfigRow(label = "Show Close Button", checked = showCloseButton) { showCloseButton = it }
    }

    LaunchButton {
        onLaunch(
            FeatureConfig.IdThickness(
                breakTime = breakTime,
                frontTime = frontTime,
                angleTime = angleTime,
                backTime = backTime,
                askUserConsent = askUserConsent,
                showIntroScreen = showIntroScreen,
                showCloseButton = showCloseButton
            )
        )
    }
}

@Composable
private fun ConfigSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        ),
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp)
    ) {
        content()
    }
}

@Composable
private fun ConfigRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = Au10tixBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = Separator
            )
        )
    }
}

@Composable
private fun LaunchButton(onClick: () -> Unit) {
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Au10tixBlue)
    ) {
        Text(
            text = "Launch",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
