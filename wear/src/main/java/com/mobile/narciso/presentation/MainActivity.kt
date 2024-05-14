/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.mobile.narciso.presentation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.mobile.narciso.R
import com.mobile.narciso.presentation.theme.NarcisoTheme
import java.io.IOException
import java.io.OutputStreamWriter

class MainActivity : ComponentActivity() {
    private lateinit var HRsensorManager: SensorManager
    private lateinit var HRsensor: Sensor
    private lateinit var HRsensorEventListener: SensorEventListener

    private lateinit var PPGsensorManager: SensorManager
    private lateinit var PPGsensor: Sensor
    private lateinit var PPGsensorEventListener: SensorEventListener
    var PPGType = 65572
    private var lastFilteredValue: Double = 0.0

    private lateinit var sendIntent: Intent

    private val PERMISSION_REQUEST_CODE = 123

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

        setContent {
            WearApp("Android")
        }

        if(!(checkSelfPermission(android.Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
        }

        sendIntent = Intent(this, MessageListener::class.java)

        HRregisterListener()
        PPGregisterListener()
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

    override fun onRestart() {
        super.onRestart()
        HRregisterListener()
        PPGregisterListener()
    }
    override fun onResume() {
        super.onResume()
        HRregisterListener()
        PPGregisterListener()
    }
    override fun onPause() {
        super.onPause()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
    }
    override fun onStop() {
        super.onStop()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
    }
    override fun onDestroy() {
        super.onDestroy()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
    }
}

@Composable
fun WearApp(greetingName: String) {
    NarcisoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}