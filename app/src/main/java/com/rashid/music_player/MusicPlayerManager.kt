package com.rashid.music_player

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.content.ContextCompat

object MusicPlayerManager {

    var mediaPlayer: MediaPlayer? = null
    var currentSong: Song? = null
    var isPlaying: Boolean = false
    var songList: List<Song> = emptyList()

    var isShuffle: Boolean = false
    var repeatMode: Int = REPEAT_OFF

    var appContext: Context? = null
    private val listeners = mutableListOf<() -> Unit>()

    const val REPEAT_OFF = 0
    const val REPEAT_ONE = 1
    const val REPEAT_ALL = 2

    private var sleepTimerHandler: android.os.Handler? = null
    private var sleepTimerRunnable: Runnable? = null
    var sleepTimerActive: Boolean = false
    var sleepTimerEndTime: Long = 0L

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
    }

    fun playSong(song: Song) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.path)
            prepare()
            start()
            setOnCompletionListener { handleCompletion() }
        }
        EqualizerManager.init(mediaPlayer!!.audioSessionId)
        currentSong = song
        isPlaying = true
        notifyListeners()
        startServiceIfNeeded()
    }

    private fun startServiceIfNeeded() {
        appContext?.let { ctx ->
            val intent = Intent(ctx, MusicService::class.java)
            ContextCompat.startForegroundService(ctx, intent)
        }
    }

    private fun handleCompletion() {
        when (repeatMode) {
            REPEAT_ONE -> currentSong?.let { playSong(it) }
            else -> playNext()
        }
    }

    fun playNext() {
        val list = songList
        val current = currentSong ?: return
        val index = list.indexOf(current)

        if (isShuffle) {
            if (list.size > 1) {
                var randomIndex: Int
                do {
                    randomIndex = list.indices.random()
                } while (randomIndex == index)
                playSong(list[randomIndex])
            }
            return
        }

        if (index != -1 && index + 1 < list.size) {
            playSong(list[index + 1])
        } else if (repeatMode == REPEAT_ALL && list.isNotEmpty()) {
            playSong(list[0])
        } else {
            isPlaying = false
            notifyListeners()
        }
    }

    fun playPrevious() {
        val list = songList
        val current = currentSong ?: return
        val index = list.indexOf(current)
        if (index > 0) {
            playSong(list[index - 1])
        }
    }

    fun toggleShuffle() {
        isShuffle = !isShuffle
        notifyListeners()
    }

    fun cycleRepeatMode() {
        repeatMode = when (repeatMode) {
            REPEAT_OFF -> REPEAT_ALL
            REPEAT_ALL -> REPEAT_ONE
            else -> REPEAT_OFF
        }
        notifyListeners()
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
            } else {
                it.start()
                isPlaying = true
            }
        }
        notifyListeners()
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        sleepTimerHandler = android.os.Handler(android.os.Looper.getMainLooper())
        sleepTimerEndTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        sleepTimerActive = true
        sleepTimerRunnable = Runnable {
            mediaPlayer?.pause()
            isPlaying = false
            sleepTimerActive = false
            notifyListeners()
        }
        sleepTimerHandler?.postDelayed(sleepTimerRunnable!!, minutes * 60 * 1000L)
        notifyListeners()
    }

    fun cancelSleepTimer() {
        sleepTimerRunnable?.let { sleepTimerHandler?.removeCallbacks(it) }
        sleepTimerActive = false
        notifyListeners()
    }

    fun getSleepTimerMinutesLeft(): Int {
        if (!sleepTimerActive) return 0
        val diff = sleepTimerEndTime - System.currentTimeMillis()
        return if (diff > 0) (diff / 60000).toInt() + 1 else 0
    }
}