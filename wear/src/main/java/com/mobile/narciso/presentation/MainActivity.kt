/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.mobile.narciso.presentation

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.mobile.narciso.R
import com.mobile.narciso.presentation.theme.NarcisoTheme

class MainActivity : ComponentActivity() {
    private lateinit var HRsensorManager: SensorManager
    private lateinit var HRsensor: Sensor
    private lateinit var HRsensorEventListener: SensorEventListener

    private lateinit var ECGsensorManager: SensorManager
    private lateinit var ECGsensor: Sensor
    private lateinit var ECGsensorEventListener: SensorEventListener
    var ECGType = 65550

    private lateinit var PPGsensorManager: SensorManager
    private lateinit var PPGsensor: Sensor
    private lateinit var PPGsensorEventListener: SensorEventListener
    var PPGType = 65572
    val intent = Intent(this, MessageListener::class.java)

    private fun HRregisterListener() {
        HRsensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        HRsensor = HRsensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)!!
        HRsensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = event.values
                Log.d("Heart Rate", "Heart Rate: ${values[0]}")
                intent.putExtra("SENSOR_NAME", "Heart Rate")
                intent.putExtra("SENSOR_DATA", values[0])
                startService(intent)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Gestisci i cambiamenti di accuratezza se necessario
            }
        }
        HRsensorManager.registerListener(HRsensorEventListener, HRsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    private fun ECGregisterListener() {
        ECGsensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        ECGsensor = ECGsensorManager.getDefaultSensor(ECGType)!!
        ECGsensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = event.values
                Log.d("ECG", "ECG: ${values[0]}")
                intent.putExtra("SENSOR_NAME", "ECG")
                intent.putExtra("SENSOR_DATA", values[0])
                startService(intent)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Gestisci i cambiamenti di accuratezza se necessario
            }
        }
        ECGsensorManager.registerListener(ECGsensorEventListener, ECGsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun PPGregisterListener() {
        PPGsensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        PPGsensor = PPGsensorManager.getDefaultSensor(PPGType)!!
        PPGsensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = event.values
                Log.d("PPG", "PPG: ${values[0]}")
                intent.putExtra("SENSOR_NAME", "PPG")
                intent.putExtra("SENSOR_DATA", values[0])
                startService(intent)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Gestisci i cambiamenti di accuratezza se necessario
            }
        }
        PPGsensorManager.registerListener(PPGsensorEventListener, PPGsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("Android")
        }
        HRregisterListener()
        ECGregisterListener()
        PPGregisterListener()
    }

    override fun onStart() {
        super.onStart()
        startService(intent)
    }

    override fun onPause() {
        super.onPause()
        HRsensorManager.unregisterListener(HRsensorEventListener)
        ECGsensorManager.unregisterListener(ECGsensorEventListener)
        PPGsensorManager.unregisterListener(PPGsensorEventListener)
    }

    override fun onResume() {
        super.onResume()
        HRregisterListener()
        ECGregisterListener()
        PPGregisterListener()
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