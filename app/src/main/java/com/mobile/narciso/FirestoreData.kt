package com.mobile.narciso

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

data class SensorsData(
    val user: String? = null,
    val image: String? = null,
    val HRdata: Float? = null,
    val PPGdata: Float? = null,
    val faceData: FaceLandmarks? = null,
    val likability: String? = null,

    )

class FirestoreDataDAO {

    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }
    private val TAG = "FirestoreHandlerTest"
    private val DATA_COLLECTION: String = "data"
    private val USR_COLLECTION: String = "users"


    fun addData(username: String, data: List<SensorsData>): Boolean{
        var testCount = runBlocking { getSampleCount(username) }
        if (testCount == -1){
            return false
        }else{
            for (test in data){
                val docName = username + "_test" + testCount
                db.collection(DATA_COLLECTION).document(docName)
                    .set(test)
                    .addOnSuccessListener {
                        Log.d(TAG, " Test data number $testCount successfully added")
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing test $testCount in data document", e) }
                testCount++
            }
            updateSampleCount(username, testCount)
            return true
        }
    }

    private fun updateSampleCount(username: String, testsDone: Int = 1){
        val newTestCount = testsDone
        db.collection(USR_COLLECTION).document(username)
            .update("testsDone", newTestCount)
            .addOnSuccessListener { Log.d(TAG, " Test count updated to $newTestCount") }
            .addOnFailureListener {e-> Log.w(TAG, "Error upgrading test count", e)}
    }

    private suspend fun getSampleCount(username: String): Int{
        try{
            val userDoc = db.collection(USR_COLLECTION).document(username).get().await()

            val user = userDoc.toObject<User>()
            if (user != null) {
                return user.testsDone
            }else{
                return -1
            }
        }catch (e: Exception){
            Log.w(TAG, "Error getting sample count", e)
            return -1
        }

    }

}