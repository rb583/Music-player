package com.rashid.music_player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArtistStatAdapter(
    private val artists: List<ArtistPlayCount>
) : RecyclerView.Adapter<ArtistStatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView = view.findViewById(R.id.artist_rank)
        val name: TextView = view.findViewById(R.id.artist_name)
        val playCount: TextView = view.findViewById(R.id.artist_play_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = artists[position]
        holder.rank.text = "#${position + 1}"
        holder.name.text = if (artist.artist.isNotBlank()) artist.artist else "Unknown Artist"
        holder.playCount.text = "${artist.playCount} plays"
    }

    override fun getItemCount(): Int = artists.size
}