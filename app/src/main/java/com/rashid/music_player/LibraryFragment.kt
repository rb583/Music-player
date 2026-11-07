package com.rashid.music_player

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LibraryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var countHeader: TextView
    private lateinit var searchBar: EditText
    private lateinit var sortSpinner: Spinner
    private var currentSort = "Title (A-Z)"

    private var allSongs: List<Song> = emptyList()

    private val playerListener: () -> Unit = {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) loadSongs()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        recyclerView = view.findViewById(R.id.recycler_songs)
        emptyText = view.findViewById(R.id.empty_text)
        countHeader = view.findViewById(R.id.song_count_header)
        searchBar = view.findViewById(R.id.search_bar)
        sortSpinner = view.findViewById(R.id.sort_spinner)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load saved sort preference
        val prefs = requireContext().getSharedPreferences("library_prefs", Context.MODE_PRIVATE)
        currentSort = prefs.getString("saved_sort_mode", "Title (A-Z)") ?: "Title (A-Z)"

        val sortOptions = arrayOf(
            "Title (A-Z)",
            "Title (Z-A)",
            "Artist (A-Z)",
            "Date Added (Newest)",
            "Date Added (Oldest)"
        )
        sortSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions)

        val initialPosition = sortOptions.indexOf(currentSort).coerceAtLeast(0)
        sortSpinner.setSelection(initialPosition)

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = sortOptions[position]
                prefs.edit().putString("saved_sort_mode", currentSort).apply()
                filterSongs(searchBar.text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterSongs(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        MusicPlayerManager.addListener(playerListener)
        checkPermissionAndLoad()
        return view
    }

    private fun checkPermissionAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadSongs()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun loadSongs() {
        val songList = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        requireContext().contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                songList.add(
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

        allSongs = songList
        MusicPlayerManager.songList = songList
        filterSongs(searchBar.text.toString())
    }

    private fun filterSongs(query: String) {
        var filtered = if (query.isBlank()) {
            allSongs
        } else {
            allSongs.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist?.contains(query, ignoreCase = true) == true
            }
        }

        filtered = when (currentSort) {
            "Title (Z-A)" -> filtered.sortedByDescending { it.title.lowercase() }
            "Artist (A-Z)" -> filtered.sortedBy { it.artist?.lowercase() ?: "" }
            "Date Added (Newest)" -> filtered.sortedByDescending { it.dateAdded }
            "Date Added (Oldest)" -> filtered.sortedBy { it.dateAdded }
            else -> filtered.sortedBy { it.title.lowercase() }
        }

        displaySongs(filtered)
    }

    private fun displaySongs(list: List<Song>) {
        countHeader.text = "${allSongs.size} songs"

        if (list.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = SongAdapter(list) { song ->
                MusicPlayerManager.playSong(song)
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
                    .selectedItemId = R.id.nav_player
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MusicPlayerManager.removeListener(playerListener)
    }
}