package com.example.cicd.api

import com.example.cicd.model.MusicResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("tracks")
    suspend fun getMusic(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20
    ): Response<MusicResult>

}