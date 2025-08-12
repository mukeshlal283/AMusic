package com.example.cicd.services


import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.cicd.R
import com.example.cicd.model.SongItem
import com.example.cicd.utils.Constant.CHANNEL_ID
import com.example.cicd.utils.Constant.currentIndex
import com.example.cicd.utils.Constant.songList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

const val PREV = "prev"
const val NEXT = "next"
const val PLAY_PAUSE = "play_pause"

class MusicPlayerService: Service() {

    val binder = MusicBinder()

    inner class MusicBinder: Binder() {
        fun getService() = this@MusicPlayerService
        fun setMusicList(list: List<SongItem>) {
            this@MusicPlayerService.musicList = list.toMutableList()
        }
        fun setSeekBar(seekBar: SeekBar) {
            this@MusicPlayerService.seekBar = seekBar
        }

        fun setPlayMode(value: Int) {
            this@MusicPlayerService.playMode.update { value }
        }

        fun getCurrentTrack() = this@MusicPlayerService.currentTrack
        fun isPlaying() = this@MusicPlayerService.isPlaying
        fun getCurrentDuration() = this@MusicPlayerService.currentDuration
        fun getMaxDuration() = this@MusicPlayerService.maxDuration

    }

    private var mediaPlayer = MediaPlayer()
    private val currentTrack = MutableStateFlow<SongItem>(SongItem())
    private val maxDuration = MutableStateFlow(0)
    private val currentDuration = MutableStateFlow(0)
    private val scope = CoroutineScope(Dispatchers.Main)
    private var musicList = mutableListOf<SongItem>()
    private val isPlaying = MutableStateFlow<Boolean>(false)
    private var job: Job? = null
    var playMode = MutableStateFlow<Int>(0)
    private val playSpeed = MutableStateFlow<Boolean>(false)
    private var seekBar: SeekBar? = null
    private val handler = Handler(Looper.getMainLooper())
    var isSpeedPlaying = false


    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(intent.action) {
                PREV -> {
                    prev()
                }
                NEXT -> {
                    next()
                }
                PLAY_PAUSE -> {
                    playPause()
                }
                else -> {
                    currentTrack.update { songList.get(currentIndex) }
                    play(currentTrack.value)
                }
            }
        }

        return START_STICKY
    }

    private fun play(item: SongItem) {
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer()

        currentTrack.update { item }
        mediaPlayer.setDataSource(item.audio)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            seekBar?.max = mediaPlayer.duration
            maxDuration.update { mediaPlayer.duration }
            updateSeeker()
            sendNotification(item)
        }
        mediaPlayer.setOnCompletionListener {
            handlePlayMode()
        }
    }

    private fun updateSeeker() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    seekBar?.progress = mediaPlayer.currentPosition
                    currentDuration.update { mediaPlayer.currentPosition }
                }
                handler.postDelayed(this, 1000)
            }

        }, 0)

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

    }

    fun prev() {
        mediaPlayer.reset()
        isPlaying.update { mediaPlayer.isPlaying }
        mediaPlayer = MediaPlayer()

        val index = musicList.indexOf(currentTrack.value)
//        val prevIndex = if (index < 0) musicList.size - 1 else index.minus(1)
        val prevIndex = index.minus(1).mod(musicList.size)
        val prevItem = musicList.get(prevIndex)

        currentTrack.update { prevItem }
        mediaPlayer.setDataSource(prevItem.audio)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            seekBar?.max = mediaPlayer.duration
            maxDuration.update { mediaPlayer.duration }
            updateSeeker()
            sendNotification(currentTrack.value)
        }
        mediaPlayer.setOnCompletionListener {
            handlePlayMode()
        }
    }

    fun next() {
        mediaPlayer.reset()
        isPlaying.update { mediaPlayer.isPlaying }
        mediaPlayer = MediaPlayer()

        val index = musicList.indexOf(currentTrack.value)
        val nextIndex = index.plus(1).mod(musicList.size)
        val nextItem = musicList.get(nextIndex)
        currentTrack.update { nextItem }
        mediaPlayer.setDataSource(nextItem.audio)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            seekBar?.max = mediaPlayer.duration
            maxDuration.update { mediaPlayer.duration }
            updateSeeker()
            sendNotification(currentTrack.value)
        }
        mediaPlayer.setOnCompletionListener {
            handlePlayMode()
        }

    }

    fun playPause() {

        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying.update { true }
            Log.d("ISPLAYING", "")
            sendNotification(currentTrack.value)
//            pause.update { true }
        } else {
            mediaPlayer.start()
            sendNotification(currentTrack.value)
//            pause.update { false }
        }

    }

    private fun handlePlayMode() {
        when (playMode.value) {
            0 -> {
                Log.d("HANDLE_PLAY", "looping = ${playMode.value}")
                mediaPlayer.isLooping = true
                play(currentTrack.value)
            }
            1 -> {
                mediaPlayer.isLooping = false
                Log.d("HANDLE_PLAY", "next automatic = ${playMode.value}")
                next()
            }
            2 -> {
                Log.d("HANDLE_PLAY", "shuffle = ${playMode.value}")
                mediaPlayer.isLooping = false
                val randomIndex = (musicList.indices).random()
                val itemFromRandomIndex = musicList.get(randomIndex)
                play(itemFromRandomIndex)
            }
            else -> {}
        }
    }

    fun playSpeed(d: Float) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val newSpeed = d // you can cycle speeds like 1.0f, 1.25f, 1.5f, etc.
            mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(newSpeed)
        } else {
            Toast.makeText(this, "Speed control not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotification(songItem: SongItem) {

        isPlaying.update { mediaPlayer.isPlaying }

        val session = MediaSessionCompat(this, "music")

        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0, 1, 2)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(style)
            .setContentTitle(songItem.name)
            .setContentText(songItem.index.toString())
            .addAction(R.drawable.outline_chevron_backward_24, "prev", createPrevPendingIntent())
            .addAction(if (mediaPlayer.isPlaying) R.drawable.twotone_pause_circle_outline_24 else R.drawable.outline_play_circle_outline_24
                , "play_pause"
                , createPlayPausePendingIntent())
            .addAction(R.drawable.outline_chevron_forward_24, "next", createNextPendingIntent())
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.music_player))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .build()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                startForeground(1, notification)
            }
        } else {
            startForeground(1, notification)
        }

    }

    private fun createPrevPendingIntent(): PendingIntent? {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = PREV
        }
        return PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createPlayPausePendingIntent(): PendingIntent? {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = PLAY_PAUSE
        }
        return PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNextPendingIntent(): PendingIntent? {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = NEXT
        }
        return PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


}