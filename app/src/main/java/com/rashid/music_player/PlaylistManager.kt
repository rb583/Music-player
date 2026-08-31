package com.rashid.music_player

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PlaylistManager {
    private const val PREFS_NAME = "playlist_prefs"
    private const val KEY_PLAYLISTS = "playlists_json"

    fun getPlaylists(context: Context): MutableList<Playlist> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PLAYLISTS, null) ?: return mutableListOf()

        val result = mutableListOf<Playlist>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.getString("name")
            val idsArray = obj.getJSONArray("songIds")
            val ids = mutableListOf<Long>()
            for (j in 0 until idsArray.length()) {
                ids.add(idsArray.getLong(j))
            }
            result.add(Playlist(name, ids))
        }
        return result
    }

    private fun savePlaylists(context: Context, playlists: List<Playlist>) {
        val array = JSONArray()
        for (playlist in playlists) {
            val obj = JSONObject()
            obj.put("name", playlist.name)
            obj.put("songIds", JSONArray(playlist.songIds))
            array.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PLAYLISTS, array.toString()).apply()
    }

    fun createPlaylist(context: Context, name: String) {
        val playlists = getPlaylists(context)
        if (playlists.any { it.name == name }) return
        playlists.add(Playlist(name))
        savePlaylists(context, playlists)
    }

    fun deletePlaylist(context: Context, name: String) {
        val playlists = getPlaylists(context)
        playlists.removeAll { it.name == name }
        savePlaylists(context, playlists)
    }

    fun addSongToPlaylist(context: Context, playlistName: String, songId: Long) {
        val playlists = getPlaylists(context)
        val playlist = playlists.find { it.name == playlistName } ?: return
        if (!playlist.songIds.contains(songId)) {
            playlist.songIds.add(songId)
        }
        savePlaylists(context, playlists)
    }

    fun removeSongFromPlaylist(context: Context, playlistName: String, songId: Long) {
        val playlists = getPlaylists(context)
        val playlist = playlists.find { it.name == playlistName } ?: return
        playlist.songIds.remove(songId)
        savePlaylists(context, playlists)
    }
}