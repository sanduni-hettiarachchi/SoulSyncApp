package com.wellnessapp.sensors

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import com.wellnessapp.data.DataManager

/**
 * Service for counting steps using accelerometer sensor
 * Implements step detection algorithm based on accelerometer data
 */
class StepCounterService : Service(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var dataManager: DataManager
    
    // Step detection variables
    private var lastAcceleration = 0f
    private var stepCount = 0
    private var lastStepTime = 0L
    private val stepThreshold = 1.5f // Minimum acceleration change to register a step
    private val minStepInterval = 200L // Minimum time between steps (ms)
    
    // Shake detection variables
    private var lastShakeTime = 0L
    private val shakeThreshold = 15f // Minimum acceleration for shake detection
    private val minShakeInterval = 2000L // Minimum time between shake detections (ms)
    
    companion object {
        private const val TAG = "StepCounterService"
        const val ACTION_START_STEP_COUNTING = "com.wellnessapp.START_STEP_COUNTING"
        const val ACTION_STOP_STEP_COUNTING = "com.wellnessapp.STOP_STEP_COUNTING"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "StepCounterService created")
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        dataManager = DataManager.getInstance(this)
        
        // Load current step count
        stepCount = dataManager.getSteps()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_STEP_COUNTING -> {
                startStepCounting()
            }
            ACTION_STOP_STEP_COUNTING -> {
                stopStepCounting()
            }
        }
        return START_STICKY
    }
    
    private fun startStepCounting() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Started step counting")
        } else {
            Log.e(TAG, "Accelerometer not available")
        }
    }
    
    private fun stopStepCounting() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Stopped step counting")
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            // Calculate magnitude of acceleration
            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            
            // Detect step based on acceleration pattern
            if (detectStep(acceleration)) {
                stepCount++
                dataManager.saveSteps(stepCount)
                Log.d(TAG, "Step detected! Total steps: $stepCount")
                
                // Broadcast step update
                val intent = Intent("com.wellnessapp.STEP_COUNT_UPDATED")
                intent.putExtra("step_count", stepCount)
                sendBroadcast(intent)
            }
            
            // Detect shake for quick mood logging
            if (detectShake(acceleration)) {
                Log.d(TAG, "Shake detected! Triggering quick mood logging")
                
                // Broadcast shake detection for quick mood logging
                val shakeIntent = Intent("com.wellnessapp.SHAKE_DETECTED")
                sendBroadcast(shakeIntent)
            }
            
            lastAcceleration = acceleration
        }
    }
    
    private fun detectStep(acceleration: Float): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Check if enough time has passed since last step
        if (currentTime - lastStepTime < minStepInterval) {
            return false
        }
        
        // Simple step detection: look for significant acceleration change
        val accelerationChange = Math.abs(acceleration - lastAcceleration)
        
        if (accelerationChange > stepThreshold) {
            lastStepTime = currentTime
            return true
        }
        
        return false
    }
    
    private fun detectShake(acceleration: Float): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Check if enough time has passed since last shake
        if (currentTime - lastShakeTime < minShakeInterval) {
            return false
        }
        
        // Shake detection: look for high acceleration magnitude
        if (acceleration > shakeThreshold) {
            lastShakeTime = currentTime
            return true
        }
        
        return false
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopStepCounting()
        Log.d(TAG, "StepCounterService destroyed")
    }
}
