# 🎵 Offline Music Player

An offline music player Android app built natively in **Kotlin**, using **Android Studio**. The app scans and plays music files stored locally on the device — no internet connection required.

## ✨ Features

- **Library** — browse all music on your device, with search and sort (Title A-Z/Z-A, Artist A-Z)
- **Player** — play/pause, next/previous, seekbar with live time display, shuffle, and repeat (off/one/all)
- **Favorites** — mark songs as favorites and view them in a dedicated tab
- **Playlists** — create custom playlists and add/remove songs from them
- **Album Art** — automatically displays embedded album artwork from audio files
- **Sleep Timer** — automatically pause playback after a set duration
- **Equalizer** — adjust frequency bands to customize sound output
- **Mini Player** — persistent playback bar visible across all screens
- **Notification Controls** — Boomplay-style media notification with album art, play/pause/next/previous controls, and a live progress bar — works even when the app is closed
- **Theme Support** — switch between Light, Dark, or Follow System theme

## 🛠️ Built With

- **Kotlin** — primary language
- **Android Views (XML)** — UI layer
- **MediaPlayer** — audio playback
- **MediaSessionCompat** — system media session integration for notification and lock screen controls
- **Equalizer (android.media.audiofx)** — audio frequency adjustment
- **MediaStore API** — reading device audio files
- **SharedPreferences** — storing favorites, playlists, and theme preference
- **RecyclerView** — displaying song and playlist lists
- **Material Components** — Bottom Navigation and UI styling

## 📱 Screens

| Screen | Description |
|---|---|
| Library | List of all songs on device with search & sort |
| Player | Now playing screen with full playback controls |
| Favorites | Songs marked as favorite |
| Playlists | User-created playlists |
| Settings | Theme selection, song count, equalizer access |

## 🚀 Getting Started

1. Clone this repository
2. Open the project in Android Studio
3. Let Gradle sync complete
4. Run the app on a device or emulator (Android 7.0 / API 24 or higher)
5. Grant the requested permissions (media access, notifications) when prompted

## 📋 Permissions Used

- `READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE` — to access music files on the device
- `POST_NOTIFICATIONS` — to show playback controls in the notification shade
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK` — to keep music playing in the background

## 📸 Screenshots
## 📸 Screenshots

| Library | Player | Settings |
|---|---|---|
| ![Library](Screenshots/Library.jpg) | ![Player](Screenshots/Player.jpg) | ![Settings](Screenshots/Settings.jpg) |


## 📄 License

This project is open for personal and educational use.
