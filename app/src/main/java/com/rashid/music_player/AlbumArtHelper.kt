package com.rashid.music_player

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever

object AlbumArtHelper {

    fun getAlbumArt(path: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val art = retriever.embeddedPicture
            retriever.release()
            if (art != null) BitmapFactory.decodeByteArray(art, 0, art.size) else null
        } catch (e: Exception) {
            null
        }
    }

    // Mbinu rahisi ya blur: tunapunguza picha iwe ndogo sana, kisha ImageView
    // inaikuza na kuilainisha, hivyo inaonekana imefifia (blurred)
    fun getBlurred(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, 24, 24, true)
    }
}