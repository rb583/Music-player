package com.rashid.music_player

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private lateinit var miniPlayer: android.widget.LinearLayout
    private lateinit var miniTitle: TextView
    private lateinit var miniPlayPause: ImageButton
    private lateinit var miniAlbumArt: ImageView

    private var isNavigatingFromBackStack = false
    private val playerListener: () -> Unit = { updateMiniPlayer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        MusicPlayerManager.appContext = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContentView(R.layout.activity_main)

        miniPlayer = findViewById(R.id.mini_player)
        miniTitle = findViewById(R.id.mini_song_title)
        miniPlayPause = findViewById(R.id.mini_play_pause)
        miniAlbumArt = findViewById(R.id.mini_album_art)

        miniPlayPause.setOnClickListener {
            MusicPlayerManager.togglePlayPause()
        }

        miniPlayer.setOnClickListener {
            findViewById<BottomNavigationView>(R.id.bottom_nav).selectedItemId = R.id.nav_player
        }

        MusicPlayerManager.addListener(playerListener)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LibraryFragment())
                .commit()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            val targetItemId = when (currentFragment) {
                is LibraryFragment -> R.id.nav_library
                is FavoritesFragment -> R.id.nav_favorites
                is HistoryFragment -> R.id.nav_history
                is PlayerFragment -> R.id.nav_player
                is PlaylistsFragment, is PlaylistDetailFragment -> R.id.nav_playlists
                is SettingsFragment, is EqualizerFragment -> R.id.nav_settings
                else -> null
            }
            if (targetItemId != null && bottomNav.selectedItemId != targetItemId) {
                isNavigatingFromBackStack = true
                bottomNav.selectedItemId = targetItemId
                isNavigatingFromBackStack = false
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            if (isNavigatingFromBackStack) return@setOnItemSelectedListener true

            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            val isSameTab = when (item.itemId) {
                R.id.nav_library -> currentFragment is LibraryFragment
                R.id.nav_favorites -> currentFragment is FavoritesFragment
                R.id.nav_history -> currentFragment is HistoryFragment
                R.id.nav_player -> currentFragment is PlayerFragment
                R.id.nav_playlists -> currentFragment is PlaylistsFragment
                R.id.nav_settings -> currentFragment is SettingsFragment
                else -> false
            }

            if (!isSameTab) {
                val selected: Fragment = when (item.itemId) {
                    R.id.nav_favorites -> FavoritesFragment()
                    R.id.nav_history -> HistoryFragment()
                    R.id.nav_playlists -> PlaylistsFragment()
                    R.id.nav_player -> PlayerFragment()
                    R.id.nav_settings -> SettingsFragment()
                    else -> LibraryFragment()
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selected)
                    .addToBackStack(null)
                    .commit()
            }
            true
        }
    }

    private fun updateMiniPlayer() {
        val song = MusicPlayerManager.currentSong
        if (song == null) {
            miniPlayer.visibility = android.view.View.GONE
            return
        }
        miniPlayer.visibility = android.view.View.VISIBLE
        miniTitle.text = song.title
        miniPlayPause.setImageResource(
            if (MusicPlayerManager.isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
        val art = AlbumArtHelper.getAlbumArt(song.path)
        if (art != null) miniAlbumArt.setImageBitmap(art)
        else miniAlbumArt.setImageResource(android.R.drawable.ic_menu_agenda)
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayerManager.removeListener(playerListener)
    }
}