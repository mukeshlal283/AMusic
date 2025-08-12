package com.example.cicd.utils

import com.example.cicd.model.SongItem

object Constant {

    const val BASE_URL = "https://api.jamendo.com/v3.0/"
    const val CLIENT_ID = "b782e5a2"

    const val CHANNEL_ID = "channel_id"
    const val CHANNEL_NAME = "channel_name"

    var currentIndex = 0

    var getName: String? = null

    val songList = arrayListOf<SongItem>()

}