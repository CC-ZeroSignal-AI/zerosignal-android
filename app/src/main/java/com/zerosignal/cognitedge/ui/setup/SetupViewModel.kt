package com.zerosignal.cognitedge.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerosignal.cognitedge.ai.di.DeviceCapability
import com.zerosignal.cognitedge.domain.model.ModelDownloadState
import com.zerosignal.cognitedge.domain.repository.ModelRepository
import com.zerosignal.cognitedge.logging.ZSLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val modelRepository: ModelRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SetupViewModel"
    }

    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.NotStarted)
    val downloadState: StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

    private val _isDeviceSupported = MutableStateFlow(true)
    val isDeviceSupported: StateFlow<Boolean> = _isDeviceSupported.asStateFlow()

    init {
        checkDevice()
    }

    private fun checkDevice() {
        val capability = DeviceCapability.detect()
        val supported = !capability.isEmulator && capability.isSnapdragon
        _isDeviceSupported.value = supported
        ZSLog.i(TAG, "Device check — emulator=${capability.isEmulator}, snapdragon=${capability.isSnapdragon}, supported=$supported")
    }

    fun startDownload() {
        ZSLog.i(TAG, "Starting model download")
        viewModelScope.launch {
            modelRepository.downloadModel().collect { state ->
                _downloadState.value = state
                when (state) {
                    is ModelDownloadState.Downloading -> {
                        ZSLog.d(TAG, "Download progress: ${(state.progress * 100).toInt()}%  (${state.bytesDownloaded / 1_000_000} MB)")
                    }
                    is ModelDownloadState.Error -> {
                        ZSLog.e(TAG, "Download error: ${state.message}")
                    }
                    is ModelDownloadState.Ready -> {
                        ZSLog.i(TAG, "Model ready")
                    }
                    else -> {
                        ZSLog.d(TAG, "Download state: $state")
                    }
                }
            }
        }
    }
}
