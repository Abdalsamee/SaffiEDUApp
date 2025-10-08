package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * يقرأ دوران الجهاز ويوجه الطالب لتحريك الهاتف (يسار/يمين/أعلى/أسفل)
 */
class ScanGuidanceController(context: Context) : SensorEventListener {

    enum class Direction { HOLD, LEFT, RIGHT, UP, DOWN }

    data class ScanHint(
        val direction: Direction = Direction.HOLD,
        val message: String = "حرّك الهاتف ببطء لمسح الغرفة"
    )

    private val TAG = "ScanGuidanceController"
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _hint = MutableStateFlow(ScanHint())
    val hint: StateFlow<ScanHint> = _hint.asStateFlow()

    // baseline زوايا البداية كمقارنة
    private var baselineYaw = Float.NaN
    private var baselinePitch = Float.NaN

    // ثوابت حساسية الحركة (درجات)
    private val yawThreshold = 12f   // لليسار/اليمين
    private val pitchThreshold = 10f // لأعلى/أسفل

    fun start() {
        try {
            resetBaseline()
            sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting sensors", e)
        }
    }

    fun stop() {
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sensors", e)
        }
    }

    fun resetBaseline() {
        baselineYaw = Float.NaN
        baselinePitch = Float.NaN
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        val rm = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rm, event.values)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rm, orientation)

        // بالدرجات: yaw(Z), pitch(X), roll(Y)
        val yaw = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()

        if (baselineYaw.isNaN()) {
            baselineYaw = yaw
            baselinePitch = pitch
        }

        val dYaw = normalizeDegrees(yaw - baselineYaw)
        val dPitch = pitch - baselinePitch

        val dir = when {
            dYaw <= -yawThreshold -> Direction.LEFT
            dYaw >= yawThreshold  -> Direction.RIGHT
            dPitch <= -pitchThreshold -> Direction.UP
            dPitch >= pitchThreshold  -> Direction.DOWN
            else -> Direction.HOLD
        }

        val msg = when (dir) {
            Direction.LEFT  -> "حرّك الهاتف ببطء إلى اليسار"
            Direction.RIGHT -> "حرّك الهاتف ببطء إلى اليمين"
            Direction.UP    -> "ارفع الهاتف قليلًا لأعلى"
            Direction.DOWN  -> "اخفض الهاتف قليلًا لأسفل"
            Direction.HOLD  -> "استمر بالتحريك البطيء لمسح الزوايا"
        }

        _hint.value = ScanHint(dir, msg)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* لا شيء */ }

    private fun normalizeDegrees(deg: Float): Float {
        var d = deg
        while (d > 180f) d -= 360f
        while (d < -180f) d += 360f
        return (d * 10).roundToInt() / 10f
    }
}
