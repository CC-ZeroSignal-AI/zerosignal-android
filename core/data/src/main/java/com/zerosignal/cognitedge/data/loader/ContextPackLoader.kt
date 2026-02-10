package com.zerosignal.cognitedge.data.loader

import android.content.Context
import com.zerosignal.cognitedge.domain.model.ContextPack
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextPackLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun loadFromAssets(assetName: String = DEFAULT_PACK_ASSET): ContextPack {
        val json = context.assets.open(assetName).bufferedReader().use(BufferedReader::readText)
        return ContextPackParser.parse(JSONObject(json))
    }

    companion object {
        const val DEFAULT_PACK_ASSET = "context_pack.json"
    }
}
