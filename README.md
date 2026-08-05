# ▄▀▄▀▄ Terminus Player ▄▀▄▀▄

Native Kotlin + Jetpack Compose. Package: `com.necroware.terminusplayer`.

## ╬Stack╬
Kotlin · Jetpack Compose (Material 3, no Material You) · Media3 ExoPlayer ·
Room · Hilt · KSP · Coil (album art) · JetBrains Mono (bundled, OFL license)
· minSdk 29 (Android 10+)

## ≡≡≡Terminal Music Player≡≡≡
An audio player for Android built entirely with Kotlin and Jetpack Compose. 
Terminus Player combines offline media playback with listening statistics, audio customization, and a componentized UI.

# █▄Features▄█

## Audio & Playback Engine
Background Playback Service: Powered by a foreground MusicService for playback uninterrupted by app lifecycle changes or device sleep.

Full Transport Controls: Mini player bar, full-screen "Now Playing" view, track scrubbing, shuffle, and repeat modes.

Built-in Equalizer: Integrated EqualizerController for fine-tuning sound profiles and audio output.

Local Media Auto-Discovery: MediaStoreScanner automatically indexes local audio files, metadata, and album art from device storage.

## Deep Listening Analytics
-Terminus Player tracks your habits locally via a dedicated PlayEvents logging engine:

Play Count & History: Tracks listening sessions, play frequency, and timestamps.

Data Visualization Suite:

- Bar Charts: Visualizes daily and weekly listening volume.

- Area Charts: Displays playback duration over time.

- Pie Charts: Breaks down top genres, artists, and albums.

- Scatter Plots: Highlights peak listening times and habits throughout the day.

## Library & Playlist Management
- Smart Organization: Browse music by Songs, Albums, Artists, and Playlists.

- Favorites & Quick Like: One-tap "Liked Songs" tracking backed by local Room DAOs.

- Custom Playlists: Create, update, and reorder custom audio queues on the fly.

- Fast Local Search: Real-time search across tracks, artists, and playlists.

## Tech Stack & Architecture
- Language: 69% Java, 31% Kotlin

- UI Framework: Jetpack Compose (Single-Activity Architecture)

- Build System: Gradle with Kotlin DSL (build.gradle.kts)

- Local Database: Room Persistence Library (Entities: Song, Playlist, LikedSong, PlayEvent)

- Audio Engine: Android MediaSession / Foreground Service API

- Architecture Pattern: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
