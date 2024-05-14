package com.mobile.narciso

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

data class User(
    val username: String? = null,
    val email: String? = null,
    val hashedpassword: String? = null,
    val testsDone: Int = 0,
)

data class SensorsData(
    val user: String? = null,
    val image: String? = null,
    val HRdata: Float? = 0,
    val PPGdata: Float? = 0,
    val faceData: FaceLandmarks? = null,
    val likability: String? = null,

)

class FirestoreHandler {

    private val TAG1 = "FirestoreHandlerUsr"
    private val TAG2 = "FirestoreHandlerTest"
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }
    private var testCount: Int = 0

    fun addUser(username: String, mail: String, password: String){
        // password needs to be hashed
        val userData = User(
            username,
            mail,
            hashPassw(password),
            0,
        )
        db.collection("users").document(username)
            .set(userData)
            .addOnSuccessListener { Log.d(TAG1, "Username successfully added") }
            .addOnFailureListener {e->Log.w(TAG1, "Error writing username in document", e)}
    }

    fun addData(username: String, data: Any): Boolean{
        getSampleCount(username)
        if (testCount == -1){
            return false
        }else{
            val docName = username + "_test" + testCount
            db.collection("data").document(docName)
                .set(data)
                .addOnSuccessListener {
                    Log.d(TAG2, " Test data successfully added")
                    updateSampleCount(username)
                }
                .addOnFailureListener { e -> Log.w(TAG2, "Error writing test data document", e) }
            return true
        }
    }

    private fun updateSampleCount(username: String){
        val newTestCount = testCount+1
        db.collection("users").document(username)
            .update("testsDone", newTestCount)
            .addOnSuccessListener { Log.d(TAG2, " Test count updated") }
            .addOnFailureListener {e->Log.w(TAG2, "Error upgrading test count", e)}
    }

    private fun getSampleCount(username: String){
        db.collection("users").document(username)
            .get()
            .addOnSuccessListener {documentSnapshot ->
                val user = documentSnapshot.toObject<User>()
                if (user != null) {
                    testCount = user.testsDone
                }
            }
            .addOnFailureListener {
                e->Log.w(TAG2, "Error getting sample count", e)
                testCount = -1
            }
    }

    private fun hashPassw(password: String): String{
        val bytes = password.toByteArray()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest =  messageDigest.digest(bytes)
        return digest.fold("", {str, it -> str + "%02x".format(it)})
    }


}
