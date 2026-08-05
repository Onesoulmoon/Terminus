# Changelog

All notable changes to TERMINUS are documented here.

The format is loosely based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Latest pass (2)
- **Dropped the audio visualizer entirely** — the `android.media.audiofx.
  Visualizer`-based L/R meters and FFT spectrum line never reliably
  produced data on-device even after adding `RECORD_AUDIO`. Rather than
  ship a permanently-flat "visualizer" (worse than none at all), removed
  it along with the now-unnecessary `RECORD_AUDIO` and
  `MODIFY_AUDIO_SETTINGS` permissions and the `SpectrumVisualizerController`
  class entirely. One less permission prompt for a feature that wasn't
  working.
- **Fixed a real album-art blur bug**: art was blurry in Now Playing
  specifically. Root cause — `SongArt`'s in-memory cache was keyed only
  by file URI, not by the requested pixel size, so a low-res thumbnail
  cached from a small view (mini player, library row) got reused and
  stretched for the much bigger Now Playing display. Cache key now
  includes the requested size.
- **Audio Monitor redesigned**: replaced the non-working L/R levels and
  spectrum line with real codec, real sample rate, real output device
  (speaker/wired/bluetooth), and a real (approximate) CPU-usage readout
  for this app's process — all genuinely computed, none of it faked.
  Moved to sit below the transport controls instead of above the art.
- **Now Playing reordered and resized**: album art is now big and at the
  top (300dp, up from 200-220dp) instead of small and below the monitor
  panel; transport control buttons are bigger too.
- **Stats restructuring**: removed the standalone day-based "Listening
  Timeline" line chart (it was redundant with the WEEK bar chart and
  didn't fit Month/Year/All-Time, which already have their own chart
  types). TODAY's chart is now an hour-by-hour line graph — a genuine
  "Listening Timeline, by hour" — instead of the histogram, built from a
  full 0-23 hour array so gaps in listening show as real gaps rather than
  being compressed out. Session stat tiles (avg/total/longest session)
  now show unconditionally instead of being tied to the chart that no
  longer exists there.

### Previous pass
- **Removed** the Now Playing "song changelog" section added last pass —
  reconsidered per feedback; the Audio Monitor's spectrum line already
  fills that space, and the changelog wasn't showing what was wanted
  (queue/next-up context) anyway.
- **Fixed a real album-art bug**: some songs displayed a totally different
  song's cover art. Root cause — art was looked up via the legacy shared
  `content://media/external/audio/albumart/{albumId}` table, keyed by a
  numeric ID Android can leave mapped to stale/reused artwork. Replaced
  with `SongArt`, which loads each song's art directly from its own file
  via `ContentResolver.loadThumbnail` (per-file, can't cross-contaminate).
  This touched every screen that shows art (Home, Library, Now Playing,
  mini player, Album/Artist/Playlist detail).
- **Fixed swipe-down-to-dismiss regression**: introduced last pass when
  Now Playing's content Column became a scrollable LazyColumn (to fit the
  changelog) — the LazyColumn's own scroll gesture competed with and won
  over the custom drag-to-dismiss gesture. Reverted to a plain Column now
  that the changelog is gone, since content fits without scrolling again.
- **Fixed "Today" stats never resetting**: `TODAY` was a rolling 24-hour
  window (`now - 24h`), not a calendar day, so it never actually reset at
  midnight like the label implies. Now computed from local midnight.
  Week/Month/Year remain rolling windows for now.
- Stats: `TODAY` now shows an **Hourly Activity histogram** instead of the
  bar chart (which was showing a multi-day view under a tab called
  "Today", which didn't make sense) — real hourly play-count buckets in
  local time. Bar chart (plays-by-day) stays on the `WEEK` tab only.
- **Optimization**: position polling in `PlaybackViewModel` now only runs
  while something is actually collecting it (`SharingStarted.WhileSubscribed`
  on a cold flow), instead of ticking in the background for the entire
  app session regardless of whether Now Playing is even visible.
- **On the visualizer still not showing movement**: `RECORD_AUDIO` was
  added and is now requested at first launch alongside the essential
  library permission, since some devices don't deliver `Visualizer`
  capture callbacks without it even for an app's own session — this is
  the most likely real cause and the standard fix used by other
  visualizer/EQ apps. Being upfront: I can't fully verify this resolves
  it without your device's Logcat output (filtered for `Visualizer` or
  `AudioEffect`), since I have no way to test against real hardware here.
  If it's still flat after granting the permission, that log output is
  the fastest way to find the exact remaining cause rather than guessing
  again.

### Added
- Stats: Pie Chart for the Month tab (artist distribution), Area Chart for
  the Year tab (one area per month), Scatter Plot for the All-Time tab
  (day-by-day distribution) — each `StatsRange` tab now shows a chart type
  suited to it, rather than one chart type everywhere.
- Now Playing: real Audio Monitor panel — genuine L/R level meters (RMS
  computed from the Visualizer's waveform capture), a real FFT-derived
  ASCII spectrum line, real detected codec/sample-rate (via the player's
  selected audio track Format), real device volume, and real loop state.
  Replaces the earlier bar-style visualizer, which wasn't rendering
  correctly and used a visual style that didn't match what was wanted.
- Now Playing: layout reflow — content is top-aligned (was vertically
  centered) so art/controls sit higher.
- Stats: Listening Timeline section with a real Canvas-drawn line chart,
  plus session stats (average session, total sessions, longest session)
  computed via gap-based session detection over raw play events.
- Stats: rule-based insight messages (top artist this week, yesterday's
  most-repeated song, a stale-but-previously-frequent artist).
- Stats: Top Categories toggle (Song / Album / Artist).
- Home: neofetch-style system info card — ASCII terminal-screen glyph,
  live RAM usage and battery temperature, track/liked counts, week summary.
- Home: Recently Played row now auto-scrolls.
- Real Playlists screen: Liked Songs, Recently Played, and Most Played as
  auto-playlists, each with a detail screen (track list + play-all).
- Like button on Now Playing.
- Album/Artist detail screens with mini-stats (plays, time listened, top
  track).
- Swipe-down-to-dismiss on Now Playing, plus a `︿` collapse button.
- Slide transitions between bottom-nav tabs and for detail-screen
  navigation (push-style, matching direction of travel).

### Fixed
- **Compile errors from your corrected build, adopted into this copy**:
  missing `androidx.lifecycle:lifecycle-runtime-compose` dependency (real
  bug — `collectAsStateWithLifecycle()` is used throughout the app and
  wouldn't resolve without it), a few invalid `import ...weight` statements
  (`weight` is a `RowScope`/`ColumnScope` member, not a top-level import),
  a non-exhaustive `when` in `TerminalNavIcon` after `Destination` grew new
  detail-screen cases, and a couple of missing `padding` imports.
- **Critical**: Album/Artist detail navigation silently failed for any
  multi-word title (e.g. "2 ALIVE (GEEK PACK)" showed a correct track
  count but an empty track list). `URLEncoder.encode` converts spaces to
  `+`, but Navigation-Compose's path-argument decoding only reverses
  `%XX` percent-escapes, not `+` — so titles round-tripped with literal
  `+` characters instead of spaces, and the downstream SQL lookup never
  matched. Fixed by re-encoding spaces as `%20` after `URLEncoder.encode`.
- Album list fragmentation: MediaStore assigns a distinct `albumId` per
  track when per-track artist/feature tags differ, splitting one real
  album (e.g. "Die Lit") into many list entries. Albums are now grouped
  and navigated by title instead of raw `albumId`.
- Like-button and liked-song indicator were rendering as color emoji
  despite using "text" heart symbols (♥/♡) — some devices render these
  as emoji regardless of codepoint intent. Replaced with plain
  `[LIKE]`/`[LIKED]` / `[L]` text tags — no symbol, no emoji risk.
- Now Playing seek bar stutter — position now interpolates every frame
  between the ViewModel's polled ticks instead of jumping on each tick.
- Bottom-nav labels ("PLAYLISTS") wrapping to a second line.
- Missing back affordance on Now Playing (only fix was swipe or a tiny
  button — both now present).
- Kotlin bumped 1.9.24 → 2.0.21 (and KSP to the matching `2.0.21-1.0.26`)
  — the Compose compiler Gradle plugin doesn't exist for Kotlin 1.9.x.

### Changed
- Theme: dark grey background + cream body text, orange reserved
  specifically for symbols/icons/interactive glyphs (not body text).
- Font: JetBrains Mono actually bundled (was a placeholder before).
- Transport controls: ASCII bracket glyphs (`|<<` `>` `||` `>>|`,
  `SHUF`/`RPT`/`RPT1`) in bordered boxes, replacing Material icons and
  then replacing an earlier attempt at Unicode arrow glyphs (which
  rendered as clipped/misaligned artifacts on-device).
- Mini player: reverted from a floating rounded pill (which clipped its
  own buttons) back to a plain edge-to-edge bar.
- Seek bar: blocky segmented style (`[████░░░░]`), tuned across a few
  passes for segment thinness.

### Known gaps (explicit, not forgotten)
- **Settings screen still a stub** — this is where codec selection, EQ,
  library sort options (artist/album/name/date-added), and crossfade all
  belong per your ask; none of it is built yet. This is its own full pass.
- Custom user-created playlists (the three auto-playlists aren't the
  same thing as manual playlist creation/editing).
- Stats: Histogram as its own distinct chart type (currently the bar
  chart doubles for it) and a Genre top-category tab (MediaStore genre
  data isn't captured by the library scanner yet).
- Dynamic color extracted from album art (Palette-style) — discussed,
  intentionally deferred as its own future pass.
- General performance: album art decode-size and position-poll throttling
  were addressed in earlier passes; broader profiling (e.g. Library list
  scroll jank on large libraries) hasn't been done since it requires
  on-device tooling this environment doesn't have.
- The Audio Monitor's stereo L/R separation is a best-effort
  interpretation of the Visualizer's waveform byte layout (assumed
  interleaved L/R) — Android doesn't rigidly document this across every
  OEM, so treat exact per-channel accuracy as approximate.
