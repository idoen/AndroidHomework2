package com.example.obstacles.input

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class SensorSteeringController(
    context: Context,
    private val onMove: (Int) -> Unit,
    private val onSpeedChange: (Float) -> Unit = {}
) : ControlInput, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastMoveTimeMs = 0L
    private var lastSpeedMultiplier = 1f

    var tiltThreshold = 1.4f
    var debounceMs = 240L
    var speedTiltThreshold = 1.1f
    var fastTiltMultiplier = 1.4f
    var slowTiltMultiplier = 0.8f

    override fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val newMultiplier = when {
            y <= -speedTiltThreshold -> fastTiltMultiplier
            y >= speedTiltThreshold -> slowTiltMultiplier
            else -> 1f
        }
        if (newMultiplier != lastSpeedMultiplier) {
            lastSpeedMultiplier = newMultiplier
            onSpeedChange(newMultiplier)
        }

        val now = System.currentTimeMillis()
        if (now - lastMoveTimeMs < debounceMs) return

        if (abs(x) >= tiltThreshold) {
            val direction = if (x > 0) -1 else 1
            onMove(direction)
            lastMoveTimeMs = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
