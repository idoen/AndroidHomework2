package com.example.obstacles.highscores

interface HighScoreRepository {
    fun topScores(limit: Int): List<HighScoreEntry>
    fun addScore(entry: HighScoreEntry)
}
