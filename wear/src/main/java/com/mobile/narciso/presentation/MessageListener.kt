package com.mobile.narciso.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.mobile.narciso.R

/*
 * The class is used to receive messages from the MainActivity and send sensor data to the smartphone.
 *
 * The class has several components:
 * - A MessageClient to send and receive messages.
 * - Variables to store the last sensor data received.
 * - An Intent to update the images count.
 * - Variables for the notification channel and the notification.
 *
 * The class also includes several methods:
 * - onCreate(): A method to set up the MessageClient, create the notification channel and the notification, check for permission.
 * - onDestroy(): A method to remove the MessageClient listener, stop the foreground service, and stop the service itself.
 * - createNotificationChannel(): A method to create the notification channel for the service.
 * - onMessageReceived(messageEvent: MessageEvent): A method to receive and send sensor data to the smartphone, and update the images count.
 * - onStartCommand(intent: Intent?, flags: Int, startId: Int): A method to register the sensor data received from the MainActivity and start the foreground service.
 */

class MessageListener : WearableListenerService(), MessageClient.OnMessageReceivedListener {

    private lateinit var TAG: String
    private lateinit var MESSAGE_PATH: String
    private lateinit var DATA_PATH: String

    private var lastHRsensorData: Float = 0.0f
    private var lastPPGsensorData: Float = 0.0f
    private var lastEDAsensorData: Float = 0.0f

    private lateinit var messageClient: MessageClient

    private var counter: Int = 0

    private lateinit var updateCounter: Intent

    private lateinit var channelId: String
    private lateinit var channelName: String
    private var importance: Int = 0
    private lateinit var channel: NotificationChannel
    private var notificationId: Int = 123
    private lateinit var notification: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        TAG = "MessageListener"
        MESSAGE_PATH = "/retrieve_data"
        DATA_PATH = "/send_data"
        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(this)

        channelId = "MessageListenerChannel"
        channelName = "Message Listener Service"
        importance = NotificationManager.IMPORTANCE_LOW
        channel = NotificationChannel(channelId, channelName, importance)

        notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Service Running")
            .setContentText("Waiting for messages from the smartphone...")
            .setSmallIcon(R.drawable.splash_icon)
            .setOngoing(true)
            .setSound(null)

        createNotificationChannel()

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        NotificationManagerCompat.from(this).notify(notificationId, notification.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        messageClient.removeListener(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // create notification channel for the service
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = "Message Listener Service"
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // send message with sensor data to the phone
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == MESSAGE_PATH) {
            counter++
            val messageReceived = String(messageEvent.data)
            Log.d(TAG, "Message received on wearable: $messageReceived")
            val nodeID = messageEvent.sourceNodeId
            val floatList = listOf(lastHRsensorData, lastPPGsensorData, lastEDAsensorData)
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
        updateCounter = Intent("updateVariable")
        val str = counter.toString()
        updateCounter.putExtra("variable", str)
        if(counter == 10)
            counter = 0
        sendBroadcast(updateCounter)
    }

    // register the sensor data received from the MainActivity
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val sensorName = intent.getStringExtra("SENSOR_NAME")
            val sensorData = intent.getFloatExtra("SENSOR_DATA", 0.0f)
            when (sensorName) {
                "Heart Rate" -> lastHRsensorData = sensorData
                "PPG" -> lastPPGsensorData = sensorData
                "EDA" -> lastEDAsensorData = sensorData
            }
        }
        startForeground(notificationId, notification.build())
        return START_STICKY
    }
}