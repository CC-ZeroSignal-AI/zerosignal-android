package com.zerosignal.cognitedge.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PackMetadataDto(
    @Json(name = "pack_id") val packId: String,
    @Json(name = "total_documents") val totalDocuments: Int,
    val topics: List<TopicStatDto>,
    @Json(name = "source_urls") val sourceUrls: List<String>,
    val metadata: Map<String, Any>? = null,
    @Json(name = "last_ingested_at") val lastIngestedAt: String
)

@JsonClass(generateAdapter = true)
data class TopicStatDto(
    val name: String,
    @Json(name = "document_count") val documentCount: Int
)

@JsonClass(generateAdapter = true)
data class DownloadResponseDto(
    @Json(name = "pack_id") val packId: String,
    val limit: Int,
    val offset: String?,
    @Json(name = "next_offset") val nextOffset: String?,
    val items: List<DownloadItemDto>
)

@JsonClass(generateAdapter = true)
data class DownloadItemDto(
    @Json(name = "document_id") val documentId: String,
    val text: String,
    val embedding: List<Float>,
    val metadata: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class SearchRequestDto(
    val query: String,
    @Json(name = "top_k") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class SearchResultDto(
    @Json(name = "document_id") val documentId: String,
    val text: String,
    val score: Float,
    val metadata: Map<String, Any>? = null
)
