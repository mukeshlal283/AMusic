package com.example.cicd.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cicd.model.MusicItem
import com.example.cicd.repo.AppRepository
import kotlinx.coroutines.launch

class MusicViewModel: ViewModel() {

    val repo = AppRepository()
    private val _musicData = MutableLiveData<List<MusicItem>>()
    val musicData: LiveData<List<MusicItem>> = _musicData

    init {
        getMusic()
    }

    private fun getMusic() {
        viewModelScope.launch {
            _musicData.value = repo.getMusic()
        }
    }

}