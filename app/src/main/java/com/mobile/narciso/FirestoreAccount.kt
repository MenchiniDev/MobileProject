package com.mobile.narciso

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
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

    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }
    private val TAG = "FirestoreHandlerUsr"
    private val USR_COLLECTION: String = "users"

    suspend fun addUser(username: String, mail: String, password: String): Int{
        // password needs to be hashed
        val userData = User(
            username,
            mail,
            hashPassw(password),
            0,
        )
        val newAccount = newAccount(username, mail)

        if(newAccount){
            try{
                db.collection(USR_COLLECTION).document(username)
                    .set(userData).await()
                Log.d(TAG, "Username successfully added")
                return 1

            }catch (e: Exception){
                Log.w(TAG, "Error writing username in document", e)
                return -1
            }
        }else{
            Log.w(TAG, "User already registered")
            return 0
        }
    }

    suspend fun checkAccount(username: String, mail: String, password: String): Boolean{
        try{
            val userDoc = db.collection(USR_COLLECTION).document(username).get().await()
            val userData = userDoc.toObject<User>()
            if (userData != null){
                if (userData.email == mail && userData.hashedpassword == hashPassw(password)){
                    return true
                }else{
                    return false
                }
            }else{
                return false
            }

        }catch (e: Exception){
            Log.w("Error getting user data from cloud", e)
            return false
        }
    }

    suspend fun checkEmailExists(mail: String): Boolean{
        try{
            val userDocs = db.collection(USR_COLLECTION).whereEqualTo("mail", mail).get().await()

            if(userDocs.isEmpty){
                return false
            }else{
                return true
            }
        }catch (e: Exception){
            Log.w(TAG, "Error getting user document", e)
            return false
        }
    }

    /*suspend fun resetPassword(mail: String): String{

    }*/

    private suspend fun newAccount(user: String, mail: String): Boolean{
        // looking for user with username
        try{
            db.collection(USR_COLLECTION).document(user)
                .get().await()
            return false    // document with user registering exists

        }catch (e: Exception){
            Log.w(TAG, "User document not found:", e)
        }
        // looking for user with mail
        try{
            db.collection(USR_COLLECTION)
                .whereEqualTo("mail", mail)
                .get().await()
            return false // found document with target mail

        }catch (e: Exception){
            Log.w(TAG, "No document with target mail found", e)
            return true
        }
    }

    private fun hashPassw(password: String): String{
        val bytes = password.toByteArray()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest =  messageDigest.digest(bytes)
        return digest.fold("", {str, it -> str + "%02x".format(it)})
    }

}