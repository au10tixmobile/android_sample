package com.au10tix.integration.sample.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.au10tix.integration.sample.features.FeatureConfig
import com.au10tix.integration.sample.features.FeatureType
import com.au10tix.integration.sample.sdk.Au10tixSdkManager
import com.au10tix.integration.sample.ui.components.BannerType
import com.au10tix.integration.sample.ui.components.FeatureCard
import com.au10tix.integration.sample.ui.components.InfoBanner
import com.au10tix.integration.sample.ui.components.LoadingOverlay
import com.au10tix.integration.sample.ui.components.SdkStatusHeader
import com.au10tix.integration.sample.ui.theme.Au10tixBlue
import com.au10tix.integration.sample.ui.theme.BackgroundCard
import com.au10tix.integration.sample.ui.theme.BackgroundDark
import com.au10tix.integration.sample.ui.theme.Separator
import com.au10tix.integration.sample.ui.theme.TextPrimary

@Composable
fun MainScreen(
    sdkManager: Au10tixSdkManager,
    onFeatureLaunched: (FeatureType, FeatureConfig) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isInitialized by sdkManager.isInitialized.collectAsState()
    val isLoading by sdkManager.isLoading.collectAsState()
    val sessionId by sdkManager.sessionId.collectAsState()
    val errorMessage by sdkManager.errorMessage.collectAsState()

    var workflowToken by rememberSaveable { mutableStateOf("") }
    var configSheetFeature by remember { mutableStateOf<FeatureType?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // App header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Au10tix",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Au10tixBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = " Integration Sample",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )
                )
            }

            // SDK status header
            SdkStatusHeader(
                isInitialized = isInitialized,
                sessionId = sessionId,
                onCopySessionId = {
                    sessionId?.let { id ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Session ID", id))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Features grid — fills remaining space
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(FeatureType.entries) { feature ->
                    FeatureCard(
                        feature = feature,
                        isEnabled = isInitialized,
                        onClick = { configSheetFeature = feature }
                    )
                }
            }

            // Bottom section: token input + buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                errorMessage?.let { error ->
                    InfoBanner(message = error, type = BannerType.ERROR)
                }

                if (isLoading) {
                    LoadingOverlay(message = "Initializing SDK…")
                } else if (!isInitialized) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = workflowToken,
                            onValueChange = {
                                workflowToken = it
                                sdkManager.clearError()
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = "Paste workflow response (JSON)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Au10tixBlue,
                                unfocusedBorderColor = Separator,
                                cursorColor = Au10tixBlue
                            )
                        )

                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    workflowToken = clip.getItemAt(0).text?.toString() ?: ""
                                    sdkManager.clearError()
                                }
                            }
                        ) {
                            Text(
                                text = "Paste",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Au10tixBlue
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (workflowToken.isNotBlank()) {
                                scope.launch { sdkManager.initialize(context, workflowToken) }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = workflowToken.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Au10tixBlue,
                            disabledContainerColor = Au10tixBlue.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = "Initialize",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        configSheetFeature?.let { feature ->
            FeatureConfigSheet(
                feature = feature,
                onLaunch = { config ->
                    configSheetFeature = null
                    onFeatureLaunched(feature, config)
                },
                onDismiss = { configSheetFeature = null }
            )
        }
    }
}
