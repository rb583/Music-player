package com.rashid.music_player

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

data class SongPlayCount(
    val songId: Long,
    val title: String,
    val artist: String,
    val path: String,
    val playCount: Int
)

data class ArtistPlayCount(
    val artist: String,
    val playCount: Int
)

@Dao
interface PlayHistoryDao {

    @Insert
    suspend fun insert(entry: PlayHistoryEntity)

    @Query("SELECT * FROM play_history WHERE id IN (SELECT MAX(id) FROM play_history GROUP BY songId) ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentlyPlayed(limit: Int): List<PlayHistoryEntity>

    @Query("SELECT songId, title, artist, path, COUNT(*) AS playCount FROM play_history GROUP BY songId ORDER BY playCount DESC LIMIT :limit")
    suspend fun getMostPlayed(limit: Int): List<SongPlayCount>

    @Query("SELECT artist, COUNT(*) AS playCount FROM play_history GROUP BY artist ORDER BY playCount DESC LIMIT :limit")
    suspend fun getTopArtists(limit: Int): List<ArtistPlayCount>

    @Query("SELECT COUNT(*) FROM play_history")
    suspend fun getTotalPlays(): Int

    @Query("SELECT SUM(durationMs) FROM play_history")
    suspend fun getTotalDurationMs(): Long?
}