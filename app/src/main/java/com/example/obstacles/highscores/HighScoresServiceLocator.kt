package com.example.obstacles.highscores

object HighScoresServiceLocator {
    @Volatile
    private var instance: HighScoreRepository? = null

    fun repository(context: android.content.Context): HighScoreRepository {
        return instance ?: synchronized(this) {
            instance ?: SharedPreferencesHighScoreRepository(context.applicationContext).also {
                instance = it
            }
        }
    }
}
