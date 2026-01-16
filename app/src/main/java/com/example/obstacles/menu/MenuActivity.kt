package com.example.obstacles.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.obstacles.MainActivity
import com.example.obstacles.R
import com.example.obstacles.highscores.HighScoresActivity

class MenuActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val buttonMode = findViewById<Button>(R.id.btnModeButtons)
        val sensorMode = findViewById<Button>(R.id.btnModeSensors)
        val highScores = findViewById<Button>(R.id.btnHighScores)

        buttonMode.setOnClickListener {
            startGame(MainActivity.ControlMode.BUTTONS)
        }

        sensorMode.setOnClickListener {
            startGame(MainActivity.ControlMode.SENSORS)
        }

        highScores.setOnClickListener {
            startActivity(Intent(this, HighScoresActivity::class.java))
        }
    }

    private fun startGame(mode: MainActivity.ControlMode) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_CONTROL_MODE, mode.name)
        }
        startActivity(intent)
    }
}
