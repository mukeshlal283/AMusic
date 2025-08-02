package com.example.cicd.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.cicd.R
import com.example.cicd.adapter.MusicAdapter
import com.example.cicd.databinding.FragmentHomeBinding
import com.example.cicd.model.MusicItem
import com.example.cicd.model.SongList
import com.example.cicd.utils.Constant.songList
import com.example.cicd.viewmodel.MusicViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MusicViewModel by viewModels()
    private lateinit var adapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        viewModel.musicData.observe(viewLifecycleOwner) { musicList ->
            adapter = MusicAdapter(::onItemClicked)
            binding.recyclerView.adapter = adapter
            adapter.differ.submitList(musicList)

            var indexValue = 0
            for (i in musicList) {
                val data = SongList(i.id, indexValue, i.audio, i.audiodownload, i.duration, i.image, i.name)
                songList.add(data)
                indexValue++
            }

        }

        // Inflate the layout for this fragment
        return binding.root
    }

    fun onItemClicked(data: MusicItem) {
        val bundle = Bundle()
        bundle.putString("id", data.id)
        bundle.putString("songName", data.name)
        bundle.putString("img", data.image)
        bundle.putString("audio", data.audio)
        bundle.putString("download", data.audiodownload)

        findNavController().navigate(R.id.action_homeFragment_to_musicPlayFragment, bundle)
    }

}