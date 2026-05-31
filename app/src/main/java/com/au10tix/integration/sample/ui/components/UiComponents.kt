package com.au10tix.integration.sample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.au10tix.integration.sample.features.FeatureType
import com.au10tix.integration.sample.ui.theme.Au10tixBlue
import com.au10tix.integration.sample.ui.theme.BackgroundCard
import com.au10tix.integration.sample.ui.theme.BackgroundDark
import com.au10tix.integration.sample.ui.theme.GreenReady
import com.au10tix.integration.sample.ui.theme.RedError
import com.au10tix.integration.sample.ui.theme.Separator
import com.au10tix.integration.sample.ui.theme.TextPrimary
import com.au10tix.integration.sample.ui.theme.TextSecondary
import com.au10tix.integration.sample.ui.theme.YellowWarning

@Composable
fun StatusDot(isReady: Boolean) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (isReady) GreenReady else TextSecondary)
    )
}

@Composable
fun SdkStatusHeader(
    isInitialized: Boolean,
    sessionId: String?,
    onCopySessionId: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(BackgroundCard, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status row: dot + label + env/org badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusDot(isReady = isInitialized)
                Text(
                    text = if (isInitialized) "SDK Ready" else "Not Initialized",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isInitialized) GreenReady else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // Session ID row
        if (isInitialized && sessionId != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Session ID",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = sessionId,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    )
                }
                Text(
                    text = "Copy",
                    modifier = Modifier
                        .clickable { onCopySessionId() }
                        .padding(4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Au10tixBlue,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    feature: FeatureType,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isEnabled) BackgroundCard else BackgroundCard.copy(alpha = 0.5f)
    val iconColor = if (isEnabled) Au10tixBlue else TextSecondary
    val titleColor = if (isEnabled) TextPrimary else TextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isEnabled) Separator else Separator.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                maxLines = 2
            )

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                maxLines = 2
            )
        }
    }
}

enum class BannerType { INFO, WARNING, SUCCESS, ERROR }

@Composable
fun InfoBanner(
    message: String,
    type: BannerType = BannerType.INFO,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (type) {
        BannerType.INFO -> Pair(Au10tixBlue.copy(alpha = 0.15f), Au10tixBlue)
        BannerType.WARNING -> Pair(YellowWarning.copy(alpha = 0.15f), YellowWarning)
        BannerType.SUCCESS -> Pair(GreenReady.copy(alpha = 0.15f), GreenReady)
        BannerType.ERROR -> Pair(RedError.copy(alpha = 0.15f), RedError)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontSize = 13.sp)
        )
    }
}

@Composable
fun LoadingOverlay(message: String = "Loading…") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.7f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Au10tixBlue,
                strokeWidth = 2.dp
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
        }
    }
}

@Composable
fun TagBadge(text: String, color: Color = Au10tixBlue) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
