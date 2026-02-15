package com.zerosignal.cognitedge.data.remote

import com.zerosignal.cognitedge.data.remote.dto.DownloadResponseDto
import com.zerosignal.cognitedge.data.remote.dto.PackMetadataDto
import com.zerosignal.cognitedge.data.remote.dto.SearchRequestDto
import com.zerosignal.cognitedge.data.remote.dto.SearchResultDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ZeroSignalApi {

    @GET("packs")
    suspend fun listPacks(): List<PackMetadataDto>

    @GET("packs/{pack_id}")
    suspend fun getPackMetadata(
        @Path("pack_id") packId: String
    ): PackMetadataDto

    @GET("packs/{pack_id}/download")
    suspend fun downloadPack(
        @Path("pack_id") packId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: String? = null
    ): DownloadResponseDto

    @POST("packs/{pack_id}/search")
    suspend fun searchPack(
        @Path("pack_id") packId: String,
        @Body request: SearchRequestDto
    ): List<SearchResultDto>
}
