package com.example.cicd.view

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.cicd.databinding.FragmentMusicPlayBinding
import com.example.cicd.utils.Constant.songList
import com.example.cicd.R

class MusicPlayFragment : Fragment() {

    private lateinit var binding: FragmentMusicPlayBinding
    private var currentPosition: Int = 0
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var seekbar: SeekBar
    private val handler = Handler(Looper.getMainLooper())
    private var playMode = 0
    private var speedPlay = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMusicPlayBinding.inflate(layoutInflater)
        seekbar = binding.seek
        val bundle = arguments
        val id = bundle?.getString("id")?.toInt()

        for (i in songList) {
            if (id == i.id.toInt()) {
                currentPosition = i.index
                updateLayoutData()
                break
            }
        }

//        updateLayoutData()

        binding.nextBtn.setOnClickListener {
            binding.playBtn.visibility = View.GONE
            binding.pauseBtn.visibility = View.VISIBLE
            mediaPlayer?.release()
            mediaPlayer = null
            currentPosition++
            if (currentPosition > songList.size-1) {
                currentPosition = 0
            }
            updateLayoutData()
        }

        binding.playPreviousBtn.setOnClickListener {
            binding.playBtn.visibility = View.GONE
            binding.pauseBtn.visibility = View.VISIBLE
            mediaPlayer?.release()
            mediaPlayer = null
            currentPosition--
            if (currentPosition < 0) {
                currentPosition = songList.size-1
            }
            updateLayoutData()
        }

        binding.playBtn.setOnClickListener {
            mediaPlayer?.start()
            updateSeekBar()
            binding.playBtn.visibility = View.GONE
            binding.pauseBtn.visibility = View.VISIBLE
        }

        binding.pauseBtn.setOnClickListener {
            onPause()
            binding.pauseBtn.visibility = View.GONE
            binding.playBtn.visibility = View.VISIBLE
        }

        binding.playMode.setOnClickListener {
            if (playMode == 0) {
                playMode = 1
                binding.playMode.setImageResource(R.drawable.repeat_one)
            } else if (playMode == 1) {
                playMode = 2
                binding.playMode.setImageResource(R.drawable.shuffle)
            } else  {
                playMode = 0
                binding.playMode.setImageResource(R.drawable.repeat_all)
            }
        }

        binding.speedUp.setOnClickListener {

            if (speedPlay) {
                playSpeed(1.5f)
                binding.speedUp.text = "1.0 f"
                speedPlay = false
            } else {
                playSpeed(1.0f)
                binding.speedUp.text = "1.5 f"
                speedPlay = true
            }

        }

        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    mediaPlayer?.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })


        // Inflate the layout for this fragment
        return binding.root
    }

    private fun playSpeed(d: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val newSpeed = d // you can cycle speeds like 1.0f, 1.25f, 1.5f, etc.
            mediaPlayer?.playbackParams = mediaPlayer!!.playbackParams.setSpeed(newSpeed)
        } else {
            Toast.makeText(requireContext(), "Speed control not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLayoutData() {
        for (i in songList) {
            if (currentPosition == i.index) {
                binding.songName.text = i.name
                Glide.with(requireContext())
                    .load(i.image)
                    .into(binding.img)
                playMusic(i.audio)
                break
            }
        }
    }

    private fun playMusic(audio: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            binding.bufferingOverlay.visibility = View.VISIBLE
            mediaPlayer?.setDataSource(audio)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                binding.bufferingOverlay.visibility = View.GONE
                it.start()
                seekbar.max = it.duration
                updateSeekBar()
                binding.totalTime.text = formatTime(it.duration)
            }
            mediaPlayer?.setOnCompletionListener {
                handler.removeCallbacksAndMessages(null)
                mediaPlayer?.release()
                mediaPlayer = null

                when (playMode) {
                    0 -> {
                        currentPosition++ //repeat all
                        updateLayoutData()
                    }
                    1 -> {
                        currentPosition //repeat
                        updateLayoutData()
                    }
                    else -> { //shuffle
                        currentPosition = (songList.indices).filter { it != currentPosition }.random()
                        updateLayoutData()
                    }
                }
            }
        } else {
            mediaPlayer?.start()
            updateSeekBar()
        }

    }

    private fun updateSeekBar() {
        handler.postDelayed(object: Runnable {
            override fun run() {
                if (mediaPlayer!!.isPlaying) {
                    seekbar.progress = mediaPlayer!!.currentPosition
                    binding.currentTime.text = formatTime(mediaPlayer!!.currentPosition)
                    handler.postDelayed(this, 1000)
                }
            }
        }, 0)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


}
