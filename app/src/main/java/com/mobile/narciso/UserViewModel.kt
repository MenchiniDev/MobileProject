package com.mobile.narciso

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel(application: Application) : ViewModel() {

    private val _authenticatedUser = MutableLiveData<Login.User>()
    val authenticatedUser: LiveData<Login.User>
        get() = _authenticatedUser

    fun login(email: String, password: String) {
        // Perform authentication logic here, e.g. using a Repository
        // For this example, let's assume the user is authenticated
        _authenticatedUser.value = Login.User(email, password)
    }
}