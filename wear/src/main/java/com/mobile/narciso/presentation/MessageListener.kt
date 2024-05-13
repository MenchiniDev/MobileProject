package com.mobile.narciso.presentation

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class MessageListener : WearableListenerService(), MessageClient.OnMessageReceivedListener {

    private lateinit var TAG: String
    private lateinit var MESSAGE_PATH: String
    private lateinit var DATA_PATH: String

    private var lastHRsensorData: Float = 0.0f
    private var lastPPGsensorData: Float = 0.0f

    private lateinit var messageClient: MessageClient

    override fun onCreate() {
        super.onCreate()
        TAG = "MessageListener"
        MESSAGE_PATH = "/retrieve_data"
        DATA_PATH = "/send_data"
        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        messageClient.removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == MESSAGE_PATH) {
            val messageReceived = String(messageEvent.data)
            Log.d(TAG, "Message received on wearable: $messageReceived")
            val nodeID = messageEvent.sourceNodeId
            val floatList = listOf(lastHRsensorData, lastPPGsensorData)
            val messageToSend = floatList.joinToString(",")
            val sendMessageTask = messageClient.sendMessage(nodeID, DATA_PATH, messageToSend.toByteArray())
            sendMessageTask.addOnSuccessListener {
                Log.d(TAG, "Float list sent to phone")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed to send float list to phone: ${e.message}")
            }
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
                "PPG" -> lastPPGsensorData = sensorData
            }
        }
        return START_STICKY
    }
}