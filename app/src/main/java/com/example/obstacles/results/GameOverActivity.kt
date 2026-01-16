package com.example.obstacles.results

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.obstacles.MainActivity
import com.example.obstacles.R
import com.example.obstacles.highscores.HighScoresActivity

class GameOverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val distance = intent.getIntExtra(EXTRA_DISTANCE, 0)
        val coins = intent.getIntExtra(EXTRA_COINS, 0)
        val controlMode = intent.getStringExtra(EXTRA_CONTROL_MODE)

        val distanceText = findViewById<TextView>(R.id.gameOverDistance)
        val coinsText = findViewById<TextView>(R.id.gameOverCoins)
        val newGameButton = findViewById<Button>(R.id.btnNewGame)
        val highScoresButton = findViewById<Button>(R.id.btnGameOverHighScores)

        distanceText.text = getString(R.string.game_over_distance, distance)
        coinsText.text = getString(R.string.game_over_coins, coins)

        newGameButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_CONTROL_MODE, controlMode)
            }
            startActivity(intent)
            finish()
        }

        highScoresButton.setOnClickListener {
            startActivity(Intent(this, HighScoresActivity::class.java))
        }
    }

    companion object {
        const val EXTRA_DISTANCE = "com.example.obstacles.RESULT_DISTANCE"
        const val EXTRA_COINS = "com.example.obstacles.RESULT_COINS"
        const val EXTRA_CONTROL_MODE = "com.example.obstacles.RESULT_CONTROL_MODE"
    }
}
