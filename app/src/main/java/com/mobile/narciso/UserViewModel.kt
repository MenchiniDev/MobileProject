package com.mobile.narciso

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val _authenticatedUser = MutableLiveData<User>()
    val authenticatedUser: LiveData<User>
        get() = _authenticatedUser

    fun login(email: String, password: String) {
        // Perform authentication logic here, e.g. using a Repository
        // For this example, let's assume the user is authenticated
        _authenticatedUser.value = User(email, password)
    }
}