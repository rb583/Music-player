package com.rashid.music_player

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemContainer: View = view.findViewById(R.id.song_item_container)
        val title: TextView = view.findViewById(R.id.song_title)
        val artist: TextView = view.findViewById(R.id.song_artist)
        val favoriteBtn: ImageButton = view.findViewById(R.id.favorite_btn)
        val albumArt: ImageView = view.findViewById(R.id.album_art)
        val playingIndicator: ImageView = view.findViewById(R.id.playing_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val context = holder.itemView.context
        holder.title.text = song.title
        holder.artist.text = song.artist

        val art = AlbumArtHelper.getAlbumArt(song.path)
        if (art != null) {
            holder.albumArt.setImageBitmap(art)
        } else {
            holder.albumArt.setImageResource(android.R.drawable.ic_menu_agenda)
        }

        // Active playing song highlight
        val isCurrentPlaying = MusicPlayerManager.currentSong?.id == song.id
        if (isCurrentPlaying) {
            holder.playingIndicator.visibility = View.VISIBLE
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.teal_accent))
            holder.itemContainer.setBackgroundColor(Color.parseColor("#1F00E5FF"))
        } else {
            holder.playingIndicator.visibility = View.GONE

            val textTypedValue = TypedValue()
            val hasColor = context.theme.resolveAttribute(android.R.attr.textColorPrimary, textTypedValue, true)
            if (hasColor) {
                val textColor = if (textTypedValue.resourceId != 0) {
                    ContextCompat.getColor(context, textTypedValue.resourceId)
                } else {
                    textTypedValue.data
                }
                holder.title.setTextColor(textColor)
            }

            val selectableTypedValue = TypedValue()
            val hasSelectable = context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableTypedValue, true)
            if (hasSelectable && selectableTypedValue.resourceId != 0) {
                holder.itemContainer.setBackgroundResource(selectableTypedValue.resourceId)
            } else {
                holder.itemContainer.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        holder.itemView.setOnClickListener { onSongClick(song) }

        updateFavoriteIcon(holder, song, context)

        holder.favoriteBtn.setOnClickListener {
            FavoritesManager.toggleFavorite(context, song.id)
            updateFavoriteIcon(holder, song, context)
        }
    }

    private fun updateFavoriteIcon(holder: SongViewHolder, song: Song, context: Context) {
        val isFav = FavoritesManager.isFavorite(context, song.id)
        holder.favoriteBtn.setImageResource(
            if (isFav) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun getItemCount() = songs.size
}