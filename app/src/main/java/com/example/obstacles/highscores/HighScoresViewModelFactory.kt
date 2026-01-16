package com.example.obstacles.highscores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HighScoresViewModelFactory(
    private val repository: HighScoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HighScoresViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HighScoresViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
