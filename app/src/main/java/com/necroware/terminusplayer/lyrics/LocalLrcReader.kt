package com.necroware.terminusplayer.lyrics

import java.io.File

object LocalLrcReader {

    fun findLocalLrc(audioPath: String?): String? {
        if (audioPath.isNullOrEmpty()) return null
        
        val audioFile = File(audioPath)
        // Note: For scoped storage, this might return false if not using DocumentFile.
        // We'll rely on it as a best-effort helper.
        if (!audioFile.exists()) return null

        val dotIndex = audioFile.absolutePath.lastIndexOf('.')
        val lrcPath = if (dotIndex != -1) {
            audioFile.absolutePath.substring(0, dotIndex) + ".lrc"
        } else {
            audioFile.absolutePath + ".lrc"
        }
        val lrcFile = File(lrcPath)

        return if (lrcFile.exists()) {
            lrcFile.readText(Charsets.UTF_8)
        } else null
    }
}
