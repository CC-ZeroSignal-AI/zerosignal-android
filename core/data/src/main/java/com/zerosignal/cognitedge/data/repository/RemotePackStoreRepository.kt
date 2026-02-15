package com.zerosignal.cognitedge.data.repository

import android.content.SharedPreferences
import com.zerosignal.cognitedge.data.database.entity.VectorChunkEntity
import com.zerosignal.cognitedge.data.remote.ZeroSignalApi
import com.zerosignal.cognitedge.domain.model.PackDownloadProgress
import com.zerosignal.cognitedge.domain.model.RemotePack
import com.zerosignal.cognitedge.domain.model.TopicStat
import com.zerosignal.cognitedge.domain.repository.PackStoreRepository
import io.objectbox.BoxStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePackStoreRepository @Inject constructor(
    private val api: ZeroSignalApi,
    private val boxStore: BoxStore,
    private val prefs: SharedPreferences
) : PackStoreRepository {

    override suspend fun fetchPacks(): List<RemotePack> {
        val downloadedIds = getDownloadedPackIds()
        return api.listPacks().map { dto ->
            val domainName = (dto.metadata?.get("default_metadata") as? Map<*, *>)
                ?.get("domain") as? String ?: "general"
            RemotePack(
                packId = dto.packId,
                totalDocuments = dto.totalDocuments,
                topics = dto.topics.map { TopicStat(it.name, it.documentCount) },
                sourceUrls = dto.sourceUrls,
                domain = domainName,
                lastIngestedAt = dto.lastIngestedAt,
                isDownloaded = downloadedIds.contains(dto.packId)
            )
        }
    }

    override fun downloadPack(packId: String): Flow<PackDownloadProgress> = flow {
        emit(PackDownloadProgress.Downloading(0f, 0, 0))

        val metadata = api.getPackMetadata(packId)
        val totalDocs = metadata.totalDocuments
        var downloadedCount = 0
        var offset: String? = null

        val box = boxStore.boxFor(VectorChunkEntity::class.java)

        do {
            val response = api.downloadPack(packId, limit = 50, offset = offset)

            val entities = response.items.map { item ->
                val metaJson = JSONObject(item.metadata?.mapValues { it.value.toString() } ?: emptyMap<String, String>()).toString()
                val embedding = item.embedding.toFloatArray()
                val padded = padTo768(embedding)

                VectorChunkEntity().apply {
                    this.chunkId = item.documentId
                    this.text = item.text
                    this.metadataJson = metaJson
                    this.embedding = padded
                }
            }

            box.put(entities)
            downloadedCount += response.items.size

            val progress = if (totalDocs > 0) downloadedCount.toFloat() / totalDocs else 0f
            emit(PackDownloadProgress.Downloading(progress.coerceAtMost(1f), downloadedCount, totalDocs))

            offset = response.nextOffset
        } while (offset != null)

        // Mark as downloaded
        prefs.edit().putStringSet(
            DOWNLOADED_PACKS_KEY,
            getDownloadedPackIds() + packId
        ).apply()

        emit(PackDownloadProgress.Completed)
    }

    override fun getDownloadedPackIds(): Set<String> {
        return prefs.getStringSet(DOWNLOADED_PACKS_KEY, emptySet()) ?: emptySet()
    }

    companion object {
        private const val DOWNLOADED_PACKS_KEY = "downloaded_pack_ids"

        fun padTo768(vector: FloatArray): FloatArray {
            if (vector.size >= 768) return vector.copyOf(768)
            return FloatArray(768).also { padded ->
                vector.copyInto(padded)
            }
        }
    }
}
