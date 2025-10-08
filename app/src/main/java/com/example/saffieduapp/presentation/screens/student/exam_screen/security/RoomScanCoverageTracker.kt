package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * يتابع دوران الجهاز للتأكد من مسح الغرفة:
 * - تغطية أفقية (Yaw) عبر قطاعات sectors
 * - تغطية رأسية (Pitch) برفع/خفض الهاتف
 */
class RoomScanCoverageTracker(
    private val context: Context,
    private val sectorCount: Int = 8,          // 8 قطاعات (كل قطاع 45°)
    private val pitchUpThresholdDeg: Float = 20f,
    private val pitchDownThresholdDeg: Float = -20f
) : SensorEventListener {

    private val TAG = "CoverageTracker"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val visitedSectors = BooleanArray(sectorCount) { false }
    private var minPitch = 999f
    private var maxPitch = -999f

    private val _state = MutableStateFlow(CoverageState())
    val state: StateFlow<CoverageState> = _state.asStateFlow()

    private var started = false

    fun start() {
        if (started) return
        started = true
        reset()
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(
                this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            Log.w(TAG, "Rotation Vector sensor not available")
        }
    }

    fun stop() {
        if (!started) return
        started = false
        try {
            sensorManager.unregisterListener(this)
        } catch (_: Exception) {}
    }

    fun reset() {
        visitedSectors.fill(false)
        minPitch = 999f
        maxPitch = -999f
        _state.value = CoverageState()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)

        // radians → degrees
        val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()  // yaw
        val pitchDeg   = Math.toDegrees(orientation[1].toDouble()).toFloat()  // pitch
        // val rollDeg    = Math.toDegrees(orientation[2].toDouble()).toFloat() // roll (غير ضروري الآن)

        // طَبّع الزاوية إلى 0..360
        val yaw360 = normalizeYawDeg(azimuthDeg)

        // علِّم القطاع الحالي كمُزار
        val sectorSize = 360f / sectorCount
        val sectorIdx = floor(yaw360 / sectorSize).toInt().coerceIn(0, sectorCount - 1)
        if (!visitedSectors[sectorIdx]) {
            visitedSectors[sectorIdx] = true
        }

        // حدِّث الـ pitch min/max
        minPitch = min(minPitch, pitchDeg)
        maxPitch = max(maxPitch, pitchDeg)

        // احسب التغطية
        val yawCovered = visitedSectors.count { it }
        val yawCoveragePercent = yawCovered.toFloat() / sectorCount.toFloat()

        val pitchUpOk = maxPitch >= pitchUpThresholdDeg
        val pitchDownOk = minPitch <= pitchDownThresholdDeg
        val pitchCoveragePercent = when {
            pitchUpOk && pitchDownOk -> 1f
            pitchUpOk || pitchDownOk -> 0.5f
            else -> 0f
        }

        // وزن أفقي 80% + رأسي 20%
        val totalPercent = (yawCoveragePercent * 0.8f) + (pitchCoveragePercent * 0.2f)

        _state.value = CoverageState(
            yawCoveredSectors = yawCovered,
            totalSectors = sectorCount,
            minPitchDeg = minPitch,
            maxPitchDeg = maxPitch,
            yawCoveragePercent = yawCoveragePercent,
            pitchCoveragePercent = pitchCoveragePercent,
            totalPercent = totalPercent,
            yawComplete = yawCoveragePercent >= 0.75f,   // تغطية ≥ 75% من الدائرة
            pitchComplete = pitchCoveragePercent >= 1f,  // شاف فوق وتحت
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* no-op */ }

    private fun normalizeYawDeg(yaw: Float): Float {
        var d = yaw
        while (d < 0f) d += 360f
        while (d >= 360f) d -= 360f
        return d
    }
}

data class CoverageState(
    val yawCoveredSectors: Int = 0,
    val totalSectors: Int = 8,
    val minPitchDeg: Float = 0f,
    val maxPitchDeg: Float = 0f,
    val yawCoveragePercent: Float = 0f,   // 0..1
    val pitchCoveragePercent: Float = 0f, // 0..1
    val totalPercent: Float = 0f,         // 0..1 (الوزن المركّب)
    val yawComplete: Boolean = false,
    val pitchComplete: Boolean = false
) {
    val complete: Boolean get() = yawComplete && pitchComplete
}
