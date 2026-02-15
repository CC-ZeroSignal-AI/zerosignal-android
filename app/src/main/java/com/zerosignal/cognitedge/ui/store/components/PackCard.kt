package com.zerosignal.cognitedge.ui.store.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zerosignal.cognitedge.domain.model.PackDownloadProgress
import com.zerosignal.cognitedge.domain.model.RemotePack
import com.zerosignal.cognitedge.ui.theme.DownloadedGreen
import com.zerosignal.cognitedge.ui.theme.DownloadingAmber
import com.zerosignal.cognitedge.ui.theme.ElectricPurple
import com.zerosignal.cognitedge.ui.theme.GradientEnd
import com.zerosignal.cognitedge.ui.theme.GradientStart
import com.zerosignal.cognitedge.ui.theme.NeonCyan
import com.zerosignal.cognitedge.ui.theme.SurfaceCard
import com.zerosignal.cognitedge.ui.theme.SurfaceElevated

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PackCard(
    pack: RemotePack,
    downloadProgress: PackDownloadProgress?,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDownloading = downloadProgress is PackDownloadProgress.Downloading
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = if (pack.isDownloaded) {
                        listOf(DownloadedGreen.copy(alpha = 0.3f), DownloadedGreen.copy(alpha = 0.1f))
                    } else {
                        listOf(GradientStart.copy(alpha = 0.2f), GradientEnd.copy(alpha = 0.1f))
                    }
                ),
                shape = shape
            )
            .background(SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row: pack name + domain badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pack.packId.replace("-", " ").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = null,
                            tint = ElectricPurple.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pack.domain,
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricPurple.copy(alpha = 0.7f)
                        )
                    }
                }

                // Document count badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${pack.totalDocuments}",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Topic chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pack.topics.forEach { topic ->
                    TopicChip(name = topic.name, count = topic.documentCount)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Download section
            when {
                pack.isDownloaded -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DownloadedGreen.copy(alpha = 0.1f))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = DownloadedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Downloaded",
                            style = MaterialTheme.typography.labelLarge,
                            color = DownloadedGreen
                        )
                    }
                }
                isDownloading -> {
                    val progress = (downloadProgress as PackDownloadProgress.Downloading).progress
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(300),
                        label = "downloadProgress"
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = DownloadingAmber,
                                trackColor = DownloadingAmber.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Downloading... ${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = DownloadingAmber
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${downloadProgress.downloaded}/${downloadProgress.total}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = DownloadingAmber,
                            trackColor = DownloadingAmber.copy(alpha = 0.15f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
                downloadProgress is PackDownloadProgress.Error -> {
                    Text(
                        text = "Error: ${downloadProgress.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DownloadButton(onClick = onDownload)
                }
                else -> {
                    DownloadButton(onClick = onDownload)
                }
            }
        }
    }
}

@Composable
private fun DownloadButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download Pack",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TopicChip(name: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .border(
                width = 0.5.dp,
                color = ElectricPurple.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan.copy(alpha = 0.5f)
        )
    }
}
