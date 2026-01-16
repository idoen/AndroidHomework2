package com.example.obstacles.highscores

data class HighScoreEntry(
    val coins: Int,
    val distance: Int,
    val location: LocationPoint
)

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val label: String
)
