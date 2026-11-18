package com.rashid.music_player

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val songId: Long,
    val title: String,
    val artist: String,
    val path: String,
    val durationMs: Long,
    val playedAt: Long
)