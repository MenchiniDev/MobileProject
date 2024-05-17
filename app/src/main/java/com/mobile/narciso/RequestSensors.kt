package com.mobile.narciso

import android.content.Intent
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class RequestSensors : WearableListenerService(), MessageClient.OnMessageReceivedListener {

    private lateinit var TAG: String
    private lateinit var MESSAGE_PATH: String
    private lateinit var DATA_PATH: String
    private lateinit var MESSAGE: String

    private var HRsensorData: Float? = null
    private var PPGsensorData: Float? = null
    private var EDAsensorData: Float? = null

    private lateinit var messageClient: MessageClient

    override fun onCreate() {
        super.onCreate()
        TAG = "RequestSensors"
        MESSAGE_PATH = "/retrieve_data"
        DATA_PATH = "/send_data"
        MESSAGE = "Requesting sensor data"
        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        messageClient.removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if(messageEvent.path == DATA_PATH) {
            val message = String(messageEvent.data)
            Log.d(TAG, "Message received on wearable: $message")
            val floatList = message.split(",").map { it.toFloat() }
            HRsensorData = floatList[0]
            PPGsensorData = floatList[1]
            EDAsensorData = floatList[2]
            Log.d(TAG, "Heart Rate: $HRsensorData")
            Log.d(TAG, "PPG: $PPGsensorData")
            Log.d(TAG, "EDA: $EDAsensorData")
            val intent = Intent("com.mobile.narciso.SENSOR_DATA")
            intent.putExtra("HRsensorData", HRsensorData)
            intent.putExtra("PPGsensorData", PPGsensorData)
            intent.putExtra("EDAsensorData", EDAsensorData)
            sendBroadcast(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val wearableNodeTask: Task<List<Node>> = Wearable.getNodeClient(this).connectedNodes
        wearableNodeTask.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                val sendMessageTask: Task<Int> = messageClient.sendMessage(node.id, MESSAGE_PATH, MESSAGE.toByteArray())
                sendMessageTask.addOnSuccessListener {
                    Log.d(TAG, "Message sent to wearable")
                }.addOnFailureListener {
                    Log.e(TAG, "Failed to send message to wearable")
                }
            }
        }
        return START_STICKY
    }
}