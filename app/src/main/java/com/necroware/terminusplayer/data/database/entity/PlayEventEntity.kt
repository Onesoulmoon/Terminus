package com.necroware.terminusplayer.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per listen session. Kept granular (rather than pre-aggregated)
 * so every chart type (bar/line/pie/histogram/scatter/box/area) can be
 * derived from the same table without separate rollup tables.
 */
@Entity(tableName = "play_events")
data class PlayEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val artist: String,
    val album: String,
    val albumId: Long = -1L,
    val startedAtEpochMs: Long,
    val msPlayed: Long,
    val completed: Boolean
)
