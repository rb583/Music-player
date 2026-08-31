package com.rashid.music_player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.song_title)
        val artist: TextView = view.findViewById(R.id.song_artist)
        val favoriteBtn: ImageButton = view.findViewById(R.id.favorite_btn)
        val albumArt: android.widget.ImageView = view.findViewById(R.id.album_art)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.artist.text = song.artist
        val art = AlbumArtHelper.getAlbumArt(song.path)
        if (art != null) {
            holder.albumArt.setImageBitmap(art)
        } else {
            holder.albumArt.setImageResource(android.R.drawable.ic_menu_agenda)
        }
        holder.itemView.setOnClickListener { onSongClick(song) }

        val context = holder.itemView.context
        updateFavoriteIcon(holder, song, context)

        holder.favoriteBtn.setOnClickListener {
            FavoritesManager.toggleFavorite(context, song.id)
            updateFavoriteIcon(holder, song, context)
        }
    }

    private fun updateFavoriteIcon(holder: SongViewHolder, song: Song, context: android.content.Context) {
        val isFav = FavoritesManager.isFavorite(context, song.id)
        holder.favoriteBtn.setImageResource(
            if (isFav) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun getItemCount() = songs.size
}