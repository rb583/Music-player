package com.rashid.music_player

import android.media.audiofx.Equalizer

object EqualizerManager {
    private var equalizer: Equalizer? = null

    fun init(audioSessionId: Int) {
        try {
            val savedLevels = savedBandLevels
            equalizer?.release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEnabled
            }
            savedLevels?.let { levels ->
                for (i in levels.indices) {
                    try {
                        equalizer?.setBandLevel(i.toShort(), levels[i])
                    } catch (e: Exception) { }
                }
            }
        } catch (e: Exception) { }
    }

    var isEnabled: Boolean = false
        set(value) {
            field = value
            equalizer?.enabled = value
        }

    private var savedBandLevels: ShortArray? = null

    fun getNumberOfBands(): Int = equalizer?.numberOfBands?.toInt() ?: 0

    fun getBandLevelRange(): ShortArray = equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)

    fun getBandFreq(band: Int): Int = equalizer?.getCenterFreq(band.toShort())?.div(1000) ?: 0

    fun getBandLevel(band: Int): Short = equalizer?.getBandLevel(band.toShort()) ?: 0

    fun setBandLevel(band: Int, level: Short) {
        equalizer?.setBandLevel(band.toShort(), level)
        val bands = getNumberOfBands()
        if (savedBandLevels == null || savedBandLevels!!.size != bands) {
            savedBandLevels = ShortArray(bands)
        }
        savedBandLevels!![band] = level
    }
}