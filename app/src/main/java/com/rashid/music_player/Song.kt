package com.rashid.music_player

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val path: String,
    val dateAdded: Long = 0L
)