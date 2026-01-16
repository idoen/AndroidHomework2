package com.example.obstacles.highscores

import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.example.obstacles.R

class HighScoresActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)

        val tableButton = findViewById<Button>(R.id.btnHighScoresTable)
        val mapButton = findViewById<Button>(R.id.btnHighScoresMap)

        tableButton.setOnClickListener {
            showTable()
        }

        mapButton.setOnClickListener {
            showMap()
        }

        if (savedInstanceState == null) {
            showTable()
        }
    }

    private fun showTable() {
        supportFragmentManager.commit {
            replace(R.id.highScoresContainer, HighScoresTableFragment())
        }
    }

    private fun showMap() {
        supportFragmentManager.commit {
            replace(R.id.highScoresContainer, HighScoresMapFragment())
        }
    }
}
