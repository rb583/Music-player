package com.rashid.music_player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private lateinit var emptyHistoryText: TextView
    private lateinit var recyclerRecentlyPlayed: RecyclerView

    private val playerListener: () -> Unit = {
        loadHistory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        emptyHistoryText = view.findViewById(R.id.empty_history_text)
        recyclerRecentlyPlayed = view.findViewById(R.id.recycler_recently_played)
        recyclerRecentlyPlayed.layoutManager = LinearLayoutManager(requireContext())

        loadHistory()
        MusicPlayerManager.addListener(playerListener)

        return view
    }

    private fun loadHistory() {
        val context = context ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(context).playHistoryDao()
            val recentlyPlayed = dao.getRecentlyPlayed(50)

            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext

                if (recentlyPlayed.isEmpty()) {
                    emptyHistoryText.visibility = View.VISIBLE
                    recyclerRecentlyPlayed.visibility = View.GONE
                } else {
                    emptyHistoryText.visibility = View.GONE
                    recyclerRecentlyPlayed.visibility = View.VISIBLE

                    recyclerRecentlyPlayed.adapter = HistoryAdapter(
                        recentlyPlayed.map { HistoryItem.Recent(it) }
                    ) { song -> playSongAndNavigate(song) }
                }
            }
        }
    }

    private fun playSongAndNavigate(song: Song) {
        MusicPlayerManager.playSong(song)
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.selectedItemId = R.id.nav_player
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MusicPlayerManager.removeListener(playerListener)
    }
}