package com.mobile.narciso

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val isWifiConnected = MutableLiveData<Boolean>()

}