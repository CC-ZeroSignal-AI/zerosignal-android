package com.zerosignal.cognitedge.domain.model

sealed interface ModelDownloadState {
    data object NotStarted : ModelDownloadState
    data class Downloading(val progress: Float, val bytesDownloaded: Long = 0L) : ModelDownloadState
    data object Verifying : ModelDownloadState
    data object Initializing : ModelDownloadState
    data object Ready : ModelDownloadState
    data class Error(val message: String) : ModelDownloadState
}
