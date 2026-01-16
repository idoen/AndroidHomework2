package com.example.obstacles.game

interface GameEngineListener {
    fun onStateUpdated(state: GameState)
    fun onCrash(state: GameState)
    fun onGameOver(state: GameState)
}
