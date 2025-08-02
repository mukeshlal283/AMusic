package com.example.cicd.repo

import com.example.cicd.api.ApiUtility
import com.example.cicd.model.MusicItem
import com.example.cicd.utils.Constant.CLIENT_ID

class AppRepository {

    suspend fun getMusic(): List<MusicItem>? {
        val response = ApiUtility.api.getMusic(CLIENT_ID)
        return if (response.isSuccessful) response.body()?.results else null
    }

}