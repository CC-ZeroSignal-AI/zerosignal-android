package com.zerosignal.cognitedge.domain.repository

import com.zerosignal.cognitedge.domain.model.ContextChunk
import com.zerosignal.cognitedge.domain.model.ContextPack

interface ContextRepository {
    fun loadContextPack(): ContextPack
    fun findRelevantChunks(query: String, topK: Int = 3): List<ContextChunk>
}
