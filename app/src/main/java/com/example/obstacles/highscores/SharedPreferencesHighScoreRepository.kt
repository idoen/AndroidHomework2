package com.example.obstacles.highscores

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SharedPreferencesHighScoreRepository(
    context: Context
) : HighScoreRepository {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun topScores(limit: Int): List<HighScoreEntry> {
        return loadEntries()
            .sortedWith(
                compareByDescending<HighScoreEntry> { it.coins }
                    .thenByDescending { it.distance }
            )
            .take(limit)
    }

    override fun addScore(entry: HighScoreEntry) {
        val updated = loadEntries().toMutableList().apply { add(entry) }
        val sorted = updated.sortedWith(
            compareByDescending<HighScoreEntry> { it.coins }
                .thenByDescending { it.distance }
        ).take(MAX_ENTRIES)
        saveEntries(sorted)
    }

    private fun loadEntries(): List<HighScoreEntry> {
        val raw = preferences.getString(KEY_ENTRIES, null) ?: return emptyList()
        val jsonArray = JSONArray(raw)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                val location = LocationPoint(
                    latitude = item.getDouble(KEY_LATITUDE),
                    longitude = item.getDouble(KEY_LONGITUDE),
                    label = item.getString(KEY_LABEL)
                )
                val legacyScore = item.optInt(KEY_LEGACY_SCORE, 0)
                add(
                    HighScoreEntry(
                        coins = item.optInt(KEY_COINS, 0),
                        distance = item.optInt(KEY_DISTANCE, legacyScore),
                        location = location
                    )
                )
            }
        }
    }

    private fun saveEntries(entries: List<HighScoreEntry>) {
        val jsonArray = JSONArray()
        entries.forEach { entry ->
            val item = JSONObject().apply {
                put(KEY_COINS, entry.coins)
                put(KEY_DISTANCE, entry.distance)
                put(KEY_LATITUDE, entry.location.latitude)
                put(KEY_LONGITUDE, entry.location.longitude)
                put(KEY_LABEL, entry.location.label)
            }
            jsonArray.put(item)
        }
        preferences.edit().putString(KEY_ENTRIES, jsonArray.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "high_scores"
        private const val KEY_ENTRIES = "entries"
        private const val KEY_COINS = "coins"
        private const val KEY_DISTANCE = "distance"
        private const val KEY_LEGACY_SCORE = "score"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_LABEL = "label"
        private const val MAX_ENTRIES = 50
    }
}
