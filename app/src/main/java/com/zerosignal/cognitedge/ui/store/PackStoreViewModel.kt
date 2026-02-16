package com.zerosignal.cognitedge.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerosignal.cognitedge.domain.model.PackDownloadProgress
import com.zerosignal.cognitedge.domain.model.RemotePack
import com.zerosignal.cognitedge.domain.repository.PackStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackStoreUiState(
    val packs: List<RemotePack> = emptyList(),
    val filteredPacks: List<RemotePack> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val downloadProgress: Map<String, PackDownloadProgress> = emptyMap()
)

@HiltViewModel
class PackStoreViewModel @Inject constructor(
    private val repository: PackStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackStoreUiState())
    val uiState: StateFlow<PackStoreUiState> = _uiState.asStateFlow()

    init {
        loadPacks()
    }

    fun loadPacks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val packs = repository.fetchPacks()
                _uiState.update {
                    it.copy(
                        packs = packs,
                        filteredPacks = filterPacks(packs, it.searchQuery),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load packs"
                    )
                }
            }
        }
    }

    fun search(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredPacks = filterPacks(it.packs, query)
            )
        }
    }

    fun downloadPack(packId: String) {
        // Prevent double-download
        val currentProgress = _uiState.value.downloadProgress[packId]
        if (currentProgress is PackDownloadProgress.Downloading) return

        viewModelScope.launch {
            try {
                repository.downloadPack(packId).collect { progress ->
                    _uiState.update { state ->
                        val newProgress = state.downloadProgress.toMutableMap()
                        newProgress[packId] = progress

                        val updatedPacks = if (progress is PackDownloadProgress.Completed) {
                            state.packs.map { pack ->
                                if (pack.packId == packId) pack.copy(isDownloaded = true) else pack
                            }
                        } else {
                            state.packs.map { pack ->
                                if (pack.packId == packId && progress is PackDownloadProgress.Downloading) {
                                    pack.copy(downloadProgress = progress.progress)
                                } else pack
                            }
                        }

                        state.copy(
                            packs = updatedPacks,
                            filteredPacks = filterPacks(updatedPacks, state.searchQuery),
                            downloadProgress = newProgress
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    val newProgress = state.downloadProgress.toMutableMap()
                    newProgress[packId] = PackDownloadProgress.Error(e.message ?: "Download failed")
                    state.copy(
                        downloadProgress = newProgress,
                        filteredPacks = filterPacks(state.packs, state.searchQuery)
                    )
                }
            }
        }
    }

    private fun filterPacks(packs: List<RemotePack>, query: String): List<RemotePack> {
        if (query.isBlank()) return packs
        val lowerQuery = query.lowercase()
        return packs.filter { pack ->
            pack.packId.lowercase().contains(lowerQuery) ||
                pack.domain.lowercase().contains(lowerQuery) ||
                pack.topics.any { it.name.lowercase().contains(lowerQuery) }
        }
    }
}
