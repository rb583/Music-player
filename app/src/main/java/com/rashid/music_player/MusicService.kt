package com.rashid.music_player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"
    }

    private lateinit var mediaSession: MediaSessionCompat
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaSession = MediaSessionCompat(this, "MusicService")
        mediaSession.isActive = true
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                if (!MusicPlayerManager.isPlaying) MusicPlayerManager.togglePlayPause()
            }
            override fun onPause() {
                if (MusicPlayerManager.isPlaying) MusicPlayerManager.togglePlayPause()
            }
            override fun onSkipToNext() {
                MusicPlayerManager.playNext()
            }
            override fun onSkipToPrevious() {
                MusicPlayerManager.playPrevious()
            }
        })
        MusicPlayerManager.addListener { updateNotification() }
        startProgressUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> MusicPlayerManager.togglePlayPause()
            ACTION_NEXT -> MusicPlayerManager.playNext()
            ACTION_PREV -> MusicPlayerManager.playPrevious()
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun startProgressUpdates() {
        progressRunnable = object : Runnable {
            override fun run() {
                updatePlaybackState()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(progressRunnable!!)
    }

    private fun updatePlaybackState() {
        val position = MusicPlayerManager.mediaPlayer?.currentPosition?.toLong() ?: 0L
        val state = if (MusicPlayerManager.isPlaying)
            PlaybackStateCompat.STATE_PLAYING
        else
            PlaybackStateCompat.STATE_PAUSED

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .setState(state, position, if (MusicPlayerManager.isPlaying) 1f else 0f)
                .build()
        )
    }

    private fun updateMetadata() {
        val song = MusicPlayerManager.currentSong
        val duration = MusicPlayerManager.mediaPlayer?.duration?.toLong() ?: 0L
        val albumArt = song?.let { AlbumArtHelper.getAlbumArt(it.path) }

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song?.title ?: "")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song?.artist ?: "")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .build()
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Music Playback", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(): Notification {
        val song = MusicPlayerManager.currentSong
        val playPauseIcon = if (MusicPlayerManager.isPlaying)
            android.R.drawable.ic_media_pause
        else
            android.R.drawable.ic_media_play

        val albumArt = song?.let { AlbumArtHelper.getAlbumArt(it.path) }

        updateMetadata()
        updatePlaybackState()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song?.title ?: "No song playing")
            .setContentText(song?.artist ?: "")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(albumArt)
            .setOnlyAlertOnce(true)
            .setColor(ContextCompat.getColor(this, R.color.notification_green))
            .setColorized(true)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent())
            .addAction(android.R.drawable.ic_media_previous, "", actionPendingIntent(ACTION_PREV))
            .addAction(playPauseIcon, "", actionPendingIntent(ACTION_PLAY_PAUSE))
            .addAction(android.R.drawable.ic_media_next, "", actionPendingIntent(ACTION_NEXT))
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        progressRunnable?.let { handler.removeCallbacks(it) }
        mediaSession.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}