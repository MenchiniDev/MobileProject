package com.mobile.narciso

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * This class provides a simple way to share data between different UI controllers (like Fragments)
 * that observe the same ViewModel. In this case, any UI controller observing 'isWifiConnected' will
 * be notified of changes to the Wi-Fi connection status.
 */

class SharedViewModel : ViewModel() {
    val isWifiConnected = MutableLiveData<Boolean>()

}