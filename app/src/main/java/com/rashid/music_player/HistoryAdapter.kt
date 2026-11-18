package com.rashid.music_player

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

sealed class HistoryItem {
    data class Recent(val entity: PlayHistoryEntity) : HistoryItem()
    data class MostPlayed(val stat: SongPlayCount) : HistoryItem()
}

class HistoryAdapter(
    private val items: List<HistoryItem>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val albumArt: ImageView = view.findViewById(R.id.history_album_art)
        val title: TextView = view.findViewById(R.id.history_song_title)
        val artist: TextView = view.findViewById(R.id.history_song_artist)
        val info: TextView = view.findViewById(R.id.history_play_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_song, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HistoryItem.Recent -> {
                val entity = item.entity
                holder.title.text = entity.title
                holder.artist.text = entity.artist
                val timeAgo = DateUtils.getRelativeTimeSpanString(
                    entity.playedAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                holder.info.text = timeAgo

                val art = AlbumArtHelper.getAlbumArt(entity.path)
                if (art != null) holder.albumArt.setImageBitmap(art)
                else holder.albumArt.setImageResource(android.R.drawable.ic_menu_agenda)

                holder.itemView.setOnClickListener {
                    val song = MusicPlayerManager.songList.find { it.id == entity.songId }
                        ?: Song(entity.songId, entity.title, entity.artist, entity.path)
                    onSongClick(song)
                }
            }
            is HistoryItem.MostPlayed -> {
                val stat = item.stat
                holder.title.text = stat.title
                holder.artist.text = stat.artist
                holder.info.text = "${stat.playCount} plays"

                val art = AlbumArtHelper.getAlbumArt(stat.path)
                if (art != null) holder.albumArt.setImageBitmap(art)
                else holder.albumArt.setImageResource(android.R.drawable.ic_menu_agenda)

                holder.itemView.setOnClickListener {
                    val song = MusicPlayerManager.songList.find { it.id == stat.songId }
                        ?: Song(stat.songId, stat.title, stat.artist, stat.path)
                    onSongClick(song)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}