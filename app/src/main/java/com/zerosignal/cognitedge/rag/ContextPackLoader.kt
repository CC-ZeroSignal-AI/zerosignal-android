package com.zerosignal.cognitedge.rag

import android.content.Context
import java.io.BufferedReader
import org.json.JSONObject

/** Loads a pre-downloaded context pack from local storage or bundled assets. */
class ContextPackLoader(private val context: Context) {

    fun loadFromAssets(assetName: String = DEFAULT_PACK_ASSET): ContextPack {
        val json = context.assets.open(assetName).bufferedReader().use(BufferedReader::readText)
        return JSONObject(json).toContextPack()
    }

    companion object {
        const val DEFAULT_PACK_ASSET = "context_pack.json"
    }
}
