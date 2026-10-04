package com.necroware.terminusplayer.lyrics

object LrcParser {
    private val LRC_REGEX = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
    private val WORD_TAG_REGEX = Regex("""<(\d{2}):(\d{2})\.(\d{2,3})>([^<]*)""")

    fun parse(lrcContent: String): List<LyricLine> {
        val rawLines = mutableListOf<LyricLine>()
        
        lrcContent.lines().forEach { line ->
            val match = LRC_REGEX.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLong()
                val sec = match.groupValues[2].toLong()
                val msStr = match.groupValues[3]
                val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                val totalMs = (min * 60 * 1000) + (sec * 1000) + ms
                
                val rawText = match.groupValues[4].trim()
                if (rawText.isNotEmpty()) {
                    val wordMatches = WORD_TAG_REGEX.findAll(rawText).toList()
                    val parsedWords = mutableListOf<LyricWord>()
                    
                    if (wordMatches.isNotEmpty()) {
                        wordMatches.forEach { wMatch ->
                            val wMin = wMatch.groupValues[1].toLong()
                            val wSec = wMatch.groupValues[2].toLong()
                            val wMsStr = wMatch.groupValues[3]
                            val wMs = if (wMsStr.length == 2) wMsStr.toLong() * 10 else wMsStr.toLong()
                            val wTotalMs = (wMin * 60 * 1000) + (wSec * 1000) + wMs
                            val wordStr = wMatch.groupValues[4].trim()
                            if (wordStr.isNotEmpty()) {
                                parsedWords.add(LyricWord(wordStr, wTotalMs))
                            }
                        }
                        val cleanText = parsedWords.joinToString(" ") { it.text }
                        rawLines.add(LyricLine(totalMs, cleanText, parsedWords))
                    } else {
                        // Strip remaining XML/HTML tags if any
                        val cleanText = rawText.replace(Regex("""<[^>]*>"""), "").trim()
                        if (cleanText.isNotEmpty()) {
                            rawLines.add(LyricLine(totalMs, cleanText, emptyList()))
                        }
                    }
                }
            }
        }

        val sorted = rawLines.sortedBy { it.timestampMs }

        // Populate words for lines without explicit word tags by interpolating timing across line duration
        return sorted.mapIndexed { index, line ->
            if (line.words.isNotEmpty()) {
                line
            } else {
                val nextTimestamp = if (index < sorted.size - 1) sorted[index + 1].timestampMs else line.timestampMs + 4000L
                val lineDuration = (nextTimestamp - line.timestampMs).coerceAtLeast(1000L)
                val splitWords = line.text.split(Regex("""\s+""")).filter { it.isNotBlank() }
                
                if (splitWords.isEmpty()) {
                    line
                } else {
                    val wordStep = lineDuration / splitWords.size
                    val generatedWords = splitWords.mapIndexed { wIdx, word ->
                        LyricWord(word, line.timestampMs + (wIdx * wordStep))
                    }
                    line.copy(words = generatedWords)
                }
            }
        }
    }
}
