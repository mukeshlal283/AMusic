package com.example.cicd.view.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.cicd.R
import com.example.cicd.databinding.ActivityMusicPlayBinding
import com.example.cicd.model.SongItem
import com.example.cicd.services.MusicPlayerService
import com.example.cicd.utils.Constant.getName
import com.example.cicd.utils.Constant.songList
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicPlayActivity : AppCompatActivity() {

//    private lateinit var seekbar: SeekBar
//    private val handler = Handler(Looper.getMainLooper())
//    private var playMode = 0
//    private var speedPlay = false
//    private var currentPosition = 0
    private lateinit var binding: ActivityMusicPlayBinding
    private val firebaseAuth = Firebase.auth
    private var playMode = MutableStateFlow(0)
    private val currentTrack = MutableStateFlow<SongItem>(SongItem())
    private val isPlaying = MutableStateFlow<Boolean>(false)
    private val maxDuration = MutableStateFlow(0)
    private val currentDuration = MutableStateFlow(0)

    private var service: MusicPlayerService? = null
    private var isBound = false
    var playSpeed = false

    val connection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            service = (binder as MusicPlayerService.MusicBinder).getService()

            binder.setMusicList(songList)
            binder.setSeekBar(binding.seek)

            lifecycleScope.launch {
                binder.getCurrentTrack().collect {
                    currentTrack.value = it
                }
            }

            lifecycleScope.launch {
                binder.isPlaying().collectLatest {
                    isPlaying.value = it
                }
            }

            lifecycleScope.launch {
                binder.getCurrentDuration().collectLatest {
                    currentDuration.value = it
                }
            }

            lifecycleScope.launch {
                binder.getMaxDuration().collectLatest {
                    maxDuration.value = it
                }
            }

            lifecycleScope.launch {
                binder.setPlayMode(0)
            }

            isBound = true

        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMusicPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intent = Intent(this, MusicPlayerService::class.java)
        startService(intent)
        bindService(intent, connection, BIND_AUTO_CREATE)

        binding.userName.text = getName

        lifecycleScope.launch {
            currentTrack.collect { item ->
                binding.songName.text = item.name
                Glide.with(this@MusicPlayActivity).load(item.image).into(binding.img)
            }
        }

        lifecycleScope.launch {
            isPlaying.collect { result ->
                Log.d("RES", "isPlaying = $result")
                if (result) {
                    binding.playPauseBtn.setImageResource(R.drawable.twotone_pause_circle_outline_24)
                    binding.bufferingOverlay.visibility = View.GONE
                } else {
                    binding.playPauseBtn.setImageResource(R.drawable.outline_play_circle_outline_24)
                }
//                binding.playPauseBtn.setImageResource(
//                    if (result) R.drawable.twotone_pause_circle_outline_24 else R.drawable.outline_play_circle_outline_24
//                )
//                if (result) binding.bufferingOverlay.visibility = View.GONE else binding.bufferingOverlay.visibility = View.VISIBLE
            }
        }

        lifecycleScope.launch {
            combine(currentDuration, maxDuration) {current, max ->
                current to max
            }.collect { (current, max) ->
                binding.currentTime.text = formatTime(current)
                binding.maxTime.text = formatTime(max)
            }
        }

        binding.playPauseBtn.setOnClickListener {
            service?.playPause()
        }

        binding.nextBtn.setOnClickListener {
            service?.next()
        }

        binding.playPreviousBtn.setOnClickListener {
            service?.prev()
        }


        binding.logoutBtn.setOnClickListener {
            val intent = Intent(this, MusicPlayerService::class.java)
            stopService(intent)
            unbindService(connection)

            firebaseAuth.signOut()
            getName = null

            val newIntent = Intent(this, AuthActivity::class.java)
            newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(newIntent)
        }

        binding.playMode.setOnClickListener {
            val getPlayMode = service?.playMode
            if (getPlayMode?.value == 0) {
                binding.playMode.setImageResource(R.drawable.repeat_all)
                getPlayMode.update { 1 }
            } else if (getPlayMode?.value == 1) {
                binding.playMode.setImageResource(R.drawable.shuffle)
                getPlayMode.update { 2 }
            } else if (getPlayMode?.value == 2) {
                binding.playMode.setImageResource(R.drawable.repeat_one)
                getPlayMode.update { 0 }
            }
        }

        binding.speedUp.setOnClickListener {
            if (!service!!.isSpeedPlaying) {
                service?.isSpeedPlaying = true
                service?.playSpeed(1.25f)
                binding.speedUp.text = "1.0f"
            } else {
                service?.isSpeedPlaying = false
                service?.playSpeed(1.0f)
                binding.speedUp.text = "1.25f"
            }
        }

//        binding.playMode.setOnClickListener {
//            if (playMode == 0) {
//                playMode = 1
//                binding.playMode.setImageResource(R.drawable.repeat_one)
//            } else if (playMode == 1) {
//                playMode = 2
//                binding.playMode.setImageResource(R.drawable.shuffle)
//            } else  {
//                playMode = 0
//                binding.playMode.setImageResource(R.drawable.repeat_all)
//            }
//        }

//        binding.speedUp.setOnClickListener {
//
//            if (speedPlay) {
//                playSpeed(1.5f)
//                binding.speedUp.text = "1.0 f"
//                speedPlay = false
//            } else {
//                playSpeed(1.0f)
//                binding.speedUp.text = "1.5 f"
//                speedPlay = true
//            }
//
//        }

//        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
//            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
//                if (p2) {
//                    mediaPlayer?.seekTo(p1)
//                }
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {}
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {}
//
//        })

    }

//    private fun playSpeed(d: Float) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val newSpeed = d // you can cycle speeds like 1.0f, 1.25f, 1.5f, etc.
//            mediaPlayer?.playbackParams = mediaPlayer!!.playbackParams.setSpeed(newSpeed)
//        } else {
//            Toast.makeText(this, "Speed control not supported on this device", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun updateSeekBar() {
//        handler.postDelayed(object: Runnable {
//            override fun run() {
//                if (mediaPlayer!!.isPlaying) {
//                    seekbar.progress = mediaPlayer!!.currentPosition
//                    binding.currentTime.text = formatTime(mediaPlayer!!.currentPosition)
//                    handler.postDelayed(this, 1000)
//                }
//            }
//        }, 0)
//    }
//
//    private fun formatTime(ms: Int): String {
//        val totalSeconds = ms / 1000
//        val minutes = totalSeconds / 60
//        val seconds = totalSeconds % 60
//        return String.format("%02d:%02d", minutes, seconds)
//    }

    fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

}