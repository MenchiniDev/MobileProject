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
        TODO("aggiorna le variabili in cui sono contenuti i dati dei sensori")
        //se intent extra_text è null, non fare nulla
        //se non è null, aggiorna le variabili in cui sono contenuti i dati dei sensori
        return super.onStartCommand(intent, flags, startId)
    }
//    override fun onBind(intent: Intent): IBinder {
//        return null
//    }
}