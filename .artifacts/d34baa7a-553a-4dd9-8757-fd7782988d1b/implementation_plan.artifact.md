# Implementation Plan - UI Enhancements and Library Features

This plan covers increasing the mini player size, fixing the dynamic theme, adding a fast-scroll slider, a "Recently Added" playlist, and a long-click song options menu.

## User Review Required

> [!IMPORTANT]
> **Delete Action**: The "Delete" option in the song menu will prompt the user to delete the file from the device via `MediaStore`. This requires user confirmation through a system dialog.

> [!NOTE]
> **Genre Support**: `MediaStore`'s genre support can be inconsistent. The app will try to fetch the genre, but it may show "Unknown" for many files.

## Proposed Changes

### [Component] Core Data & Models
Update song models to support `genre` and recency.

#### [MODIFY] [SongEntity.kt](file:///C:/Users/USER/Downloads/terminus-native/app/src/main/java/com/necroware/terminusplayer/data/database/entity/SongEntity.kt)
- Add `genre: String` field.

#### [MODIFY] [Song.kt](file:///C:/Users/USER/Downloads/terminus-native/app/src/main/java/com/necroware/terminusplayer/data/model/Song.kt)
- Add `genre: String` field.

#### [MODIFY] [SongDao.kt](file:///C:/Users/USER/Downloads/terminus-native/app/src/main/java/com/necroware/terminusplayer/data/database/dao/SongDao.kt)
- Add `observeRecentlyAdded(limit: Int): Flow<List<SongEntity>>`.

---

### [Component] UI Components & Theme
Enhance Mini Player and Dynamic Theming.

#### [MODIFY] [MiniPlayerBar.kt](file:///C:/Users/USER/Downloads/terminus-native/app/src/main/java/com/necroware/terminusplayer/ui/components/MiniPlayerBar.kt)
- Increase height from `52.dp` to `68.dp`.
- Increase album art size to `48.dp`.
- Adjust font sizes and vertical padding.

#### [MODIFY] [MainActivityViewModel.kt](file:///C:/Users/USER/Downloads/terminus-native/app/src/main/java/com/necroware/terminusplayer/MainActivityViewModel.kt)
- Refine `Palette` color extraction for more vibrant dynamic themes.
- Base the dynamic theme on the *selected* theme's structure rather than always defaulting to `TERMINAL`.

---

### [Component] Library Features
Implement Recently Added tab, Fast Scroll, and Long-press actions.

#### [MODIFY] [LibraryViewModel.kt](file:///C:/Users/USER/Downloads/terminus-native/app/src/main/java/com/necroware/terminusplayer/ui/screens/library/LibraryViewModel.kt)
- Add `LibraryTab.RECENTLY`.
- Expose `recentlyAdded` flow.
- Add `removeFromLibrary(song: Song)` and `addToPlaylist(song: Song, playlistId: Long)`.

#### [MODIFY] [LibraryScreen.kt](file:///C:/Users/USER/Downloads/terminus-native/app/src/main/java/com/necroware/terminusplayer/ui/screens/library/LibraryScreen.kt)
- Add a new "RECENTLY" tab.
- Integrate a vertical `FastScrollHandle` component.
- Update `SongRow` to handle long clicks and show the `SongActionSheet`.

#### [NEW] [SongActionSheet.kt](file:///C:/Users/USER/Downloads/terminus-native/app/src/main/java/com/necroware/terminusplayer/ui/components/SongActionSheet.kt)
- A bottom sheet displaying song metadata (Duration, Genre, Album, Artist, Path).
- Actions: Play Next, Add to Queue, Add to Playlist, Delete.

## Verification Plan

### Manual Verification
1. **Mini Player**: Verify the bar is larger and art is more visible.
2. **Dynamic Theme**: Play songs with distinct album art colors and verify the accent color changes in the UI.
3. **Recently Added**: Check the new tab and verify songs are sorted by date added.
4. **Fast Scroll**: Drag the handle on the right of the song list and verify rapid scrolling.
5. **Long Click**: Long-press a song, verify the bottom sheet appears with correct info, and test "Add to Queue".
6. **Delete**: Verify the system prompt appears when selecting Delete.
