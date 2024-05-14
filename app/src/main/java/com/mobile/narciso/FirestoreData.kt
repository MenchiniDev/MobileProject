package com.mobile.narciso

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

data class SensorsData(
    val user: String? = null,
    val image: String? = null,
    val HRdata: Float? = null,
    val PPGdata: Float? = null,
    val faceData: FaceLandmarks? = null,
    val likability: String? = null,

    )

class FirestoreDataDAO {

    val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }
    private val TAG = "FirestoreHandlerTest"
    private var testCount: Int = 0
    private val DATA_COLLECTION: String = "data"

    fun addData(username: String, data: Any): Boolean{
        getSampleCount(username)
        if (testCount == -1){
            return false
        }else{
            val docName = username + "_test" + testCount
            db.collection(DATA_COLLECTION).document(docName)
                .set(data)
                .addOnSuccessListener {
                    Log.d(TAG, " Test data successfully added")
                    updateSampleCount(username)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing test data document", e) }
            return true
        }
    }

    private fun updateSampleCount(username: String){
        val newTestCount = testCount+1
        db.collection("users").document(username)
            .update("testsDone", newTestCount)
            .addOnSuccessListener { Log.d(TAG, " Test count updated") }
            .addOnFailureListener {e-> Log.w(TAG, "Error upgrading test count", e)}
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
                    e->
                Log.w(TAG, "Error getting sample count", e)
                testCount = -1
            }
    }

}