package com.rashid.music_player

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.toList().forEach { it.invoke() }
    }

    fun playSong(song: Song, contextList: List<Song>? = null) {
        if (!contextList.isNullOrEmpty()) {
            songList = contextList
        }
        try {
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
            recordPlay(song, mediaPlayer!!.duration.toLong())
            notifyListeners()
            startServiceIfNeeded()
        } catch (e: Exception) {
            isPlaying = false
            notifyListeners()
        }
    }

    private fun recordPlay(song: Song, durationMs: Long) {
        val ctx = appContext ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getInstance(ctx).playHistoryDao().insert(
                    PlayHistoryEntity(
                        songId = song.id,
                        title = song.title,
                        artist = song.artist,
                        path = song.path,
                        durationMs = durationMs,
                        playedAt = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {
            }
        }
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

    private fun getActiveSongList(): List<Song> {
        return if (songList.isNotEmpty()) songList else loadFallbackSongs()
    }

    private fun loadFallbackSongs(): List<Song> {
        val ctx = appContext ?: return emptyList()
        val list = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            ctx.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    list.add(
                        Song(
                            cursor.getLong(idCol),
                            cursor.getString(titleCol),
                            cursor.getString(artistCol),
                            cursor.getString(pathCol),
                            cursor.getLong(dateAddedCol)
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        if (list.isNotEmpty()) {
            this.songList = list
        }
        return list
    }

    fun playNext() {
        val list = getActiveSongList()
        if (list.isEmpty()) return

        val current = currentSong ?: run {
            playSong(list[0])
            return
        }

        var index = list.indexOfFirst { it.id == current.id }
        if (index == -1) index = 0

        if (isShuffle) {
            if (list.size > 1) {
                var randomIndex: Int
                do {
                    randomIndex = list.indices.random()
                } while (randomIndex == index)
                playSong(list[randomIndex])
            } else {
                playSong(list[0])
            }
            return
        }

        val nextIndex = (index + 1) % list.size
        playSong(list[nextIndex])
    }

    fun playPrevious() {
        val list = getActiveSongList()
        if (list.isEmpty()) return

        val current = currentSong ?: run {
            playSong(list[0])
            return
        }

        var index = list.indexOfFirst { it.id == current.id }
        if (index == -1) index = 0

        if (isShuffle) {
            if (list.size > 1) {
                var randomIndex: Int
                do {
                    randomIndex = list.indices.random()
                } while (randomIndex == index)
                playSong(list[randomIndex])
            } else {
                playSong(list[0])
            }
            return
        }

        val prevIndex = if (index > 0) index - 1 else list.size - 1
        playSong(list[prevIndex])
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
        val player = mediaPlayer
        if (player == null) {
            val songToPlay = currentSong ?: getActiveSongList().firstOrNull()
            songToPlay?.let { playSong(it) }
            return
        }

        try {
            if (player.isPlaying) {
                player.pause()
                isPlaying = false
            } else {
                player.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            val songToPlay = currentSong ?: getActiveSongList().firstOrNull()
            songToPlay?.let { playSong(it) }
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