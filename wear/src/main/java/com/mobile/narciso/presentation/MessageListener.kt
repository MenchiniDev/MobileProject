package com.mobile.narciso.presentation

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class MessageListener : WearableListenerService() {

    private val TAG = "MessageListener"
    private val MESSAGE_PATH = "/retrieve_data"

    var lastHRsensorData: Float = 0.0f
    var lastECGsensorData: Float = 0.0f
    var lastPPGsensorData: Float = 0.0f

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == MESSAGE_PATH) {
            val message = String(messageEvent.data)
            Log.d(TAG, "Message received on wearable: $message")
            val intent = Intent(this, ReadSensors::class.java)
            startService(intent)
        } else {
            Log.e(TAG, "Message path not recognized")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val sensorName = intent.getStringExtra("SENSOR_NAME")
            val sensorData = intent.getFloatExtra("SENSOR_DATA", 0.0f)
            when (sensorName) {
                "Heart Rate" -> lastHRsensorData = sensorData
                "ECG" -> lastECGsensorData = sensorData
                "PPG" -> lastPPGsensorData = sensorData
            }
        }
        return START_STICKY
    }
}