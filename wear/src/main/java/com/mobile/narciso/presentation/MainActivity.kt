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

//    private lateinit var ECGsensorManager: SensorManager
//    private lateinit var ECGsensor: Sensor
//    private lateinit var ECGsensorEventListener: SensorEventListener
//    var ECGType = 65550
//
//    private lateinit var PPGsensorManager: SensorManager
//    private lateinit var PPGsensor: Sensor
//    private lateinit var PPGsensorEventListener: SensorEventListener
//    var PPGType = 65572

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
//                sendIntent.putExtra("SENSOR_NAME", "Heart Rate")
//                sendIntent.putExtra("SENSOR_DATA", values[0])
//                startService(sendIntent)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Gestisci i cambiamenti di accuratezza se necessario
            }
        }
        HRsensorManager.registerListener(HRsensorEventListener, HRsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
//    private fun ECGregisterListener() {
//        ECGsensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        ECGsensor = ECGsensorManager.getDefaultSensor(ECGType)!!
//        ECGsensorEventListener = object : SensorEventListener {
//            override fun onSensorChanged(event: SensorEvent) {
//                val values = event.values
//                Log.d("ECG", "ECG: ${values[0]}")
//                sendIntent.putExtra("SENSOR_NAME", "ECG")
//                sendIntent.putExtra("SENSOR_DATA", values[0])
//                startService(sendIntent)
//            }
//            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//                // Gestisci i cambiamenti di accuratezza se necessario
//            }
//        }
//        ECGsensorManager.registerListener(ECGsensorEventListener, ECGsensor, SensorManager.SENSOR_DELAY_NORMAL)
//    }
//
//    private fun PPGregisterListener() {
//        PPGsensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        PPGsensor = PPGsensorManager.getDefaultSensor(PPGType)!!
//        PPGsensorEventListener = object : SensorEventListener {
//            override fun onSensorChanged(event: SensorEvent) {
//                val values = event.values
//                Log.d("PPG", "PPG: ${values[0]}")
//                sendIntent.putExtra("SENSOR_NAME", "PPG")
//                sendIntent.putExtra("SENSOR_DATA", values[0])
//                startService(sendIntent)
//            }
//            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//                // Gestisci i cambiamenti di accuratezza se necessario
//            }
//        }
//        PPGsensorManager.registerListener(PPGsensorEventListener, PPGsensor, SensorManager.SENSOR_DELAY_NORMAL)
//    }
    fun tentativounpostrano() {
        var sensorManager: SensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        var SensorEventListener: SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor == sensorManager?.getDefaultSensor(65554) || //eda
                    event.sensor == sensorManager?.getDefaultSensor(65572) || //ppg
                    event.sensor == sensorManager?.getDefaultSensor(65550)    //ecg
                ) {
                    try {
                        val fileTitle =
                            when(event.sensor) {
                                sensorManager?.getDefaultSensor(65554) -> "eda.csv"
                                sensorManager?.getDefaultSensor(65572) -> "ppg.csv"
                                sensorManager?.getDefaultSensor(65550) -> "ecg.csv"
                                else -> "test.csv"
                            }
                        val fos = openFileOutput(fileTitle, Context.MODE_APPEND)
                        val writer = OutputStreamWriter(fos)

                        var header = "timestamp"
                        if (fos.channel.size() == 0L) {
                            for(i in 0..event.values.size) {
                                header += ",value$i"
                            }
                            writer.write(header+"\n")
                        }
                        val timestamp = System.currentTimeMillis()
                        val dataTest = listOf(timestamp)
                        for(i in 0..event.values.size) {
                            dataTest.plus(event.values[i])
                        }

                        val csvRow = dataTest.joinToString(separator = ",")
                        writer.write(csvRow)
                        writer.write("\n") // new line

                        writer.close()
                        fos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Gestisci i cambiamenti di accuratezza se necessario
            }
        }
    val EDAsensor = sensorManager?.getDefaultSensor(65554) //eda
    val PPGsensor = sensorManager?.getDefaultSensor(65572) //ppg
    val ECGsensor = sensorManager?.getDefaultSensor(65550) //ecg
    sensorManager.registerListener(SensorEventListener, EDAsensor, SensorManager.SENSOR_DELAY_NORMAL)
    sensorManager.registerListener(SensorEventListener, PPGsensor, SensorManager.SENSOR_DELAY_NORMAL)
    sensorManager.registerListener(SensorEventListener, ECGsensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    /*
    if (event.sensor == sensorManager?.getDefaultSensor(65554) || //eda
            event.sensor == sensorManager?.getDefaultSensor(65572) || //ppg
            event.sensor == sensorManager?.getDefaultSensor(65550)    //ecg
        ) {
            try {
                val fileTitle =
                when(event.sensor) {
                    sensorManager?.getDefaultSensor(65554) -> "eda.csv"
                    sensorManager?.getDefaultSensor(65572) -> "ppg.csv"
                    sensorManager?.getDefaultSensor(65550) -> "ecg.csv"
                    else -> "test.csv"
                }
                val fos = openFileOutput(fileTitle, Context.MODE_APPEND)
                val writer = OutputStreamWriter(fos)

                var header = "timestamp"
                if (fos.channel.size() == 0L) {
                    for(i in 0..event.values.size) {
                        header += ",value$i"
                    }
                    writer.write(header+"\n")
                }
                val dataTest = listOf(timestamp)
                for(i in 0..event.values.size) {
                    dataTest.plus(event.values[i])
                }

                val csvRow = dataTest.joinToString(separator = ",")
                writer.write(csvRow)
                writer.write("\n") // new line

                writer.close()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
     */
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
//        ECGregisterListener()
//        PPGregisterListener()
//        tentativounpostrano()
    }

    //se l'utente non accetta i permessi chiusura dell'app
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
        startService(sendIntent)
    }

    override fun onPause() {
        super.onPause()
        HRsensorManager.unregisterListener(HRsensorEventListener)
//        ECGsensorManager.unregisterListener(ECGsensorEventListener)
//        PPGsensorManager.unregisterListener(PPGsensorEventListener)
    }

    override fun onResume() {
        super.onResume()
        HRregisterListener()
//        ECGregisterListener()
//        PPGregisterListener()
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