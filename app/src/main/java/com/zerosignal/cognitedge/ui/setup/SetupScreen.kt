package com.zerosignal.cognitedge.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zerosignal.cognitedge.domain.model.ModelDownloadState
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ZeroSignal AI",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Offline Intelligence",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (!isDeviceSupported) {
            Text(
                text = "Unsupported Device",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "This app requires a Qualcomm Snapdragon device with NPU support. " +
                    "Emulators and non-Snapdragon devices are not supported.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        } else {
            when (downloadState) {
                is ModelDownloadState.NotStarted -> {
                    Text(
                        text = "Download Llama 3.2 3B model for NPU inference",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onStartDownload) {
                        Text("Download Model")
                    }
                }

                is ModelDownloadState.Downloading -> {
                    val progress = downloadState.progress
                    val mb = downloadState.bytesDownloaded / 1_000_000
                    Text(
                        text = "Downloading model... ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${mb} MB downloaded",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                is ModelDownloadState.Verifying -> {
                    Text(
                        text = "Verifying model integrity...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                is ModelDownloadState.Initializing -> {
                    Text(
                        text = "Initializing NPU runtime...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                is ModelDownloadState.Ready -> {
                    Text(
                        text = "Ready!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is ModelDownloadState.Error -> {
                    Text(
                        text = "Error: ${downloadState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SetupScreenDownloadingPreview() {
    ZeroSignalTheme {
        SetupScreenContent(
            downloadState = ModelDownloadState.Downloading(0.65f, 1_950_000_000L),
            isDeviceSupported = true,
            onStartDownload = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
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
