package com.mobile.narciso

import android.content.Context
import android.content.SharedPreferences

/*
 * SessionManager is used to manage user sessions.
 * It uses Android's SharedPreferences to store and retrieve user data, specifically the username in this case.
 *
 * The SessionManager class has the following components:
 *
 * - A constructor that accepts a Context object. This context is used to access the SharedPreferences.
 *
 * - A private SharedPreferences instance that is initialized in the constructor using the provided context.
 *   The SharedPreferences file is named "UserSession" and is accessed in private mode.
 *
 * This class provides a simple way to manage user sessions by persistently storing and retrieving the username.
 */

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    var username: String?
        get() = prefs.getString("username", null)
        set(value) = prefs.edit().putString("username", value).apply()
}