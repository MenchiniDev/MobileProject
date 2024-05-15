/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.mobile.narciso.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.widget.CurvedTextView
import com.mobile.narciso.R
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var HRsensorManager: SensorManager
    private lateinit var HRsensor: Sensor
    private lateinit var HRsensorEventListener: SensorEventListener
    private lateinit var HRText: TextView

    private lateinit var PPGsensorManager: SensorManager
    private lateinit var PPGsensor: Sensor
    private lateinit var PPGsensorEventListener: SensorEventListener
    var PPGType = 65572
    private var lastFilteredValue: Double = 0.0
    private lateinit var PPGText: TextView

    private lateinit var sendIntent: Intent

    private val PERMISSION_REQUEST_CODE = 123

    private var newCounter: Int = 0

    private var isReceiverRegistered = false

    private lateinit var timeTextView: CurvedTextView
    private val handler = Handler(Looper.getMainLooper())
    private val runnableCode = object : Runnable {
        override fun run() {
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            timeTextView.text = currentTime
            handler.postDelayed(this, 60000)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "updateVariable") {
                newCounter = intent.getStringExtra("variable")?.toInt() ?: 0
                val imagesCount = findViewById<TextView>(R.id.ImagesCount)
                imagesCount.text = "Images seen: ${newCounter}/10"
            }
        }
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                finish()
            }
        }

    private fun HRregisterListener() {
        HRsensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        HRsensor = HRsensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)!!
        HRsensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = event.values
                Log.d("Heart Rate", "Heart Rate: ${values[0]}")
                HRText.text = "HR: ${values[0]}"
                sendIntent.putExtra("SENSOR_NAME", "Heart Rate")
                sendIntent.putExtra("SENSOR_DATA", values[0])
                startService(sendIntent)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        HRsensorManager.registerListener(HRsensorEventListener, HRsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    private fun PPGregisterListener() {
        PPGsensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        PPGsensor = PPGsensorManager.getDefaultSensor(PPGType)!!
        PPGsensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = event.values
                Log.d("PPG", "PPG: ${values[2]}")
                val filteredValue = filter(values[2].toDouble())
                Log.d("PPG", "Filtered PPG: $filteredValue")
                PPGText.text = "PPG: ${filteredValue}"
                sendIntent.putExtra("SENSOR_NAME", "PPG")
                sendIntent.putExtra("SENSOR_DATA", values[2])
                startService(sendIntent)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        PPGsensorManager.registerListener(PPGsensorEventListener, PPGsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    fun filter(input: Double): Double {
        var alpha = 0.1
        alpha = alpha.coerceIn(0.0, 1.0)
        lastFilteredValue = alpha * input + (1 - alpha) * lastFilteredValue
        return lastFilteredValue
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        setContentView(R.layout.activity_main)
        timeTextView = findViewById(R.id.Time)
        HRText = findViewById(R.id.HeartRate)
        PPGText = findViewById(R.id.PPG)
        handler.post(runnableCode)

        if(!(checkSelfPermission(android.Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
        }

        sendIntent = Intent(this, MessageListener::class.java)

        HRregisterListener()
        PPGregisterListener()
        registerReceiver(receiver, IntentFilter("updateVariable"), RECEIVER_NOT_EXPORTED)
        isReceiverRegistered = true
        startService(sendIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        HRregisterListener()
        PPGregisterListener()
        if (!isReceiverRegistered) {
            registerReceiver(receiver, IntentFilter("updateVariable"), RECEIVER_NOT_EXPORTED)
            isReceiverRegistered = true
        }
    }

    override fun onRestart() {
        super.onRestart()
        HRregisterListener()
        PPGregisterListener()
        if (!isReceiverRegistered) {
            registerReceiver(receiver, IntentFilter("updateVariable"), RECEIVER_NOT_EXPORTED)
            isReceiverRegistered = true
        }
    }
    override fun onResume() {
        super.onResume()
        HRregisterListener()
        PPGregisterListener()
        if (!isReceiverRegistered) {
            registerReceiver(receiver, IntentFilter("updateVariable"), RECEIVER_NOT_EXPORTED)
            isReceiverRegistered = true
        }
    }
    override fun onPause() {
        super.onPause()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
        if (isReceiverRegistered) {
            unregisterReceiver(receiver)
            isReceiverRegistered = false
        }
    }
    override fun onStop() {
        super.onStop()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
        if (isReceiverRegistered) {
            unregisterReceiver(receiver)
            isReceiverRegistered = false
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
        if (isReceiverRegistered) {
            unregisterReceiver(receiver)
            isReceiverRegistered = false
        }
        handler.removeCallbacks(runnableCode)
    }
}