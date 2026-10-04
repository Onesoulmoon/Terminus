package com.necroware.terminusplayer.widget

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.necroware.terminusplayer.playback.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Asynchronous command receiver for the Terminus Widget.
 * Communicates with the MediaSession without requiring a foreground service start.
 */
class TerminusWidgetReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                val sessionToken = SessionToken(
                    context,
                    ComponentName(context, MusicService::class.java)
                )
                
                val future = MediaController.Builder(context, sessionToken).buildAsync()
                future.addListener({
                    try {
                        val controller = future.get()
                        
                        when (action) {
                            ACTION_TOGGLE_PLAY -> {
                                if (controller.isPlaying) {
                                    controller.pause()
                                } else {
                                    if (controller.playbackState == Player.STATE_ENDED) {
                                        controller.seekTo(0, 0)
                                    }
                                    controller.play()
                                }
                            }
                            ACTION_NEXT -> controller.seekToNextMediaItem()
                            ACTION_PREVIOUS -> controller.seekToPreviousMediaItem()
                        }
                        
                        controller.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }, MoreExecutors.directExecutor())

            } catch (e: Exception) {
                e.printStackTrace()
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_PLAY = "com.necroware.terminusplayer.ACTION_TOGGLE_PLAY"
        const val ACTION_NEXT = "com.necroware.terminusplayer.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.necroware.terminusplayer.ACTION_PREVIOUS"
    }
}
