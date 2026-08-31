package com.rashid.music_player

import android.content.Context

object FavoritesManager {
    private const val PREFS_NAME = "favorites_prefs"
    private const val KEY_FAVORITES = "favorite_ids"

    fun getFavorites(context: Context): MutableSet<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return HashSet(prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet())
    }

    fun isFavorite(context: Context, songId: Long): Boolean {
        return getFavorites(context).contains(songId.toString())
    }

    fun toggleFavorite(context: Context, songId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = getFavorites(context)
        val idStr = songId.toString()
        if (favorites.contains(idStr)) {
            favorites.remove(idStr)
        } else {
            favorites.add(idStr)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }
}