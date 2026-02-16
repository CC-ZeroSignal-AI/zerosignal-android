package com.zerosignal.cognitedge.ui.setup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zerosignal.cognitedge.domain.model.ModelDownloadState
import com.zerosignal.cognitedge.ui.theme.Accent
import com.zerosignal.cognitedge.ui.theme.AccentSubtle
import com.zerosignal.cognitedge.ui.theme.BorderMuted
import com.zerosignal.cognitedge.ui.theme.BorderSubtle
import com.zerosignal.cognitedge.ui.theme.Error
import com.zerosignal.cognitedge.ui.theme.ErrorSubtle
import com.zerosignal.cognitedge.ui.theme.Obsidian
import com.zerosignal.cognitedge.ui.theme.Surface2
import com.zerosignal.cognitedge.ui.theme.Surface3
import com.zerosignal.cognitedge.ui.theme.TextPrimary
import com.zerosignal.cognitedge.ui.theme.TextQuaternary
import com.zerosignal.cognitedge.ui.theme.TextSecondary
import com.zerosignal.cognitedge.ui.theme.TextTertiary
import com.zerosignal.cognitedge.ui.theme.ZeroSignalTheme

@Composable
fun SetupScreen(
    onContinue: () -> Unit = {},
    viewModel: SetupViewModel = hiltViewModel()
) {
    val downloadState by viewModel.downloadState.collectAsState()
    val isDeviceSupported by viewModel.isDeviceSupported.collectAsState()

    LaunchedEffect(downloadState) {
        if (downloadState is ModelDownloadState.Ready) {
            onContinue()
        }
    }

    SetupScreenContent(
        downloadState = downloadState,
        isDeviceSupported = isDeviceSupported,
        onStartDownload = { viewModel.startDownload() },
        onRetry = { viewModel.startDownload() }
    )
}

@Composable
private fun SetupScreenContent(
    downloadState: ModelDownloadState,
    isDeviceSupported: Boolean,
    onStartDownload: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        // ── Brand ──
        Text(
            text = "ZeroSignal",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Offline Intelligence",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(36.dp))

        if (!isDeviceSupported) {
            UnsupportedDeviceSection()
        } else {
            when (downloadState) {
                is ModelDownloadState.NotStarted -> {
                    NotStartedSection(onStartDownload = onStartDownload)
                }
                is ModelDownloadState.Downloading -> {
                    DownloadingSection(
                        progress = downloadState.progress,
                        bytesDownloaded = downloadState.bytesDownloaded
                    )
                }
                is ModelDownloadState.Verifying -> {
                    ProgressSection(label = "Verifying model integrity")
                }
                is ModelDownloadState.Initializing -> {
                    ProgressSection(label = "Initializing NPU runtime")
                }
                is ModelDownloadState.Ready -> {
                    ReadySection()
                }
                is ModelDownloadState.Error -> {
                    ErrorSection(
                        message = downloadState.message,
                        onRetry = onRetry
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// NotStarted — the main onboarding view
// ─────────────────────────────────────────────────────────────

@Composable
private fun NotStartedSection(onStartDownload: () -> Unit) {
    // Capability cards
    CapabilityRow(
        icon = Icons.Outlined.WifiOff,
        title = "Completely Offline",
        description = "Runs entirely on your device. No internet needed after setup."
    )
    Spacer(modifier = Modifier.height(16.dp))
    CapabilityRow(
        icon = Icons.Outlined.Shield,
        title = "Private by Design",
        description = "Your conversations never leave your phone. Zero data collection."
    )
    Spacer(modifier = Modifier.height(16.dp))
    CapabilityRow(
        icon = Icons.Outlined.Memory,
        title = "NPU Accelerated",
        description = "Hardware-accelerated inference on Qualcomm Snapdragon NPU."
    )

    Spacer(modifier = Modifier.height(28.dp))
    HorizontalDivider(color = BorderMuted, thickness = 0.5.dp)
    Spacer(modifier = Modifier.height(28.dp))

    // Model spec card
    ModelSpecCard()

    Spacer(modifier = Modifier.height(32.dp))

    // CTA
    Button(
        onClick = onStartDownload,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TextPrimary,
            contentColor = Obsidian
        )
    ) {
        Text(
            "Download Model",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Download requires Wi-Fi. Model is stored locally.",
        style = MaterialTheme.typography.labelMedium,
        color = TextQuaternary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CapabilityRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Surface2)
                .border(0.5.dp, BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun ModelSpecCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface2)
            .border(0.5.dp, BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Psychology,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Model",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        SpecRow(label = "Name", value = "Llama 3.2 3B Instruct")
        Spacer(modifier = Modifier.height(8.dp))
        SpecRow(label = "Provider", value = "Meta  \u00b7  Nexa AI")
        Spacer(modifier = Modifier.height(8.dp))
        SpecRow(label = "Size", value = "~5.5 GB")
        Spacer(modifier = Modifier.height(8.dp))
        SpecRow(label = "Quantization", value = "Q4_0 (4-bit)")
        Spacer(modifier = Modifier.height(8.dp))
        SpecRow(label = "Runtime", value = "NPU + CPU/GPU fallback")
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextQuaternary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Downloading
// ─────────────────────────────────────────────────────────────

@Composable
private fun DownloadingSection(progress: Float, bytesDownloaded: Long) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "downloadProgress"
    )
    val mb = bytesDownloaded / 1_000_000
    val gb = String.format("%.1f", bytesDownloaded / 1_000_000_000.0)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Downloading model",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(32.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Accent,
            trackColor = Surface3,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${gb} GB downloaded",
                style = MaterialTheme.typography.labelMedium,
                color = TextQuaternary
            )
            Text(
                text = "~5.5 GB total",
                style = MaterialTheme.typography.labelMedium,
                color = TextQuaternary
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Context during download
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface2)
                .padding(16.dp)
        ) {
            Text(
                text = "While you wait",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "The AI model is being downloaded and stored locally on your device. Once complete, ZeroSignal works entirely offline — no internet required.",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Verifying / Initializing
// ─────────────────────────────────────────────────────────────

@Composable
private fun ProgressSection(label: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(20.dp))
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Accent,
            trackColor = Surface3,
            strokeCap = StrokeCap.Round
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Ready
// ─────────────────────────────────────────────────────────────

@Composable
private fun ReadySection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(
            text = "Ready",
            style = MaterialTheme.typography.headlineMedium,
            color = Accent,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Error
// ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ErrorSubtle.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = Error,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Download failed",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextPrimary
            )
        ) {
            Text(
                "Retry Download",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Unsupported Device
// ─────────────────────────────────────────────────────────────

@Composable
private fun UnsupportedDeviceSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(ErrorSubtle.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = Error,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Unsupported Device",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ZeroSignal requires hardware AI acceleration to run the language model on-device.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Requirements card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface2)
                .border(0.5.dp, BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Requirements",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            RequirementRow(label = "Chipset", value = "Qualcomm Snapdragon")
            Spacer(modifier = Modifier.height(6.dp))
            RequirementRow(label = "Accelerator", value = "Hexagon NPU")
            Spacer(modifier = Modifier.height(6.dp))
            RequirementRow(label = "Device type", value = "Physical device (no emulators)")
        }
    }
}

@Composable
private fun RequirementRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextQuaternary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun SetupScreenNotStartedPreview() {
    ZeroSignalTheme {
        SetupScreenContent(
            downloadState = ModelDownloadState.NotStarted,
            isDeviceSupported = true,
            onStartDownload = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun SetupScreenDownloadingPreview() {
    ZeroSignalTheme {
        SetupScreenContent(
            downloadState = ModelDownloadState.Downloading(0.42f, 2_310_000_000L),
            isDeviceSupported = true,
            onStartDownload = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun SetupScreenUnsupportedPreview() {
    ZeroSignalTheme {
        SetupScreenContent(
            downloadState = ModelDownloadState.NotStarted,
            isDeviceSupported = false,
            onStartDownload = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun SetupScreenErrorPreview() {
    ZeroSignalTheme {
        SetupScreenContent(
            downloadState = ModelDownloadState.Error("Network connection lost. Check your Wi-Fi and try again."),
            isDeviceSupported = true,
            onStartDownload = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun SetupScreenVerifyingPreview() {
    ZeroSignalTheme {
        SetupScreenContent(
            downloadState = ModelDownloadState.Verifying,
            isDeviceSupported = true,
            onStartDownload = {},
            onRetry = {}
        )
    }
}
