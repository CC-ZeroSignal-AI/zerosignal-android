package com.zerosignal.cognitedge.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _isEmulator = MutableStateFlow(false)
    val isEmulator: StateFlow<Boolean> = _isEmulator.asStateFlow()

    init {
        checkDevice()
    }

    private fun checkDevice() {
        val fingerprint = android.os.Build.FINGERPRINT
        val model = android.os.Build.MODEL
        _isEmulator.value = fingerprint.contains("generic", ignoreCase = true)
            || model.contains("Emulator", ignoreCase = true)
            || model.contains("Android SDK", ignoreCase = true)
    }

    fun startDownload() {
        viewModelScope.launch {
            modelRepository.downloadModel().collect { state ->
                _downloadState.value = state
            }
        }
    }

    fun skipSetup() {
        _downloadState.value = ModelDownloadState.Ready
    }
}
