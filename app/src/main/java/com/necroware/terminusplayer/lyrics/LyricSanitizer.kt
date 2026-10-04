package com.necroware.terminusplayer.lyrics

object LyricSanitizer {

    fun cleanTitle(title: String): String {
        return title
            .replace(Regex("""(?i)[\(\[\{].*?(feat|ft|remaster|deluxe|version|edition|bonus|explicit|live|mono|stereo).*?[\)\]\}]"""), "")
            .replace(Regex("""(?i)\b(feat|ft)\..*"""), "")
            .trim()
    }

    fun cleanArtist(artist: String): String {
        return artist
            .split(",", ";", " feat.", " ft.", " x ", " & ")[0]
            .trim()
    }
}
