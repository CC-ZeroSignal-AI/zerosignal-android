package com.zerosignal.cognitedge.domain.repository

import com.zerosignal.cognitedge.domain.model.PackDownloadProgress
import com.zerosignal.cognitedge.domain.model.RemotePack
import kotlinx.coroutines.flow.Flow

interface PackStoreRepository {
    suspend fun fetchPacks(): List<RemotePack>
    fun downloadPack(packId: String): Flow<PackDownloadProgress>
    fun getDownloadedPackIds(): Set<String>
}
