package com.rashid.music_player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val songCountText = view.findViewById<TextView>(R.id.song_count_text)
        songCountText.text = "Songs found: ${MusicPlayerManager.songList.size}"

        val radioGroup = view.findViewById<RadioGroup>(R.id.theme_radio_group)
        val radioLight = view.findViewById<View>(R.id.radio_light).id
        val radioDark = view.findViewById<View>(R.id.radio_dark).id
        val radioSystem = view.findViewById<View>(R.id.radio_system).id

        when (ThemeHelper.getSavedMode(requireContext())) {
            AppCompatDelegate.MODE_NIGHT_NO -> radioGroup.check(radioLight)
            AppCompatDelegate.MODE_NIGHT_YES -> radioGroup.check(radioDark)
            else -> radioGroup.check(radioSystem)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                radioLight -> AppCompatDelegate.MODE_NIGHT_NO
                radioDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            ThemeHelper.setTheme(requireContext(), mode)
            requireActivity().recreate()
        }

        view.findViewById<Button>(R.id.btn_open_equalizer).setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EqualizerFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}