package com.necroware.terminusplayer.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.ui.components.SongActionSheet
import com.necroware.terminusplayer.ui.components.SongArt
import com.necroware.terminusplayer.ui.components.TerminalBorder
import com.necroware.terminusplayer.ui.components.terminalInteraction
import com.necroware.terminusplayer.ui.components.safeTerminalInteraction
import com.necroware.terminusplayer.util.safeItemClick
import com.necroware.terminusplayer.util.toMinutesSeconds
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.os.Build

import com.necroware.terminusplayer.ui.components.PixelatedAlbumArt
import com.necroware.terminusplayer.util.safeGridItemClick
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import com.necroware.terminusplayer.data.model.Album
import com.necroware.terminusplayer.ui.components.TerminalTypewriterHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()

    var songToOption by remember { mutableStateOf<Song?>(null) }
    var pendingDeleteSong by remember { mutableStateOf<Song?>(null) }
    
    val songsListState = rememberLazyListState()
    val albumsGridState = rememberLazyGridState()
    val artistsListState = rememberLazyListState()
    val foldersListState = rememberLazyListState()
    
    val coroutineScope = rememberCoroutineScope()
    val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle()

    // Auto-scroll to currently playing track on open or track change
    LaunchedEffect(nowPlaying.mediaId, selectedTab) {
        if (selectedTab == LibraryTab.SONGS && !nowPlaying.mediaId.isNullOrEmpty()) {
            val playingIndex = songs.indexOfFirst {
                it.id.toString() == nowPlaying.mediaId || it.uriString == nowPlaying.mediaId
            }
            if (playingIndex >= 0) {
                songsListState.animateScrollToItem(playingIndex)
            }
        }
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val pending = pendingDeleteSong
        pendingDeleteSong = null
        if (pending != null && result.resultCode == Activity.RESULT_OK) {
            viewModel.finalizeDeletedSong(pending)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TerminalTypewriterHeader(
                    text = "> LIBRARY",
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize
                )
                Text(
                    text = "[SEARCH]",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onSearchClick() }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val palette = com.necroware.terminusplayer.ui.theme.LocalTerminalPalette.current
                LibraryTab.entries.forEach { tab ->
                    val tabColor = when (tab) {
                        LibraryTab.SONGS -> palette.primaryAccent
                        LibraryTab.ALBUMS -> palette.secondaryAccent
                        LibraryTab.ARTISTS -> palette.tertiaryAccent
                        LibraryTab.FOLDERS -> palette.highlightAccent
                    }
                    Text(
                        text = "[${tab.name}]",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Normal,
                        color = if (tab == selectedTab) tabColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { viewModel.setSelectedTab(tab) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    LibraryTab.SONGS -> {
                        LazyColumn(
                            state = songsListState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(songs, key = { it.id }) { song ->
                                val isCurrent = song.id.toString() == nowPlaying.mediaId || song.uriString == nowPlaying.mediaId
                                SongRow(
                                    song = song,
                                    isCurrent = isCurrent,
                                    isPlaying = nowPlaying.isPlaying,
                                    onClick = { onSongClick(song, songs) },
                                    onLongClick = { songToOption = song },
                                    listState = songsListState
                                )
                            }
                        }
                    }
                    LibraryTab.ALBUMS -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "// ALBUM_DIRECTORY [${albums.size}]",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = TerminalFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "VIEW: GRID_SQ",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                state = albumsGridState,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(end = 12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(albums, key = { it.title }) { album ->
                                    SquareAlbumCard(
                                        album = album,
                                        modifier = Modifier.safeGridItemClick(albumsGridState) {
                                            onAlbumClick(album.title)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    LibraryTab.ARTISTS -> {
                        LazyColumn(
                            state = artistsListState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(artists, key = { it.name }) { artist ->
                                ArtistRow(
                                    artist = artist,
                                    listState = artistsListState,
                                    onClick = { onArtistClick(artist.name) }
                                )
                            }
                        }
                    }
                    LibraryTab.FOLDERS -> {
                        val currentFolderPath by viewModel.currentFolderPath.collectAsStateWithLifecycle()
                        val folderNodes by viewModel.folderNodes.collectAsStateWithLifecycle()
                        val currentFolderSongs by viewModel.currentFolderSongs.collectAsStateWithLifecycle()

                        Column(modifier = Modifier.fillMaxSize()) {
                            if (currentFolderPath != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clickable { viewModel.navigateUpFolder() },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "[..] UP ONE LEVEL // ${currentFolderPath?.substringAfterLast("/")}",
                                        color = com.necroware.terminusplayer.ui.theme.LocalTerminalPalette.current.secondaryAccent,
                                        fontFamily = TerminalFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            LazyColumn(
                                state = foldersListState,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize().padding(end = 12.dp)
                            ) {
                                items(folderNodes, key = { it.fullPath }) { folder ->
                                    TerminalBorder(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.openFolder(folder.fullPath) }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "[📁 ${folder.name}]",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = com.necroware.terminusplayer.ui.theme.LocalTerminalPalette.current.primaryAccent,
                                                fontFamily = TerminalFontFamily
                                            )
                                            Text(
                                                text = "${folder.songCount} TRKS",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = com.necroware.terminusplayer.ui.theme.LocalTerminalPalette.current.tertiaryAccent,
                                                fontFamily = TerminalFontFamily
                                            )
                                        }
                                    }
                                }

                                items(currentFolderSongs, key = { it.id }) { song ->
                                    SongRow(
                                        song = song,
                                        isCurrent = song.id.toString() == nowPlaying.mediaId || song.uriString == nowPlaying.mediaId,
                                        isPlaying = nowPlaying.isPlaying,
                                        onClick = { viewModel.playSong(song, currentFolderSongs) },
                                        onLongClick = { songToOption = song },
                                        listState = foldersListState
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Edge-tucked scrollbars (outside column padding)
        val scrollerModifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(vertical = 140.dp)
            .padding(end = 1.dp)

        when (selectedTab) {
            LibraryTab.SONGS -> FastScrollHandle(
                modifier = scrollerModifier,
                listState = songsListState,
                itemCount = songs.size,
                onScroll = { index -> coroutineScope.launch { songsListState.scrollToItem(index) } }
            )
            LibraryTab.ALBUMS -> FastScrollHandleGrid(
                modifier = scrollerModifier,
                gridState = albumsGridState,
                itemCount = albums.size,
                onScroll = { index -> coroutineScope.launch { albumsGridState.scrollToItem(index) } }
            )
            LibraryTab.ARTISTS -> FastScrollHandle(
                modifier = scrollerModifier,
                listState = artistsListState,
                itemCount = artists.size,
                onScroll = { index -> coroutineScope.launch { artistsListState.scrollToItem(index) } }
            )
            LibraryTab.FOLDERS -> FastScrollHandle(
                modifier = scrollerModifier,
                listState = foldersListState,
                itemCount = folders.size,
                onScroll = { index -> coroutineScope.launch { foldersListState.scrollToItem(index) } }
            )
        }

        songToOption?.let { song ->
            SongActionSheet(
                song = song,
                onDismiss = { songToOption = null },
                onPlayNext = { viewModel.playNext(song) },
                onAddToQueue = { viewModel.addToQueue(song) },
                onAddToPlaylist = { /* TODO */ },
                onDelete = {
                    songToOption = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intentSender = viewModel.createDeleteRequest(song)
                        if (intentSender != null) {
                            pendingDeleteSong = song
                            deleteLauncher.launch(
                                IntentSenderRequest.Builder(intentSender).build()
                            )
                        }
                    } else {
                        viewModel.deleteSongDirect(song)
                    }
                }
            )
        }
    }
}

@Composable
private fun SquareAlbumCard(
    album: Album,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var bitmap by remember(album.representativeUriString) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(album.representativeUriString) {
        if (album.representativeUriString.isNotBlank()) {
            val uri = android.net.Uri.parse(album.representativeUriString)
            if (uri.scheme == "content") {
                bitmap = try {
                    context.contentResolver.loadThumbnail(uri, android.util.Size(200, 200), null)
                } catch (e: Exception) { null }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0E0C))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            .padding(6.dp)
    ) {
        SongArt(
            uriString = album.representativeUriString,
            size = 180.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = album.title,
                color = Color.White,
                fontFamily = TerminalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = album.artist,
                color = Color.Gray,
                fontFamily = TerminalFontFamily,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${album.songCount} TRKS",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = TerminalFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "2026",
                    color = Color.DarkGray,
                    fontFamily = TerminalFontFamily,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun ArtistRow(
    artist: com.necroware.terminusplayer.data.model.Artist,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onClick: () -> Unit
) {
    TerminalBorder(
        modifier = Modifier
            .fillMaxWidth()
            .safeTerminalInteraction(listState = listState, onClick = onClick)
            .padding(end = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = TerminalFontFamily
                )
                Text(
                    text = "ID: ${artist.name.hashCode().toString(16).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontFamily = TerminalFontFamily
                )
            }

            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${artist.songCount} SONGS",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = TerminalFontFamily
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongRow(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val accentColor = com.necroware.terminusplayer.ui.theme.LocalTerminusAccent.current
    TerminalBorder(
        borderColor = if (isCurrent) accentColor else MaterialTheme.colorScheme.outline,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isCurrent) accentColor.copy(alpha = 0.12f) else Color.Transparent)
            .safeTerminalInteraction(
                listState = listState,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongArt(uriString = song.uriString, size = 42.dp)
            
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = song.title,
                    color = if (isCurrent) accentColor else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = TerminalFontFamily
                )
                Text(
                    text = song.artist.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = TerminalFontFamily
                )
            }

            Box(
                modifier = Modifier
                    .width(36.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrent) {
                    com.necroware.terminusplayer.ui.components.AsciiVisualizer(
                        isPlaying = isPlaying,
                        barCount = 4,
                        accentColor = accentColor
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = song.duration.toMinutesSeconds(),
                    color = if (isCurrent) accentColor else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = TerminalFontFamily
                )
                if (song.isLiked) {
                    Text(
                        text = "[L]",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = TerminalFontFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun FastScrollHandleGrid(
    modifier: Modifier = Modifier,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    itemCount: Int,
    onScroll: (Int) -> Unit
) {
    if (itemCount < 10) return

    var isDragging by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableFloatStateOf(1f) }

    val scrollFraction by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) 0f else {
                val firstVisible = gridState.firstVisibleItemIndex
                (firstVisible.toFloat() / totalItems).coerceIn(0f, 1f)
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .width(18.dp)
            .fillMaxHeight()
            .onSizeChanged { trackHeightPx = it.height.toFloat().coerceAtLeast(1f) }
            .pointerInput(itemCount) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, _ ->
                        change.consume()
                        val fraction = (change.position.y / trackHeightPx).coerceIn(0f, 1f)
                        onScroll((fraction * itemCount).toInt().coerceIn(0, itemCount - 1))
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val segmentCount = 20
            val segmentHeight = h / segmentCount
            val thumbIndex = (scrollFraction * (segmentCount - 1)).toInt()

            repeat(segmentCount) { index ->
                val top = index * segmentHeight
                val isThumb = index == thumbIndex
                
                val blockW = w * 0.3f
                val blockH = segmentHeight * 0.7f
                val left = (w - blockW) / 2
                val blockTop = top + (segmentHeight - blockH) / 2

                if (isThumb) {
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(left, blockTop),
                        size = Size(blockW, blockH)
                    )
                } else {
                    drawRect(
                        color = mutedColor,
                        topLeft = Offset(left, blockTop),
                        size = Size(blockW, blockH),
                        style = Stroke(width = 0.5.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun FastScrollHandle(
    modifier: Modifier = Modifier,
    listState: androidx.compose.foundation.lazy.LazyListState,
    itemCount: Int,
    onScroll: (Int) -> Unit
) {
    if (itemCount < 10) return

    var isDragging by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableFloatStateOf(1f) }

    val scrollFraction by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) 0f else {
                val firstVisible = listState.firstVisibleItemIndex
                (firstVisible.toFloat() / totalItems).coerceIn(0f, 1f)
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .width(18.dp)
            .fillMaxHeight()
            .onSizeChanged { trackHeightPx = it.height.toFloat().coerceAtLeast(1f) }
            .pointerInput(itemCount) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, _ ->
                        change.consume()
                        val fraction = (change.position.y / trackHeightPx).coerceIn(0f, 1f)
                        onScroll((fraction * itemCount).toInt().coerceIn(0, itemCount - 1))
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val segmentCount = 20
            val segmentHeight = h / segmentCount
            val thumbIndex = (scrollFraction * (segmentCount - 1)).toInt()

            repeat(segmentCount) { index ->
                val top = index * segmentHeight
                val isThumb = index == thumbIndex
                
                val blockW = w * 0.3f
                val blockH = segmentHeight * 0.7f
                val left = (w - blockW) / 2
                val blockTop = top + (segmentHeight - blockH) / 2

                if (isThumb) {
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(left, blockTop),
                        size = Size(blockW, blockH)
                    )
                } else {
                    drawRect(
                        color = mutedColor,
                        topLeft = Offset(left, blockTop),
                        size = Size(blockW, blockH),
                        style = Stroke(width = 0.5.dp.toPx())
                    )
                }
            }
        }
    }
}
