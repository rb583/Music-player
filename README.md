# 🎵 Offline Music Player

An offline music player Android app built natively in **Kotlin**, using **Android Studio**. The app scans and plays music files stored locally on the device — no internet connection required.

## ✨ Features

- **Library** — Browse all music on your device, with search and advanced sorting (**Title A-Z/Z-A**, **Artist A-Z**, **Date Added Newest/Oldest**).
- **Listening History & Stats** — Dedicated screen showing:
  - **Listening Stats**: Total songs played & total time listened (hours/minutes).
  - **Recently Played**: Songs played recently with relative timestamps.
  - **Most Played Songs**: Frequency ranking of your most played tracks.
  - **Top Artists**: Your most listened-to artists.
- **Player** — Play/pause, next/previous, seekbar with live time display, shuffle, and repeat (off/one/all).
- **Favorites** — Mark songs as favorites and view them in a dedicated tab.
- **Playlists** — Create custom playlists and add/remove songs.
- **Album Art** — Automatically displays embedded album artwork from audio files.
- **Sleep Timer** — Automatically pause playback after a set duration.
- **Equalizer** — Adjust frequency bands to customize sound output.
- **Mini Player** — Persistent playback bar visible across all screens.
- **Notification Controls** — Boomplay-style media notification with album art, play/pause/next/previous controls, and a live progress bar — works even when the app is closed.
- **Ocean Sapphire Theme & Theme Support** — Modern Ocean Sapphire palette with support for Light, Dark, or Follow System themes.
- **Custom Adaptive App Icon** — Modern Minimalist Vinyl record icon design.

## 🛠️ Built With

- **Kotlin** — Primary programming language.
- **Android Views (XML)** — UI layer.
- **Room Database (Room KTX & KSP)** — Persistent play history and listening stats tracking.
- **Kotlin Coroutines** — Asynchronous data fetching and database operations.
- **MediaPlayer** — Audio playback engine.
- **MediaSessionCompat** — System media session integration for notification and lock screen controls.
- **Equalizer (android.media.audiofx)** — Audio frequency adjustment.
- **MediaStore API** — Reading device audio files & metadata.
- **SharedPreferences** — Storing favorites, playlists, and theme preferences.
- **RecyclerView & NestedScrollView** — Smooth list displays and scrollable stats.
- **Material Components** — Bottom Navigation and Material 3 UI styling.

## 📱 Screens

| Screen | Description |
|---|---|
| Library | List of all songs on device with search & multi-criterion sorting |
| History & Stats | Listening stats, Recently Played, Most Played, and Top Artists |
| Player | Now playing screen with full playback controls |
| Favorites | Songs marked as favorite |
| Playlists | User-created playlists |
| Settings | Theme selection, total song count, listening stats summary, equalizer access |

## 🚀 Getting Started

1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/Music-player.git
   ```
2. Open the project in Android Studio.
3. Let Gradle sync complete.
4. Run the app on a device or emulator (Android 7.0 / API 24 or higher).
5. Grant the requested permissions (media access, notifications) when prompted.

## 📋 Permissions Used

- `READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE` — To access music files on the device.
- `POST_NOTIFICATIONS` — To show playback controls in the notification shade.
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK` — To keep music playing in the background.

## 📸 Screenshots

| Library | Player | Settings |
|---|---|---|
| ![Library](Screenshots/Library.jpg) | ![Player](Screenshots/Player.jpg) | ![Settings](Screenshots/Settings.jpg) |

## 📄 License

This project is open for personal and educational use.
