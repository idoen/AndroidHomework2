package com.example.obstacles.game

data class GameConfig(
    val laneCount: Int = 5,
    val obstacleRows: Int = 25,
    val startingLives: Int = 3,
    val moveIntervalSeconds: Float = 0.1f,
    val minSpawnInterval: Float = 0.7f,
    val maxSpawnInterval: Float = 1.1f
) {
    val carRow: Int = obstacleRows
}
