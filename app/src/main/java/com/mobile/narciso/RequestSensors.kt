package com.mobile.narciso

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import java.nio.charset.StandardCharsets

class RequestSensors : Service(), MessageClient.OnMessageReceivedListener {

    private val TAG = "RequestSensors"
    private val MESSAGE_PATH = "/retrieve_data"

    private lateinit var messageClient: MessageClient
    override fun onCreate() {
        super.onCreate()
        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        messageClient.removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if(messageEvent.path == MESSAGE_PATH) {
            val message = String(messageEvent.data, StandardCharsets.UTF_8)
            Log.d(TAG, "Message received on wearable: $message")
            TODO("Gestisci il messaggio ricevuto")
        }
    }

    private fun startServiceOnWearable(message: String) {
        val wearableNodeTask: Task<List<Node>> = Wearable.getNodeClient(this).connectedNodes
        wearableNodeTask.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                val sendMessageTask: Task<Int> = messageClient.sendMessage(node.id, MESSAGE_PATH, message.toByteArray())
                sendMessageTask.addOnSuccessListener {
                    Log.d(TAG, "Message sent to wearable")
                }.addOnFailureListener {
                    Log.e(TAG, "Failed to send message to wearable")
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServiceOnWearable("Request sensors")
        return START_STICKY
    }
}