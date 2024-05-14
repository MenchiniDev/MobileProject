package com.mobile.narciso

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    // registration
    fun addUser(username: String, email: String?, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_USERNAME, username)
        values.put(COL_EMAIL, email)
        values.put(COL_PASSWORD, hashPassword(password))
        val result = db.insert(TABLE_NAME, null, values)
        return result != -1L // Ritorna true se l'inserimento è riuscito, altrimenti false
    }

    // credentials control
    fun checkUser(username: String, email: String, password: String): Boolean {
        val db = this.readableDatabase
        val hashedPassword = hashPassword(password) // password hashed
        val cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_NAME + " WHERE " +
                    COL_USERNAME + "=? AND " + COL_EMAIL + "=? AND " + COL_PASSWORD + "=?", arrayOf(username, email, hashedPassword)
        )
        val count = cursor.count
        cursor.close()
        return count > 0 // it has at least 1, so it exists
    }

    // password hashing
    private fun hashPassword(password: String): String? {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray())
            val hexString = StringBuilder()
            for (b: Byte in hash) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return null // Gestione dell'eccezione
        }
    }

    // checking for password reset
    fun checkEmailExists(email: String?): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_EMAIL + "=?", arrayOf(email)
        )
        val count = cursor.count
        cursor.close()
        return count > 0 //same as before
    }


    fun resetPassword(email: String): String {
        val db = this.writableDatabase
        val newPassword = generateRandomPassword(8)
        val hashedPassword = hashPassword(newPassword) // Hashing new password

        val contentValues = ContentValues()
        contentValues.put(COL_PASSWORD, hashedPassword)

        val result = db.update(TABLE_NAME, contentValues, "$COL_EMAIL = ?", arrayOf(email))

        return newPassword
    }

    private fun generateRandomPassword(length: Int): String {
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    companion object {
        private val TAG = "DatabaseHelper"
        private val DATABASE_NAME = "user.db"
        private val DATABASE_VERSION = 2 // Increment this
        private val TABLE_NAME = "users"
        private val COL_ID = "ID"
        private val COL_EMAIL = "EMAIL"
        private val COL_PASSWORD = "PASSWORD"
        private val COL_USERNAME = "USERNAME"
        private val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT, " +
                COL_EMAIL + " TEXT, " +
                COL_PASSWORD + " TEXT);")
    }
}
