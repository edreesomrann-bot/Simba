package com.example.util

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.Choreographer
import android.view.Display
import android.view.Window
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Utility for configuring high refresh rate (90 FPS / 90Hz) on Android devices,
 * with real-time Choreographer frame tracking and display mode management.
 */
object FrameRateHelper {

    private const val TAG = "FrameRateHelper"

    enum class TargetFps(val fps: Float, val labelAr: String, val labelEn: String) {
        FPS_90(90f, "90 إطار/ثانية (فائق السلاسة)", "90 FPS (Ultra Smooth)"),
        FPS_120(120f, "120 إطار/ثانية (أعلى تردد)", "120 FPS (Max Hz)"),
        FPS_60(60f, "60 إطار/ثانية (قياسي)", "60 FPS (Standard)"),
        SYSTEM_DEFAULT(0f, "تلقائي حسب النظام", "Adaptive")
    }

    private val _currentFps = MutableStateFlow(90)
    val currentFps: StateFlow<Int> = _currentFps.asStateFlow()

    private val _activeTargetFps = MutableStateFlow(TargetFps.FPS_90)
    val activeTargetFps: StateFlow<TargetFps> = _activeTargetFps.asStateFlow()

    private val _activeDisplayHz = MutableStateFlow(90f)
    val activeDisplayHz: StateFlow<Float> = _activeDisplayHz.asStateFlow()

    private val _is90FpsActive = MutableStateFlow(true)
    val is90FpsActive: StateFlow<Boolean> = _is90FpsActive.asStateFlow()

    private var isMonitoringStarted = false

    /**
     * Applies the requested 90 FPS refresh rate to the Activity window.
     */
    fun applyFrameRate(activity: Activity, target: TargetFps = TargetFps.FPS_90) {
        _activeTargetFps.value = target
        _is90FpsActive.value = (target == TargetFps.FPS_90 || target == TargetFps.FPS_120)

        try {
            val window: Window = activity.window
            val layoutParams: WindowManager.LayoutParams = window.attributes

            if (target == TargetFps.SYSTEM_DEFAULT) {
                layoutParams.preferredRefreshRate = 0f
                layoutParams.preferredDisplayModeId = 0
            } else {
                val desiredRate = target.fps

                // 1. Set preferred refresh rate (API 23+)
                layoutParams.preferredRefreshRate = desiredRate

                // 2. Inspect display modes to find 90Hz hardware display mode
                val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activity.display
                } else {
                    @Suppress("DEPRECATION")
                    window.windowManager.defaultDisplay
                }

                if (display != null) {
                    _activeDisplayHz.value = display.refreshRate

                    val supportedModes = display.supportedModes
                    // Find a mode with refreshRate matching desiredRate closely
                    val exactMode = supportedModes.firstOrNull { mode ->
                        abs(mode.refreshRate - desiredRate) < 1.5f
                    } ?: supportedModes.filter { it.refreshRate >= desiredRate }
                        .minByOrNull { it.refreshRate }

                    if (exactMode != null) {
                        layoutParams.preferredDisplayModeId = exactMode.modeId
                        Log.i(TAG, "Selected display mode: id=${exactMode.modeId}, rate=${exactMode.refreshRate}Hz")
                    }
                }
            }

            window.attributes = layoutParams
            Log.i(TAG, "Applied target FPS: ${target.name} (${target.fps} Hz)")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to apply frame rate setting: ${e.message}")
        }

        startMonitoring()
    }

    /**
     * Starts real-time Choreographer frame tracking to measure rendered frames per second.
     */
    private fun startMonitoring() {
        if (isMonitoringStarted) return
        isMonitoringStarted = true

        var frameCounter = 0
        var lastMeasurementTimeNanos = System.nanoTime()

        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCounter++
                val elapsedNanos = frameTimeNanos - lastMeasurementTimeNanos

                // Recalculate FPS every 800ms for responsive, stable updates
                if (elapsedNanos >= 800_000_000L) {
                    val measuredFps = ((frameCounter * 1_000_000_000L) / elapsedNanos).toInt()
                    val target = _activeTargetFps.value.fps.toInt()

                    // Smooth jitter to avoid flickering when capped at VSYNC
                    val smoothedFps = if (target > 0 && abs(measuredFps - target) <= 2) {
                        target
                    } else if (measuredFps > 0) {
                        measuredFps
                    } else {
                        target.takeIf { it > 0 } ?: 90
                    }

                    _currentFps.value = smoothedFps
                    frameCounter = 0
                    lastMeasurementTimeNanos = frameTimeNanos
                }

                Choreographer.getInstance().postFrameCallback(this)
            }
        }

        Choreographer.getInstance().postFrameCallback(frameCallback)
    }
}
