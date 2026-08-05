# TERMINUS (native) — Phase 1.5

Native Kotlin + Jetpack Compose. Package: `com.necroware.terminusplayer`.

## Stack
Kotlin · Jetpack Compose (Material 3, no Material You) · Media3 ExoPlayer ·
Room · Hilt · KSP · Coil (album art) · JetBrains Mono (bundled, OFL license)
· minSdk 29 (Android 10+)

## Opening in Android Studio
Same as before — open the folder, let Gradle sync, run. If you see the
"SDK location not found" error, create `local.properties` with your SDK
path (see earlier conversation / Android Studio auto-fixes this on sync).

## ⚠️ Important: Room schema bumped to v2
`PlayEventEntity` gained an `albumId` field (needed for reliable per-album
stats — filtering by album *title* string was unreliable across artists
with same-named albums). The database uses `fallbackToDestructiveMigration()`
during this active-development phase, which means **your liked songs and
listening history reset once on this update**, then accumulate normally
again. This needs to become a real `Migration` object before any real
release — flagging it so it's not forgotten.

## What's new this pass
- **Theme**: dark grey background + cream body text + **orange** as the
  dedicated "symbol" accent color (icons, transport glyphs, liked-heart,
  chart bars, prompt carets) — distinct from body text color, per your ask.
- **Font**: JetBrains Mono actually bundled (`res/font/`), wired into
  `Type.kt`. Real monospace now, not a placeholder.
- **Album art everywhere**: Library rows, Home (mix + recently played),
  mini player, and now the full Now Playing screen. Threaded through via
  `MediaItem` extras (`albumId`) so the player screen doesn't need a
  separate lookup.
- **ASCII transport controls**: shuffle/prev/play-pause/next/repeat as
  plain typographic glyphs (not Material icons, not emoji) — see
  `ui/components/TransportControls.kt`. Used in both the full player and
  the mini player.
- **Floating mini player**: rounded pill, inset margins, now includes
  prev/next (previously play/pause only).
- **Now Playing back button**: there was no way to leave the full player
  before this — fixed with a `︿` collapse glyph.
- **Slide-up + fade transition** into/out of Now Playing.
- **Album/Artist detail screens** (full screen, per your call): tapping an
  album or artist in Library now navigates to a dedicated screen with the
  track list, a "▶ PLAY ALL" action, and real mini-stats — total plays,
  time listened, and top track — computed from actual listening history,
  not placeholders.

## Known gaps (unchanged from last pass, still deferred on purpose)
- **Playlists**: still a stub.
- **Settings**: still a stub (theme mode / accent picker / nav style —
  the color infrastructure for an accent picker already exists in
  `Color.kt`, just needs a UI + DataStore persistence).
- **Stats**: Layer 1 only (summary numbers, weekly bar chart, top artists,
  range switcher). Layers 2+ (concentration donut, session stats, and the
  line/pie/area/scatter/box chart types) not yet built.
- **Dynamic color from album art**: discussed, intentionally deferred —
  real feature, own pass later.
- Queue reordering, crossfade slider, gapless toggle: not yet built.
- Genres tab: skipped (MediaStore genre data is unreliable across OEMs).

## Architecture notes (updated)
- `Song → MediaItem` mapping now stashes `albumId` in `MediaMetadata.extras`
  (see `util/MediaItemMapper.kt`) so `PlaybackController` can read it back
  into `NowPlayingState` without a second repository query.
- `MusicService` now runs a `Player.Listener` that logs a `PlayEventEntity`
  (with `albumId`) whenever a track changes or the service is destroyed,
  ignoring sub-3-second accidental taps. This is what feeds Recently
  Played, Your Mix, Stats, and the new Album/Artist mini-stats.
- Album/Artist detail screens each have their own `SavedStateHandle`-backed
  Hilt ViewModel reading the nav arg (`albumId: Long` / `artist: String`)
  directly — no shared "selected item" state needed.

## Latest pass — polish + album merge fix
- **Album grouping fixed**: albums were fragmenting (e.g. "Die Lit" showing
  as 7+ separate entries) because MediaStore assigns a distinct albumId per
  track when per-track artist/feature tags differ. Albums are now grouped
  and navigated by TITLE, not raw albumId — `AlbumDetail` route param
  changed from `albumId: Long` to `albumTitle: String` accordingly, and all
  related DAO queries/repository methods were updated to match (see
  `SongDao.observeSongsByAlbumTitle`, `PlayEventDao.statsForAlbumTitle` /
  `topSongForAlbumTitle`).
- **Seek bar**: thinner segments (44 of them, 8dp tall, 1.5dp gaps) —
  reads as a continuous textured bar rather than obviously chunky blocks.
- **Shuffle/repeat glyphs**: were unclear ("x2", "<->") — now plain text
  labels (SHUF / RPT / RPT1) in their bordered boxes.
- **Slide transitions**: bottom-nav tab switches now slide left/right based
  on tab order (instead of the implicit crossfade), and Album/Artist detail
  screens push in from the right / pop back out to the right, matching
  standard "drill in" navigation. Now Playing keeps its vertical slide.
- **Swipe-down-to-dismiss** on Now Playing — drag the screen down past ~25%
  of its height to collapse back to mini player, or it snaps back. The
  `︿` button still works too.

## Optimization status
Album art decode-size fix and reduced position-polling frequency are in
from the last pass. You mentioned it's "not quite there yet" — noted, not
chasing further perf right now per your call; flag specifics whenever you
want another pass at it.

## Explicitly deferred (your call — not touching until you say go)
Codec/EQ selection, per-file customization, color picker / UI alternates
(button shapes, font selection, Material U ↔ terminal aesthetic switching).
Stats layers 2+ and Playlists remain queued as previously agreed.

## Latest pass — Playlists, like button, smooth slider, finished Stats Layer 1.5, Home redesign

### Real bug fixes
- **Seek bar smoothness**: the actual fix this time — position now advances
  via a per-frame interpolation (`rememberSmoothPosition` in
  `NowPlayingScreen`) between the ViewModel's polled ticks, so it's silky
  at 60fps regardless of backend polling cadence. Previous passes only
  changed the bar's visual style, which didn't address the real cause.
- **Nav label wrapping**: "PLAYLISTS" was wrapping to a second line.
  Explicit small font size + `maxLines = 1` + `softWrap = false` fixes it
  for good rather than just shrinking text further.
- **Like button**: there was no way to like a song from Now Playing —
  added a `[♥]`/`[♡]` toggle next to the collapse button, wired through
  `PlaybackViewModel.isCurrentLiked` / `toggleCurrentLike()`.

### New features
- **Real Playlists screen**: Liked Songs, Recently Played, and Most Played
  as auto-playlists, each with its own detail screen (track list, play-all).
  Custom user-created playlists still not built — noted in the screen
  itself ("custom playlists coming later").
- **Stats insights**: rule-based observations at the top of Stats —
  top artist this week, yesterday's most-repeated song, and an artist
  you played a lot this month but not in the past week. Simple heuristics
  over the existing play_events data, not a recommendation engine — will
  say nothing rather than force a message when there isn't enough signal.
- **Stats top categories toggle**: Song / Album / Artist tabs, each showing
  a ranked list for the selected date range.
- **Home screen redesign**: a neofetch-style `SystemInfoCard` (device,
  Android version, track/liked counts, this week's plays+time) using the
  app's own `>_` glyph rather than hand-drawn ASCII art — safer than
  encoding pixel art blind without on-device alignment testing. Bigger
  Your Mix thumbnails (84dp → 96dp).

### Known gaps, still explicit
- **Custom user playlists** (create/rename/delete/add-to) not built —
  the three auto-playlists cover "liked songs and stuff" per your ask,
  but manual playlist creation is a separate, real feature.
- **Stats**: still missing the concentration donut, session-length stats,
  and the line/pie/area/scatter/box canvas chart types. Insights + top
  categories were the priority this round; the remaining chart types are
  next whenever you want them.
- **Settings**: still a stub — album art style switching (CRT/static,
  vinyl, cassette per your reference images), theme/accent picker, and
  general customization all queued there, explicitly deferred per your
  "we'll see for Settings" note.
- **Codec selection + EQ**: still parked in Settings' future scope, not
  started.
