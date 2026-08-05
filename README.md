<img width="540" height="1273" alt="ef3178e0-aca4-4a79-b8d4-8c5d191ade30" src="https://github.com/user-attachments/assets/3ae426c4-7844-410b-8112-4f1687930e8c" /> <img width="540" height="1273" alt="297dc6d1-38f6-4416-9a71-df40ec83062f" src="https://github.com/user-attachments/assets/843000f0-4706-4a61-a686-a8ff180b66cb" /> <img width="540" height="1273" alt="9915d781-6037-4c48-bc16-68e66f04307b" src="https://github.com/user-attachments/assets/00d82bcb-ff10-4d2d-a3db-b9ed89497e59" /> <img width="540" height="1273" alt="2b67136b-2df2-4bd9-9287-b8491738bd74" src="https://github.com/user-attachments/assets/991b869b-7602-4fcb-8750-f73aafd3e879" />
<img width="540" height="1273" alt="d818dffd-5b06-41c2-a2d8-34609f9518f8" src="https://github.com/user-attachments/assets/0cd58323-43a8-4732-8646-57e2db04ff44" />











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
