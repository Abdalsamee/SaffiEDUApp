package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * يتعقّب التغطية باستخدام مستشعر ROTATION_VECTOR
 * (لا يحتاج صلاحيات).
 */
class RoomScanCoverageTracker(
    context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _state = MutableStateFlow(CoverageState())
    val state = _state.asStateFlow()

    private var minYaw = Float.MAX_VALUE
    private var maxYaw = Float.MIN_VALUE
    private var minPitch = Float.MAX_VALUE
    private var maxPitch = Float.MIN_VALUE

    // حدود تقريبية
    private val yawSpanForFull = 180f        // لو لف تقريباً 180°
    private val pitchUpThreshold = -20f      // رفع الهاتف للأعلى (pitch سالبة)
    private val pitchDownThreshold = 40f     // خفض الهاتف للأسفل (pitch موجبة)

    fun start() {
        reset()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun reset() {
        minYaw = Float.MAX_VALUE
        maxYaw = Float.MIN_VALUE
        minPitch = Float.MAX_VALUE
        maxPitch = Float.MIN_VALUE
        _state.value = CoverageState()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        val rot = FloatArray(9)
        val orientation = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(rot, event.values)
        SensorManager.getOrientation(rot, orientation)

        // azimuth(yaw), pitch, roll بالراديان -> درجات
        val yawDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()      // -180 .. 180
        val pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()    // -90 .. 90

        minYaw = kotlin.math.min(minYaw, yawDeg)
        maxYaw = kotlin.math.max(maxYaw, yawDeg)
        minPitch = kotlin.math.min(minPitch, pitchDeg)
        maxPitch = kotlin.math.max(maxPitch, pitchDeg)

        val yawSpan = abs(maxYaw - minYaw).coerceAtMost(360f)
        val yawPercent = (yawSpan / yawSpanForFull).coerceIn(0f, 1f)

        val upReached = pitchDeg <= pitchUpThreshold
        val downReached = pitchDeg >= pitchDownThreshold
        val pitchPercent = when {
            upReached && downReached -> 1f
            upReached || downReached -> 0.6f
            else -> 0.2f
        }

        _state.value = _state.value.copy(
            yawCoveragePercent = yawPercent,
            pitchCoveragePercent = pitchPercent,
            pitchUpReached = upReached || _state.value.pitchUpReached,
            pitchDownReached = downReached || _state.value.pitchDownReached
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
