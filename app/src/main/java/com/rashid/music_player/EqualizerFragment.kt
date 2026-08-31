package com.rashid.music_player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment

class EqualizerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_equalizer, container, false)

        val eqSwitch = view.findViewById<Switch>(R.id.eq_switch)
        val bandsContainer = view.findViewById<LinearLayout>(R.id.bands_container)

        eqSwitch.isChecked = EqualizerManager.isEnabled
        eqSwitch.setOnCheckedChangeListener { _, isChecked ->
            EqualizerManager.isEnabled = isChecked
        }

        val numberOfBands = EqualizerManager.getNumberOfBands()
        if (numberOfBands == 0) {
            val msg = TextView(requireContext())
            msg.text = "Play a song first to enable the equalizer"
            bandsContainer.addView(msg)
        } else {
            val range = EqualizerManager.getBandLevelRange()
            val minLevel = range[0]
            val maxLevel = range[1]

            for (band in 0 until numberOfBands) {
                val freqLabel = TextView(requireContext())
                freqLabel.text = "${EqualizerManager.getBandFreq(band)} Hz"
                freqLabel.setPadding(0, 24, 0, 4)
                bandsContainer.addView(freqLabel)

                val seekBar = SeekBar(requireContext())
                seekBar.max = (maxLevel - minLevel).toInt()
                seekBar.progress = (EqualizerManager.getBandLevel(band) - minLevel).toInt()

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            val level = (progress + minLevel).toShort()
                            EqualizerManager.setBandLevel(band, level)
                        }
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })

                bandsContainer.addView(seekBar)
            }
        }

        return view
    }
}