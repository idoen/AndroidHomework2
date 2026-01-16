package com.example.obstacles.game

data class GameState(
    val lives: Int,
    val carLane: Int,
    val obstacles: List<Obstacle>,
    val distance: Int,
    val coins: Int
)


data class Obstacle(
    val lane: Int,
    val row: Int,
    val kind: ObstacleKind
)


enum class ObstacleKind {
    CONE,
    BARRIER,
    TIRE,
    COIN
}
