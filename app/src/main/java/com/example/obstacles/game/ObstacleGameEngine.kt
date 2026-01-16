package com.example.obstacles.game

import kotlin.random.Random

class ObstacleGameEngine(
    val config: GameConfig,
    private val random: Random = Random.Default
) {
    var listener: GameEngineListener? = null

    private var lives: Int = config.startingLives
    private var carLane: Int = config.laneCount / 2
    private val obstacles = mutableListOf<Obstacle>()
    private var distance = 0
    private var coins = 0

    var speedMultiplier: Float = 1f

    private var spawnTimer = 0f
    private var moveTimer = 0f
    private var nextSpawnIn = randomSpawnInterval()

    val state: GameState
        get() = GameState(lives, carLane, obstacles.toList(), distance, coins)

    fun reset() {
        lives = config.startingLives
        carLane = config.laneCount / 2
        obstacles.clear()
        distance = 0
        coins = 0
        spawnTimer = 0f
        moveTimer = 0f
        nextSpawnIn = randomSpawnInterval()
        listener?.onStateUpdated(state)
    }

    fun moveCar(direction: Int) {
        val next = carLane + direction
        carLane = when {
            next < 0 -> config.laneCount - 1
            next >= config.laneCount -> 0
            else -> next
        }
        listener?.onStateUpdated(state)
    }

    fun update(dt: Float) {
        if (dt <= 0f) return

        var stateChanged = false
        spawnTimer += dt
        moveTimer += dt

        if (spawnTimer >= nextSpawnIn) {
            stateChanged = spawnObstacle() || stateChanged
            spawnTimer = 0f
            nextSpawnIn = randomSpawnInterval()
        }

        val moveInterval = effectiveMoveInterval()
        while (moveTimer >= moveInterval) {
            stateChanged = stepMovement() || stateChanged
            moveTimer -= moveInterval
        }

        if (stateChanged) {
            listener?.onStateUpdated(state)
        }
    }

    private fun spawnObstacle(): Boolean {
        val availableLanes = (0 until config.laneCount).filter { lane ->
            obstacles.none { it.lane == lane && it.row == 0 }
        }
        if (availableLanes.isEmpty()) return false

        val lane = availableLanes[random.nextInt(availableLanes.size)]
        val kind = if (random.nextFloat() < COIN_SPAWN_CHANCE) {
            ObstacleKind.COIN
        } else {
            ObstacleKind.entries.filterNot { it == ObstacleKind.COIN }[
                random.nextInt(ObstacleKind.entries.size - 1)
            ]
        }
        obstacles.add(Obstacle(lane = lane, row = 0, kind = kind))
        return true
    }

    private fun stepMovement(): Boolean {
        var stateChanged = false
        distance += 1
        stateChanged = true
        if (obstacles.isEmpty()) return stateChanged
        var crashed = false
        var collisionState: GameState? = null

        val iterator = obstacles.listIterator()
        while (iterator.hasNext()) {
            val obstacle = iterator.next()
            val nextRow = obstacle.row + 1

            if (nextRow >= config.carRow) {
                if (obstacle.lane == carLane && !crashed) {
                    if (obstacle.kind == ObstacleKind.COIN) {
                        coins += 1
                        stateChanged = true
                        iterator.remove()
                        continue
                    } else {
                        crashed = true
                        lives = (lives - 1).coerceAtLeast(0)
                        collisionState = state
                    }
                }
                iterator.remove()
                continue
            }

            iterator.set(obstacle.copy(row = nextRow))
        }

        if (crashed && collisionState != null) {
            listener?.onCrash(collisionState)
            if (lives <= 0) {
                listener?.onGameOver(state)
            }
        }

        return true
    }

    private fun randomSpawnInterval(): Float {
        val min = config.minSpawnInterval
        val max = config.maxSpawnInterval
        return min + random.nextFloat() * (max - min)
    }

    private fun effectiveMoveInterval(): Float {
        val multiplier = speedMultiplier.coerceIn(MIN_SPEED_MULTIPLIER, MAX_SPEED_MULTIPLIER)
        return config.moveIntervalSeconds / multiplier
    }

    companion object {
        private const val COIN_SPAWN_CHANCE = 0.18f
        private const val MIN_SPEED_MULTIPLIER = 0.6f
        private const val MAX_SPEED_MULTIPLIER = 1.8f
    }
}
