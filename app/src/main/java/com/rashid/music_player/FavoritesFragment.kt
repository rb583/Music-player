package com.rashid.music_player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_favorites)
        val emptyText = view.findViewById<TextView>(R.id.empty_favorites_text)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val favoriteIds = FavoritesManager.getFavorites(requireContext())
        val favoriteSongs = MusicPlayerManager.songList.filter { favoriteIds.contains(it.id.toString()) }

        if (favoriteSongs.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = SongAdapter(favoriteSongs) { song ->
                MusicPlayerManager.playSong(song)
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
                    .selectedItemId = R.id.nav_player
            }
        }
        return view
    }
}