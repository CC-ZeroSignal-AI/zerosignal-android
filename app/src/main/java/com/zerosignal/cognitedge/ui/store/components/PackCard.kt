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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Description
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zerosignal.cognitedge.domain.model.PackDownloadProgress
import com.zerosignal.cognitedge.domain.model.RemotePack
import com.zerosignal.cognitedge.ui.theme.Accent
import com.zerosignal.cognitedge.ui.theme.BorderSubtle
import com.zerosignal.cognitedge.ui.theme.Error
import com.zerosignal.cognitedge.ui.theme.Obsidian
import com.zerosignal.cognitedge.ui.theme.Success
import com.zerosignal.cognitedge.ui.theme.SuccessSubtle
import com.zerosignal.cognitedge.ui.theme.Surface2
import com.zerosignal.cognitedge.ui.theme.Surface3
import com.zerosignal.cognitedge.ui.theme.TextPrimary
import com.zerosignal.cognitedge.ui.theme.TextQuaternary
import com.zerosignal.cognitedge.ui.theme.TextSecondary
import com.zerosignal.cognitedge.ui.theme.TextTertiary
import com.zerosignal.cognitedge.ui.theme.Warning

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PackCard(
    pack: RemotePack,
    downloadProgress: PackDownloadProgress?,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDownloading = downloadProgress is PackDownloadProgress.Downloading
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(shape)
            .border(
                width = 0.5.dp,
                color = if (pack.isDownloaded) Success.copy(alpha = 0.2f)
                else BorderSubtle.copy(alpha = 0.5f),
                shape = shape
            )
            .background(Surface2)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row: pack name + doc count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pack.packId
                            .replace("-", " ")
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = pack.domain,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary
                    )
                }

                // Document count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Surface3)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${pack.totalDocuments}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(SuccessSubtle.copy(alpha = 0.25f))
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Downloaded",
                            style = MaterialTheme.typography.labelLarge,
                            color = Success
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
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 1.5.dp,
                                color = Warning,
                                trackColor = Warning.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Downloading ${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${downloadProgress.downloaded}/${downloadProgress.total}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextQuaternary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Warning,
                            trackColor = Warning.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
                downloadProgress is PackDownloadProgress.Error -> {
                    Text(
                        text = downloadProgress.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
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
            .height(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TextPrimary,
            contentColor = Obsidian
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Download",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TopicChip(name: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Surface3)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(Accent.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = TextQuaternary
        )
    }
}
