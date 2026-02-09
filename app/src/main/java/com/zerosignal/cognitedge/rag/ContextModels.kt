package com.zerosignal.cognitedge.rag

import org.json.JSONArray
import org.json.JSONObject

/** Represents metadata for a context pack downloaded from the server. */
data class ContextPackMetadata(
    val packId: String,
    val language: String,
    val domain: String,
    val embeddingDimensions: Int,
    val version: Int
)

/** Individual chunk plus its embedding vector. */
data class ContextChunk(
    val id: String,
    val text: String,
    val embedding: FloatArray,
    val metadata: Map<String, String>
)

/** Full pack that can answer questions offline. */
data class ContextPack(
    val metadata: ContextPackMetadata,
    val chunks: List<ContextChunk>
)

internal fun JSONObject.toContextPack(): ContextPack {
    val meta = getJSONObject("metadata")
    val metadata = ContextPackMetadata(
        packId = meta.getString("packId"),
        language = meta.optString("language", "en"),
        domain = meta.optString("domain", "general"),
        embeddingDimensions = meta.getInt("embeddingDimensions"),
        version = meta.optInt("version", 1)
    )

    val chunkArray = getJSONArray("chunks")
    val chunks = (0 until chunkArray.length()).map { index ->
        val chunkObj = chunkArray.getJSONObject(index)
        ContextChunk(
            id = chunkObj.getString("id"),
            text = chunkObj.getString("text"),
            embedding = chunkObj.getJSONArray("embedding").toFloatArray(),
            metadata = chunkObj.optJSONObject("metadata")?.toMap().orEmpty()
        )
    }

    return ContextPack(metadata, chunks)
}

private fun JSONArray.toFloatArray(): FloatArray {
    val array = FloatArray(length())
    for (i in 0 until length()) {
        array[i] = getDouble(i).toFloat()
    }
    return array
}

private fun JSONObject.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    keys().forEachRemaining { key ->
        map[key] = getString(key)
    }
    return map
}
