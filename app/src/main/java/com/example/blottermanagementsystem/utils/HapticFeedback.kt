package com.example.blottermanagementsystem.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Haptic Feedback Utility
 * Provides vibration feedback for user interactions
 */
class HapticFeedback(private val context: Context) {
    
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Light tap - for button clicks
     */
    fun lightTap() {
        vibrate(50)
    }
    
    /**
     * Medium tap - for important actions
     */
    fun mediumTap() {
        vibrate(100)
    }
    
    /**
     * Heavy tap - for warnings or errors
     */
    fun heavyTap() {
        vibrate(200)
    }
    
    /**
     * Success pattern - double tap
     */
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 50, 50, 50)
            val amplitudes = intArrayOf(0, 100, 0, 100)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
        }
    }
    
    /**
     * Error pattern - long vibration
     */
    fun error() {
        vibrate(300)
    }
    
    /**
     * Warning pattern - three short taps
     */
    fun warning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 50, 100, 50, 100, 50)
            val amplitudes = intArrayOf(0, 150, 0, 150, 0, 150)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 100, 50, 100, 50), -1)
        }
    }
    
    private fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}

/**
 * Composable function to get HapticFeedback instance
 */
@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val context = LocalContext.current
    return remember { HapticFeedback(context) }
}
