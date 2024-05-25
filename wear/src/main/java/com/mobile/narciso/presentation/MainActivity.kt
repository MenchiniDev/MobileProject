/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.mobile.narciso.presentation

import android.Manifest
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
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
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
    private lateinit var PPGText: TextView
    private var lastFilteredValue: Double = 0.0

    private lateinit var EDAsensorManager: SensorManager
    private lateinit var EDAsensor: Sensor
    private lateinit var EDAsensorEventListener: SensorEventListener
    var EDAType = 65554
    private lateinit var EDAText: TextView

    private lateinit var sendIntent: Intent

    private val PERMISSION_REQUEST_CODE = 123

    private lateinit var newCounter: String

    private var isReceiverRegistered = false

    private lateinit var imagesCount: TextView
    private lateinit var timeTextView: CurvedTextView
    private val handler = Handler(Looper.getMainLooper())

    private val runnableCode = object : Runnable {
        override fun run() {
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            timeTextView.text = getString(R.string.current_time, currentTime)
            handler.postDelayed(this, 60000)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "updateVariable") {
                newCounter = intent.getStringExtra("variable").toString()
                imagesCount.text = getString(R.string.images_seen, newCounter)
            }
        }
    }

//    private val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (!isGranted) {
//                finish()
//            }
//        }

    fun filter(input: Double): Double {
        var alpha = 0.1
        alpha = alpha.coerceIn(0.0, 1.0)
        lastFilteredValue = alpha * input + (1 - alpha) * lastFilteredValue
        return lastFilteredValue
    }

    private fun HRregisterListener() {
        HRsensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        HRsensor = HRsensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)!!
        HRsensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = event.values
                Log.d("Heart Rate", "Heart Rate: ${values[0]}")
                val str = values[0].toString()
                HRText.text = getString(R.string.heart_rate, str)
                sendIntent.putExtra("SENSOR_NAME", "Heart Rate")
                sendIntent.putExtra("SENSOR_DATA", values[0])
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(sendIntent)
                } else {
                    startService(sendIntent)
                }
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
                val str = filteredValue.toInt().toString()
                PPGText.text = getString(R.string.ppg, str)
                sendIntent.putExtra("SENSOR_NAME", "PPG")
                sendIntent.putExtra("SENSOR_DATA", filteredValue.toFloat())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(sendIntent)
                } else {
                    startService(sendIntent)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        PPGsensorManager.registerListener(PPGsensorEventListener, PPGsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun EDAregisterListener() {
        EDAsensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        EDAsensor = EDAsensorManager.getDefaultSensor(EDAType)!!
        EDAsensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = event.values
                Log.d("EDA", "EDA: ${values[2]}")
                val filteredValue = filter(values[2].toDouble())
                Log.d("EDA", "Filtered EDA: $filteredValue")
                val str = filteredValue.toInt().toString()
                EDAText.text = getString(R.string.eda, str)
                sendIntent.putExtra("SENSOR_NAME", "EDA")
                sendIntent.putExtra("SENSOR_DATA", filteredValue.toFloat())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(sendIntent)
                } else {
                    startService(sendIntent)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        EDAsensorManager.registerListener(EDAsensorEventListener, EDAsensor, 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        setContentView(R.layout.activity_main)
        imagesCount = findViewById(R.id.ImagesCount)
        timeTextView = findViewById(R.id.Time)
        HRText = findViewById(R.id.HeartRate)
        HRText.text = getString(R.string.heart_rate, "No Data")
        PPGText = findViewById(R.id.PPG)
        PPGText.text = getString(R.string.ppg, "No Data")
        EDAText = findViewById(R.id.EDA)
        EDAText.text = getString(R.string.eda, "No Data")
        newCounter = "0"
        imagesCount.text = getString(R.string.images_seen, newCounter)
        handler.post(runnableCode)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS, Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)

        sendIntent = Intent(this, MessageListener::class.java)

        HRregisterListener()
        PPGregisterListener()
        EDAregisterListener()
        registerReceiver(receiver, IntentFilter("updateVariable"), RECEIVER_NOT_EXPORTED)
        isReceiverRegistered = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(sendIntent)
        } else {
            startService(sendIntent)
        }
    }

    private fun checkPermission(permission: String) {
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermission(Manifest.permission.BODY_SENSORS)
        HRregisterListener()
        PPGregisterListener()
        EDAregisterListener()
        if (!isReceiverRegistered) {
            registerReceiver(receiver, IntentFilter("updateVariable"), RECEIVER_NOT_EXPORTED)
            isReceiverRegistered = true
        }
    }

    override fun onRestart() {
        super.onRestart()
        checkPermission(Manifest.permission.BODY_SENSORS)
        HRregisterListener()
        PPGregisterListener()
        EDAregisterListener()
        if (!isReceiverRegistered) {
            registerReceiver(receiver, IntentFilter("updateVariable"), RECEIVER_NOT_EXPORTED)
            isReceiverRegistered = true
        }
    }
    override fun onResume() {
        super.onResume()
        checkPermission(Manifest.permission.BODY_SENSORS)
        HRregisterListener()
        PPGregisterListener()
        EDAregisterListener()
        if (!isReceiverRegistered) {
            registerReceiver(receiver, IntentFilter("updateVariable"), RECEIVER_NOT_EXPORTED)
            isReceiverRegistered = true
        }
    }
    override fun onPause() {
        super.onPause()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
        EDAsensorManager.unregisterListener(EDAsensorEventListener)
        if (isReceiverRegistered) {
            unregisterReceiver(receiver)
            isReceiverRegistered = false
        }
    }
    override fun onStop() {
        super.onStop()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
        EDAsensorManager.unregisterListener(EDAsensorEventListener)
        if (isReceiverRegistered) {
            unregisterReceiver(receiver)
            isReceiverRegistered = false
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
        EDAsensorManager.unregisterListener(EDAsensorEventListener)
        if (isReceiverRegistered) {
            unregisterReceiver(receiver)
            isReceiverRegistered = false
        }
        handler.removeCallbacks(runnableCode)
    }
}