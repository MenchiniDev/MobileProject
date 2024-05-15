package com.mobile.narciso.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.mobile.narciso.R

class MessageListener : WearableListenerService(), MessageClient.OnMessageReceivedListener {

    private lateinit var TAG: String
    private lateinit var MESSAGE_PATH: String
    private lateinit var DATA_PATH: String

    private var lastHRsensorData: Float = 0.0f
    private var lastPPGsensorData: Float = 0.0f

    private lateinit var messageClient: MessageClient

    private var counter: Int = 0

    private lateinit var updateCounter: Intent

    override fun onCreate() {
        super.onCreate()
        TAG = "MessageListener"
        MESSAGE_PATH = "/retrieve_data"
        DATA_PATH = "/send_data"
        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(this)

        //code from MenchiniDev
        val channelId = "MessageListenerChannel"
        val channelName = "Message Listener Service"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Service Running")
            .setContentText("Message Listener is running...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        messageClient.removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == MESSAGE_PATH) {
            counter++
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
        if(counter == 10)
            counter = 0
        updateCounter = Intent("updateVariable")
        updateCounter.putExtra("variable", counter)
        sendBroadcast(updateCounter)
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