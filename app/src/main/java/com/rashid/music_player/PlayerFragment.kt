package com.rashid.music_player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale

class PlayerFragment : Fragment() {

    private lateinit var titleText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playPauseBtn: ImageButton
    private lateinit var timeText: TextView
    private lateinit var shuffleBtn: ImageButton
    private lateinit var repeatBtn: ImageButton
    private lateinit var albumArtView: ImageView
    private lateinit var sleepTimerText: TextView
    private lateinit var sleepTimerBtn: ImageButton
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

        titleText = view.findViewById(R.id.now_playing_title)
        seekBar = view.findViewById(R.id.seek_bar)
        playPauseBtn = view.findViewById(R.id.btn_play_pause)
        timeText = view.findViewById(R.id.time_text)
        shuffleBtn = view.findViewById(R.id.btn_shuffle)
        repeatBtn = view.findViewById(R.id.btn_repeat)
        albumArtView = view.findViewById(R.id.player_album_art)
        sleepTimerText = view.findViewById(R.id.sleep_timer_text)
        sleepTimerBtn = view.findViewById(R.id.btn_sleep_timer)

        updateUI()

        playPauseBtn.setOnClickListener {
            MusicPlayerManager.togglePlayPause()
            updateUI()
        }

        view.findViewById<ImageButton>(R.id.btn_next).setOnClickListener {
            MusicPlayerManager.playNext()
            updateUI()
        }

        view.findViewById<ImageButton>(R.id.btn_prev).setOnClickListener {
            MusicPlayerManager.playPrevious()
            updateUI()
        }

        shuffleBtn.setOnClickListener {
            MusicPlayerManager.toggleShuffle()
            updateUI()
        }

        repeatBtn.setOnClickListener {
            MusicPlayerManager.cycleRepeatMode()
            updateUI()
        }

        sleepTimerBtn.setOnClickListener {
            showSleepTimerDialog()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) MusicPlayerManager.mediaPlayer?.seekTo(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        startSeekBarUpdates()
        return view
    }

    private fun showSleepTimerDialog() {
        if (MusicPlayerManager.sleepTimerActive) {
            MusicPlayerManager.cancelSleepTimer()
            updateUI()
            return
        }
        val options = arrayOf("15 minutes", "30 minutes", "45 minutes", "60 minutes")
        val minutesValues = arrayOf(15, 30, 45, 60)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Set Sleep Timer")
            .setItems(options) { _, which ->
                MusicPlayerManager.startSleepTimer(minutesValues[which])
                updateUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUI() {
        val song = MusicPlayerManager.currentSong
        titleText.text = song?.title ?: "No song playing"

        playPauseBtn.setImageResource(
            if (MusicPlayerManager.isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )

        MusicPlayerManager.mediaPlayer?.let {
            seekBar.max = it.duration
        }

        shuffleBtn.alpha = if (MusicPlayerManager.isShuffle) 1.0f else 0.4f

        repeatBtn.alpha = if (MusicPlayerManager.repeatMode == MusicPlayerManager.REPEAT_OFF) 0.4f else 1.0f
        repeatBtn.setImageResource(
            if (MusicPlayerManager.repeatMode == MusicPlayerManager.REPEAT_ONE)
                android.R.drawable.ic_menu_revert
            else android.R.drawable.ic_menu_revert
        )

        if (song != null) {
            val art = AlbumArtHelper.getAlbumArt(song.path)
            if (art != null) albumArtView.setImageBitmap(art)
            else albumArtView.setImageResource(android.R.drawable.ic_menu_agenda)
        } else {
            albumArtView.setImageResource(android.R.drawable.ic_menu_agenda)
        }

        if (MusicPlayerManager.sleepTimerActive) {
            sleepTimerText.text = "${MusicPlayerManager.getSleepTimerMinutesLeft()} min left"
            sleepTimerBtn.alpha = 1.0f
        } else {
            sleepTimerText.text = ""
            sleepTimerBtn.alpha = 0.4f
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    private fun startSeekBarUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                MusicPlayerManager.mediaPlayer?.let {
                    seekBar.progress = it.currentPosition
                    timeText.text = "${formatTime(it.currentPosition)} / ${formatTime(it.duration)}"

                    if (!it.isPlaying && MusicPlayerManager.isPlaying) {
                        MusicPlayerManager.isPlaying = false
                        playPauseBtn.setImageResource(android.R.drawable.ic_media_play)
                    }
                }
                handler.postDelayed(this, 500)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}