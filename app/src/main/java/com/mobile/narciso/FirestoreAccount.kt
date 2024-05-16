package com.mobile.narciso

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
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
        return try{
            val userDoc = db.collection(USR_COLLECTION).document(username).get().await()
            val userData = userDoc.toObject<User>()
            if (userData != null){
                if (userData.email == mail && userData.hashedpassword == hashPassw(password)){
                    true
                }else{
                    false
                }
            }else{
                false
            }

        }catch (e: Exception){
            Log.w("Error getting user data from cloud", e)
            false
        }
    }

    suspend fun checkEmailExists(mail: String): Boolean{
        return try{
            val userDocs = db.collection(USR_COLLECTION).whereEqualTo("email", mail).get().await()
            !userDocs.isEmpty

        }catch (e: Exception){
            Log.w(TAG, "No document with target mail found", e)
            false
        }
    }

    suspend fun resetPassword(mail: String): String{
        val newPassw = generateRandomPassword(8)
        val newPasswHash = hashPassw(newPassw)
        val userDoc = db.collection(USR_COLLECTION).whereEqualTo("email", mail)
            .get().await()
        val userInfo = userDoc.first().toObject<User>()
        if(userInfo != null){
            val targetUser = userInfo.username
            db.collection(USR_COLLECTION).document(targetUser!!)
                .update("hashedpassword", newPasswHash)
                .addOnSuccessListener { Log.d(TAG, "New password saved") }
                .addOnFailureListener { e -> Log.w(TAG, "Password change not occured on cloud", e) }
            return newPassw

        }else{  // user doesn't exist
            return "User not found"
        }

    }

    private suspend fun newAccount(user: String, mail: String): Boolean{
        // looking for user with username
        return try{
            val userDoc: DocumentSnapshot = db.collection(USR_COLLECTION).document(user)
                .get().await()
            if(userDoc.exists()) { // if exists user already signed up (no newAccount), otherwise new user
                false
            }else{
                !checkEmailExists(mail)         // looking for user with mail
            }

        }catch (e: Exception){
            Log.w(TAG, "User document not found:", e)
            false
        }
    }

    private fun hashPassw(password: String): String{
        val bytes = password.toByteArray()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest =  messageDigest.digest(bytes)
        return digest.fold("", {str, it -> str + "%02x".format(it)})
    }

    private fun generateRandomPassword(length: Int): String {
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}