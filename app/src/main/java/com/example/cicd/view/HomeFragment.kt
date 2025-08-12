package com.example.cicd.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.cicd.adapter.MusicAdapter
import com.example.cicd.databinding.FragmentHomeBinding
import com.example.cicd.model.MusicItem
import com.example.cicd.model.SongItem
import com.example.cicd.utils.Constant.currentIndex
import com.example.cicd.utils.Constant.songList
import com.example.cicd.view.activity.MusicPlayActivity
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
            binding.shimmerView.visibility = View.GONE

            var indexValue = 0
            for (i in musicList) {
                val data = SongItem(i.id, indexValue, i.audio, i.audiodownload, i.duration, i.image, i.name)
                songList.add(data)
                indexValue++
            }

        }

        // Inflate the layout for this fragment
        return binding.root
    }

    fun onItemClicked(data: MusicItem) {

        getCurrentIndex(data.id)

        val intent = Intent(requireActivity(), MusicPlayActivity::class.java)
        startActivity(intent)


    }

    private fun getCurrentIndex(id: String) {
        for (i in songList) {
            if (id == i.id) {
                currentIndex = i.index
            }
        }
    }

}