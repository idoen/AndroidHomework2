package com.example.obstacles.highscores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HighScoresViewModel(
    private val repository: HighScoreRepository
) : ViewModel() {

    private val scoresLiveData = MutableLiveData<List<HighScoreEntry>>()
    private val selectedScoreLiveData = MutableLiveData<HighScoreEntry?>()

    val topScores: LiveData<List<HighScoreEntry>> = scoresLiveData
    val selectedScore: LiveData<HighScoreEntry?> = selectedScoreLiveData

    init {
        refresh()
    }

    fun select(entry: HighScoreEntry) {
        selectedScoreLiveData.value = entry
    }

    fun refresh() {
        val scores = repository.topScores(10)
        scoresLiveData.value = scores
        if (scores.isNotEmpty()) {
            val current = selectedScoreLiveData.value
            if (current == null || !scores.contains(current)) {
                selectedScoreLiveData.value = scores.first()
            }
        } else {
            selectedScoreLiveData.value = null
        }
    }
}
