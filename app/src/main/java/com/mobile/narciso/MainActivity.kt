package com.mobile.narciso

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.mobile.narciso.databinding.ActivityMainBinding
import mylibrary.mindrove.SensorData
import mylibrary.mindrove.ServerManager
import java.io.Serializable

class EEGsensordata(val channel1: Double, val channel2: Double, val channel3: Double, val channel4: Double, val channel5: Double, val channel6: Double , val imageID: String): Serializable //, val userVote: Int): Serializable
class MainActivity : AppCompatActivity() {
    private val networkStatus = MutableLiveData("Checking network status...")
    private var isServerManagerStarted = false
    private var i = 0


    private val viewModel: SharedViewModel by viewModels()

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var isWifiSettingsOpen = false


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            val isNetworkAvailable = isNetworkAvailable()
            viewModel.isWifiConnected.value = isNetworkAvailable
            if (!isNetworkAvailable) {
                // If no network, update the network status and open Wi-Fi settings
                viewModel.isWifiConnected.value = false

                if (!isWifiSettingsOpen) {
                    openWifiSettings()
                    isWifiSettingsOpen = true
                }
            } else {
                viewModel.isWifiConnected.value = true
                isWifiSettingsOpen = false
            }
            handler.postDelayed(runnable, 3000)
        }
        handler.post(runnable)

        //end mindrove code
        Log.d("STATUS MINDROVE", "MindroveActivity created")

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val isNetworkAvailable = isNetworkAvailable()
        viewModel.isWifiConnected.value = isNetworkAvailable

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)

        // Stop the server when the activity is destroyed
        Log.d("STATUS MINDROVE", "MindroveActivity DESTROYED")
    }

    // Function to check network connectivity
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private val wifiSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // This block is executed when the Wi-Fi settings activity is finished
            isWifiSettingsOpen = false
        }

    // Function to open Wi-Fi settings
    private fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        wifiSettingsLauncher.launch(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.login)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    companion object {
        var currentImageIndex: String = "0"
        var currentVote: Int = 0
        private val sensorDataText = MutableLiveData("No data yet")
        var EEGsensordataList = ArrayList<EEGsensordata>()
        val serverManager = ServerManager { sensorData: SensorData ->
            Log.d("MindroveActivity", sensorData.channel1.toString())
            // Update the sensor data text
            sensorDataText.postValue(sensorData.channel1.toString())
            sensorDataText.postValue(sensorData.channel2.toString())
            sensorDataText.postValue(sensorData.channel3.toString())
            sensorDataText.postValue(sensorData.channel4.toString())
            sensorDataText.postValue(sensorData.channel5.toString())
            sensorDataText.postValue(sensorData.channel6.toString())

            EEGsensordataList.add(EEGsensordata(sensorData.channel1, sensorData.channel2, sensorData.channel3, sensorData.channel4, sensorData.channel5, sensorData.channel6,
                 currentImageIndex)) //, currentVote)) //default values for imageID and userVote
            Log.d("IMMAGINE INDEX", currentImageIndex)
            Thread.sleep(700)
        }
    }
}