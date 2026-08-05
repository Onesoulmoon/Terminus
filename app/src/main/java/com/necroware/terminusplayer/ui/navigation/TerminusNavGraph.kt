package com.necroware.terminusplayer.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.ui.components.MiniPlayerBar
import com.necroware.terminusplayer.ui.components.TerminalNavIcon
import com.necroware.terminusplayer.ui.screens.albumdetail.AlbumDetailScreen
import com.necroware.terminusplayer.ui.screens.artistdetail.ArtistDetailScreen
import com.necroware.terminusplayer.ui.screens.home.HomeScreen
import com.necroware.terminusplayer.ui.screens.library.LibraryScreen
import com.necroware.terminusplayer.ui.screens.nowplaying.NowPlayingScreen
import com.necroware.terminusplayer.ui.screens.nowplaying.PlaybackViewModel
import com.necroware.terminusplayer.ui.screens.playlists.PlaylistDetailScreen
import com.necroware.terminusplayer.ui.screens.playlists.PlaylistsScreen
import com.necroware.terminusplayer.ui.screens.search.SearchScreen
import com.necroware.terminusplayer.ui.screens.settings.SettingsScreen
import com.necroware.terminusplayer.ui.screens.stats.StatsScreen
import com.necroware.terminusplayer.util.toMediaItems

/** Index within the bottom-nav tab order, used to pick left/right slide direction. -1 if not a tab route. */
private fun tabIndex(route: String?): Int = bottomNavItems.indexOfFirst { it.route == route }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabSlideEnter() =
    run {
        val fromIndex = tabIndex(initialState.destination.route)
        val toIndex = tabIndex(targetState.destination.route)
        val movingForward = toIndex >= fromIndex
        slideInHorizontally(
            initialOffsetX = { fullWidth -> if (movingForward) fullWidth else -fullWidth },
            animationSpec = tween(220)
        ) + fadeIn(animationSpec = tween(220))
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabSlideExit() =
    run {
        val fromIndex = tabIndex(initialState.destination.route)
        val toIndex = tabIndex(targetState.destination.route)
        val movingForward = toIndex >= fromIndex
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> if (movingForward) -fullWidth else fullWidth },
            animationSpec = tween(220)
        ) + fadeOut(animationSpec = tween(220))
    }

@Composable
fun TerminusNavGraph() {
    val navController = rememberNavController()
    // Single PlaybackViewModel instance (Hilt, Activity-scoped by default when
    // requested at this top-level composable) shared by the mini bar and the
    // full Now Playing screen so both reflect the same MediaController state.
    val playbackViewModel: PlaybackViewModel = hiltViewModel()
    val nowPlaying by playbackViewModel.nowPlaying.collectAsStateWithLifecycle()
    val currentSongUri by playbackViewModel.currentSongUri.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Mini player + bottom nav are hidden on both the full player and the
    // detail screens (they push their own back affordance instead).
    val hideChromeRoutes = setOf(
        Destination.NowPlaying.route,
        Destination.AlbumDetail.route,
        Destination.ArtistDetail.route,
        Destination.PlaylistDetail.route,
        Destination.Search.route
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute !in hideChromeRoutes) {
                Column {
                    MiniPlayerBar(
                        nowPlaying = nowPlaying,
                        currentSongUri = currentSongUri,
                        onTogglePlayPause = { playbackViewModel.togglePlayPause() },
                        onSkipNext = { playbackViewModel.skipToNext() },
                        onSkipPrevious = { playbackViewModel.skipToPrevious() },
                        onClick = { navController.navigate(Destination.NowPlaying.route) }
                    )
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        bottomNavItems.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    TerminalNavIcon(
                                        destination = destination,
                                        tint = if (currentRoute == destination.route) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp),
                                        letterSpacing = androidx.compose.ui.unit.TextUnit(0f, androidx.compose.ui.unit.TextUnitType.Sp),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Clip
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { tabSlideEnter() },
            exitTransition = { tabSlideExit() },
            popEnterTransition = { tabSlideEnter() },
            popExitTransition = { tabSlideExit() }
        ) {
            composable(Destination.Home.route) {
                HomeScreen(
                    onSongClick = { song, queue -> playSongAndOpenPlayer(song, queue, playbackViewModel, navController) }
                )
            }
            composable(Destination.Library.route) {
                LibraryScreen(
                    onSongClick = { song, queue -> playSongAndOpenPlayer(song, queue, playbackViewModel, navController) },
                    onAlbumClick = { albumTitle -> navController.navigate(Destination.AlbumDetail.createRoute(albumTitle)) },
                    onArtistClick = { artist -> navController.navigate(Destination.ArtistDetail.createRoute(artist)) },
                    onSearchClick = { navController.navigate(Destination.Search.route) }
                )
            }
            composable(Destination.Playlists.route) {
                PlaylistsScreen(
                    onPlaylistClick = { kind -> navController.navigate(Destination.PlaylistDetail.createRoute(kind.name)) },
                    onCustomPlaylistClick = { id -> navController.navigate(Destination.PlaylistDetail.createRoute("custom:$id")) }
                )
            }
            composable(Destination.Stats.route) { StatsScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
            composable(
                route = Destination.NowPlaying.route,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(280)
                    ) + fadeIn(animationSpec = tween(280))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(180))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(180))
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(280)
                    ) + fadeOut(animationSpec = tween(280))
                }
            ) {
                NowPlayingScreen(
                    viewModel = playbackViewModel,
                    onCollapse = { navController.popBackStack() }
                )
            }
            composable(
                route = Destination.AlbumDetail.route,
                arguments = listOf(navArgument("albumTitle") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(240)) + fadeIn(tween(240))
                },
                exitTransition = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(160)) },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(240)) + fadeOut(tween(240))
                }
            ) {
                AlbumDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Destination.ArtistDetail.route,
                arguments = listOf(navArgument("artist") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(240)) + fadeIn(tween(240))
                },
                exitTransition = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(160)) },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(240)) + fadeOut(tween(240))
                }
            ) {
                ArtistDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Destination.PlaylistDetail.route,
                arguments = listOf(navArgument("kind") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(240)) + fadeIn(tween(240))
                },
                exitTransition = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(160)) },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(240)) + fadeOut(tween(240))
                }
            ) {
                PlaylistDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Destination.Search.route,
                enterTransition = { fadeIn(tween(160)) },
                exitTransition = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(160)) },
                popExitTransition = { fadeOut(tween(160)) }
            ) {
                SearchScreen(
                    onSongClick = { song, queue -> playSongAndOpenPlayer(song, queue, playbackViewModel, navController) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun playSongAndOpenPlayer(
    song: Song,
    queue: List<Song>,
    playbackViewModel: PlaybackViewModel,
    navController: androidx.navigation.NavHostController
) {
    val index = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
    playbackViewModel.playQueue(queue.toMediaItems(), index)
    navController.navigate(Destination.NowPlaying.route)
}
