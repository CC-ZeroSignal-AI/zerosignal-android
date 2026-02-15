package com.zerosignal.cognitedge.domain.model

data class RemotePack(
    val packId: String,
    val totalDocuments: Int,
    val topics: List<TopicStat>,
    val sourceUrls: List<String>,
    val domain: String,
    val lastIngestedAt: String,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f
)

data class TopicStat(
    val name: String,
    val documentCount: Int
)

sealed class PackDownloadProgress {
    data object Idle : PackDownloadProgress()
    data class Downloading(val progress: Float, val downloaded: Int, val total: Int) : PackDownloadProgress()
    data object Completed : PackDownloadProgress()
    data class Error(val message: String) : PackDownloadProgress()
}
