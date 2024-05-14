package com.mobile.narciso

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

data class User(
    val username: String? = null,
    val email: String? = null,
    val hashedpassword: String? = null,
    val testsDone: Int = 0,
)

class FirestoreAccountDAO {

    val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }
    private val TAG = "FirestoreHandlerUsr"
    private val USR_COLLECTION: String = "users"

    suspend fun addUser(username: String, mail: String, password: String): Boolean{
        // password needs to be hashed
        val userData = User(
            username,
            mail,
            hashPassw(password),
            0,
        )
        val newAccount = checkAccount(username, mail)

        if(newAccount){
            try{
                db.collection(USR_COLLECTION).document(username)
                    .set(userData).await()
                Log.d(TAG, "Username successfully added")
                return true

            }catch (e: Exception){
                Log.w(TAG, "Error writing username in document", e)
                return false
            }
        }else{
            Log.w(TAG, "User already registered")
            return false
        }
    }

    private suspend fun checkAccount(user: String?, mail: String?): Boolean{

    }

    private fun hashPassw(password: String): String{
        val bytes = password.toByteArray()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest =  messageDigest.digest(bytes)
        return digest.fold("", {str, it -> str + "%02x".format(it)})
    }

}