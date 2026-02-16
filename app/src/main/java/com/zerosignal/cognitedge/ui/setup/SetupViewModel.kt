package com.zerosignal.cognitedge.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerosignal.cognitedge.ai.di.DeviceCapability
import com.zerosignal.cognitedge.domain.model.ModelDownloadState
import com.zerosignal.cognitedge.domain.repository.ModelRepository
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

    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.NotStarted)
    val downloadState: StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

    private val _isDeviceSupported = MutableStateFlow(true)
    val isDeviceSupported: StateFlow<Boolean> = _isDeviceSupported.asStateFlow()

    init {
        checkDevice()
    }

    private fun checkDevice() {
        val capability = DeviceCapability.detect()
        _isDeviceSupported.value = !capability.isEmulator && capability.isSnapdragon
    }

    fun startDownload() {
        viewModelScope.launch {
            modelRepository.downloadModel().collect { state ->
                _downloadState.value = state
            }
        }
    }
}
