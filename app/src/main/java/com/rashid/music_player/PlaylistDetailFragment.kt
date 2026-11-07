package com.rashid.music_player

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlaylistDetailFragment : Fragment() {

    private lateinit var playlistName: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView

    private val playerListener: () -> Unit = {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    companion object {
        private const val ARG_NAME = "playlist_name"
        fun newInstance(name: String): PlaylistDetailFragment {
            val fragment = PlaylistDetailFragment()
            val args = Bundle()
            args.putString(ARG_NAME, name)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        playlistName = arguments?.getString(ARG_NAME) ?: ""
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        view.findViewById<TextView>(R.id.playlist_detail_title).text = playlistName
        recyclerView = view.findViewById(R.id.recycler_playlist_songs)
        emptyText = view.findViewById(R.id.empty_playlist_text)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        view.findViewById<Button>(R.id.btn_add_songs).setOnClickListener {
            showAddSongsDialog()
        }

        refreshList()
        MusicPlayerManager.addListener(playerListener)
        return view
    }

    private fun refreshList() {
        val playlists = PlaylistManager.getPlaylists(requireContext())
        val playlist = playlists.find { it.name == playlistName }
        val songIds = playlist?.songIds ?: emptyList()
        val songs = MusicPlayerManager.songList.filter { songIds.contains(it.id) }

        if (songs.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = SongAdapter(songs) { song ->
                MusicPlayerManager.playSong(song)
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
                    .selectedItemId = R.id.nav_player
            }
        }
    }

    private fun showAddSongsDialog() {
        val allSongs = MusicPlayerManager.songList
        if (allSongs.isEmpty()) return

        val titles = allSongs.map { it.title }.toTypedArray()
        val playlists = PlaylistManager.getPlaylists(requireContext())
        val playlist = playlists.find { it.name == playlistName }
        val checkedItems = allSongs.map { playlist?.songIds?.contains(it.id) == true }.toBooleanArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Add songs to $playlistName")
            .setMultiChoiceItems(titles, checkedItems) { _, which, isChecked ->
                val song = allSongs[which]
                if (isChecked) {
                    PlaylistManager.addSongToPlaylist(requireContext(), playlistName, song.id)
                } else {
                    PlaylistManager.removeSongFromPlaylist(requireContext(), playlistName, song.id)
                }
            }
            .setPositiveButton("Done") { _, _ -> refreshList() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MusicPlayerManager.removeListener(playerListener)
    }
}