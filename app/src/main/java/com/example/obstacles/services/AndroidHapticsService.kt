package com.example.obstacles.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

class AndroidHapticsService(
    private val context: Context,
    private val fallbackView: View?
) : HapticsService {

    override fun performCrashHaptic() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        val didVibrate = vibrator?.let { service ->
            if (!service.hasVibrator()) return@let false
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    service.vibrate(
                        VibrationEffect.createOneShot(
                            200,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    service.vibrate(200)
                }
                true
            } catch (_: Exception) {
                false
            }
        } ?: false

        if (!didVibrate) {
            fallbackHaptic()
        }
    }

    private fun fallbackHaptic() {
        fallbackView?.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
}
