package com.example.obstacles

import android.os.Bundle
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.obstacles.game.GameConfig
import com.example.obstacles.game.GameEngineListener
import com.example.obstacles.game.GameState
import com.example.obstacles.game.ObstacleGameEngine
import com.example.obstacles.game.ObstacleKind
import com.example.obstacles.highscores.HighScoreEntry
import com.example.obstacles.highscores.HighScoreLocationService
import com.example.obstacles.highscores.HighScoresServiceLocator
import com.example.obstacles.input.ControlInput
import com.example.obstacles.input.SensorSteeringController
import com.example.obstacles.location.AndroidLocationProvider
import com.example.obstacles.location.LocationLabelFormatter
import com.example.obstacles.location.LocationPermissionManager
import com.example.obstacles.services.AndroidHapticsService
import com.example.obstacles.services.HapticsService
import kotlin.math.max
import android.media.AudioManager
import android.media.ToneGenerator
import android.content.Intent
import com.example.obstacles.menu.MenuActivity
import com.example.obstacles.results.GameOverActivity

class MainActivity : ComponentActivity(), GameEngineListener {

    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var lanesContainer: LinearLayout
    private lateinit var distanceCounter: TextView
    private lateinit var btnMenu: ImageButton

    private lateinit var laneObstacleCells: List<MutableList<ImageView>>
    private lateinit var carSlots: MutableList<ImageView?>

    private val engine = ObstacleGameEngine(GameConfig())
    private lateinit var hapticsService: HapticsService
    private val crashTone = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    private var controlMode: ControlMode = ControlMode.SENSORS
    private var sensorController: ControlInput? = null
    private lateinit var locationPermissionManager: LocationPermissionManager
    private lateinit var highScoreLocationService: HighScoreLocationService

    private val choreographer: Choreographer = Choreographer.getInstance()
    private var lastFrameNanos = 0L
    private var isRunning = false

    private val obstacleDrawables = mapOf(
        ObstacleKind.CONE to R.drawable.ic_obstacle_cone,
        ObstacleKind.BARRIER to R.drawable.ic_obstacle_barrier,
        ObstacleKind.TIRE to R.drawable.ic_obstacle_tire,
        ObstacleKind.COIN to R.drawable.ic_coin
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        btnMenu = findViewById(R.id.btnMenu)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)
        lanesContainer = findViewById(R.id.lanesContainer)
        distanceCounter = findViewById(R.id.distanceCounter)

        hapticsService = AndroidHapticsService(this, window.decorView)
        controlMode = ControlMode.fromName(intent.getStringExtra(EXTRA_CONTROL_MODE))
        locationPermissionManager = LocationPermissionManager(this) { }
        highScoreLocationService = HighScoreLocationService(
            AndroidLocationProvider(this, locationPermissionManager),
            LocationLabelFormatter(this)
        )
        if (!locationPermissionManager.hasLocationPermissions()) {
            locationPermissionManager.requestPermissions()
        }

        setupLanes()

        btnLeft.setOnTouchListener { view, event -> handleMoveTouch(event, -1, view) }
        btnRight.setOnTouchListener { view, event -> handleMoveTouch(event, 1, view) }
        btnMenu.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }
        configureControls()

        engine.listener = this
        engine.reset()
        startGameLoop()
    }

    private fun setupLanes() {
        lanesContainer.removeAllViews()
        val rows = engine.config.obstacleRows + 1
        val laneCount = engine.config.laneCount

        lanesContainer.weightSum = laneCount.toFloat()
        laneObstacleCells = List(laneCount) { ArrayList() }
        carSlots = MutableList(laneCount) { null }

        repeat(laneCount) { lane ->
            val laneColumn = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                weightSum = rows.toFloat()
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
            }

            repeat(engine.config.obstacleRows) {
                val cell = ImageView(this).apply {
                    alpha = 0f
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                }
                laneObstacleCells[lane].add(cell)
                laneColumn.addView(cell)
            }

            val carSlot = ImageView(this).apply {
                setImageResource(R.drawable.ic_car)
                contentDescription = getString(R.string.content_description_car)
                alpha = if (lane == engine.state.carLane) 1f else 0f
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
            carSlots[lane] = carSlot
            laneColumn.addView(carSlot)

            lanesContainer.addView(laneColumn)
        }
    }

    private fun configureControls() {
        if (controlMode == ControlMode.SENSORS) {
            btnLeft.visibility = View.GONE
            btnRight.visibility = View.GONE
            sensorController = SensorSteeringController(
                this,
                onMove = { direction -> engine.moveCar(direction) },
                onSpeedChange = { multiplier -> engine.speedMultiplier = multiplier }
            )
            engine.speedMultiplier = ControlMode.SENSORS.speedMultiplier
        } else {
            btnLeft.visibility = View.VISIBLE
            btnRight.visibility = View.VISIBLE
            sensorController = null
            engine.speedMultiplier = controlMode.speedMultiplier
        }
    }

    private fun handleMoveTouch(event: MotionEvent, direction: Int, view: View): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                engine.moveCar(direction)
                true
            }

            MotionEvent.ACTION_UP -> {
                view.performClick()
                true
            }

            MotionEvent.ACTION_CANCEL -> true
            else -> false
        }
    }

    override fun onStateUpdated(state: GameState) {
        render(state)
    }

    override fun onCrash(state: GameState) {
        hapticsService.performCrashHaptic()
        crashTone.startTone(ToneGenerator.TONE_PROP_BEEP2, 200)
        Toast.makeText(this, getString(R.string.toast_crash), Toast.LENGTH_SHORT).show()
    }

    override fun onGameOver(state: GameState) {
        saveHighScore(state)
    }

    private fun render(state: GameState) {
        updateHearts(state.lives)
        updateCarSlots(state.carLane)
        updateObstacleCells(state)
        distanceCounter.text = getString(
            R.string.distance_value_format,
            state.distance
        )
    }

    private fun updateHearts(lives: Int) {
        heart1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        heart2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        heart3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }

    private fun updateCarSlots(carLane: Int) {
        for (lane in 0 until engine.config.laneCount) {
            val slot = carSlots[lane]
            slot?.alpha = if (lane == carLane) 1f else 0f
        }
    }

    private fun updateObstacleCells(state: GameState) {
        for (lane in 0 until engine.config.laneCount) {
            laneObstacleCells[lane].forEach { cell ->
                cell.alpha = 0f
                cell.setImageDrawable(null)
            }
        }

        for (obstacle in state.obstacles) {
            if (obstacle.row >= engine.config.obstacleRows) continue
            val cell = laneObstacleCells[obstacle.lane][obstacle.row]
            val drawable = obstacleDrawables[obstacle.kind]
            if (drawable != null) {
                cell.setImageResource(drawable)
                cell.alpha = 1f
            }
        }
    }

    private fun startGameLoop() {
        if (isRunning) return
        isRunning = true
        lastFrameNanos = 0L
        choreographer.postFrameCallback(frameCallback)
    }

    private fun stopGameLoop() {
        isRunning = false
        choreographer.removeFrameCallback(frameCallback)
    }

    private val frameCallback: Choreographer.FrameCallback = Choreographer.FrameCallback { frameTimeNanos ->
        if (!isRunning) return@FrameCallback

        if (lastFrameNanos == 0L) lastFrameNanos = frameTimeNanos
        val dt = (frameTimeNanos - lastFrameNanos) / 1_000_000_000f
        lastFrameNanos = frameTimeNanos

        engine.update(kotlin.math.max(dt, 0f))
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        super.onPause()
        stopGameLoop()
        sensorController?.stop()
    }

    override fun onResume() {
        super.onResume()
        startGameLoop()
        sensorController?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        crashTone.release()
    }

    enum class ControlMode {
        BUTTONS,
        SENSORS;

        val speedMultiplier: Float
            get() = when (this) {
                BUTTONS -> 1f
                SENSORS -> 1f
            }

        companion object {
            fun fromName(name: String?): ControlMode {
                return entries.firstOrNull { it.name == name } ?: SENSORS
            }
        }
    }

    companion object {
        const val EXTRA_CONTROL_MODE = "com.example.obstacles.CONTROL_MODE"
    }

    private fun saveHighScore(state: GameState) {
        val repository = HighScoresServiceLocator.repository(applicationContext)
        highScoreLocationService.fetchLocation { location ->
            repository.addScore(
                HighScoreEntry(
                    coins = state.coins,
                    distance = state.distance,
                    location = location
                )
            )
            navigateToGameOver(state)
        }
    }

    private fun navigateToGameOver(state: GameState) {
        stopGameLoop()
        val intent = Intent(this, GameOverActivity::class.java).apply {
            putExtra(GameOverActivity.EXTRA_DISTANCE, state.distance)
            putExtra(GameOverActivity.EXTRA_COINS, state.coins)
            putExtra(GameOverActivity.EXTRA_CONTROL_MODE, controlMode.name)
        }
        startActivity(intent)
        finish()
    }
}
