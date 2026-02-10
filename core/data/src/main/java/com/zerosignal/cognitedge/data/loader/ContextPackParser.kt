package com.zerosignal.cognitedge.data.loader

import com.zerosignal.cognitedge.domain.model.ContextChunk
import com.zerosignal.cognitedge.domain.model.ContextPack
import com.zerosignal.cognitedge.domain.model.ContextPackMetadata
import org.json.JSONArray
import org.json.JSONObject

object ContextPackParser {

    fun parse(json: JSONObject): ContextPack {
        val meta = json.getJSONObject("metadata")
        val metadata = ContextPackMetadata(
            packId = meta.getString("packId"),
            language = meta.optString("language", "en"),
            domain = meta.optString("domain", "general"),
            embeddingDimensions = meta.getInt("embeddingDimensions"),
            version = meta.optInt("version", 1)
        )

        val chunkArray = json.getJSONArray("chunks")
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
}
