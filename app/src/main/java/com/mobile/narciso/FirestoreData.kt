package com.mobile.narciso

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

data class SensorsData(
    val testUser: String? = null,
    val imageID: String? = null,
    val HearthRate: Float? = null,
    val PPG: Float? = null,
    val EDA: Float? = null,
    val faceData: FaceLandmarks? = null,
    val likability: Int? = null
)

data class EEGonly(
    val channel1: Double,
    val channel2: Double,
    val channel3: Double,
    val channel4: Double,
    val channel5: Double,
    val channel6: Double
)

class FirestoreDataDAO {

    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }
    private val TAG = "FirestoreHandlerData"
    private val DATA_COLLECTION: String = "data"
    private val USR_COLLECTION: String = "users"
    private val EEG_COLLECTION: String = "EEGdata"



    fun sendData(username: String, data: ArrayList<SensorsData>, EEGdata: ArrayList<EEGsensordata>): Boolean{
        var testCount = runBlocking { getSampleCount(username) }
        if (testCount == -1){
            return false
        }else{
            val EEGdataPacked = EEGdataSelector(data, EEGdata)
            var docCount = 0
            for (test in data){
                val docName = username + "_test" + testCount
                db.collection(DATA_COLLECTION).document(docName)
                    .set(test)
                    .addOnSuccessListener { Log.d(TAG, " Test data number $testCount successfully added") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing test $testCount in data document", e) }
                if(EEGdataPacked.isNotEmpty()){
                    EEDdataSend(username, docName, EEGdataPacked[docCount])   // Adding EEG data corresponding to test sent to cloud
                    docCount++
                }else{
                    Log.w("TAG", "EEG data not collected")
                }
                testCount++

            }
            updateSampleCount(username, testCount)
            return true
        }
    }

    private fun EEGdataSelector(testData: ArrayList<SensorsData>, EEGdata: ArrayList<EEGsensordata>): ArrayList<ArrayList<EEGonly>>{ // Creating and array the same size of data test number in which we assign the EEG values obtained for each test
        val EEGselectedArray: ArrayList<ArrayList<EEGonly>> = ArrayList(testData.size)
        var counter = 0
        for(test in testData){
            val imgCode = test.imageID
            Log.d("EEG check", "Checking for img $imgCode in EEG data ($EEGdata)")
            for (EEGread in EEGdata){
                if(EEGread.imageID == imgCode){
                    val EEGwrite = EEGonly(
                        EEGread.channel1,
                        EEGread.channel2,
                        EEGread.channel3,
                        EEGread.channel4,
                        EEGread.channel5,
                        EEGread.channel6
                    )
                    EEGselectedArray[counter].add(EEGwrite)
                    Log.d("EEG check", "Setting $EEGwrite for test $counter")
                }else{      // supposed EEG sensor data in test order, when the img code doesn't correspond means we don't have any other EEG data for that image
                    Log.d("EEG check", "No image $imgCode corresponding for test $counter, EEG image is: ${EEGread.imageID}")
                }
            }
            counter++
        }
        return EEGselectedArray
    }

    private fun EEDdataSend(user: String?, testDocName: String, EEGdata: ArrayList<EEGonly>){
        var EEGsampleCount = 0
        for(EEGsample in EEGdata){
            val EEGdocName = "sample"+EEGsampleCount
            db.collection(DATA_COLLECTION).document(testDocName).collection(EEG_COLLECTION).document(EEGdocName)
                .set(EEGsample, SetOptions.merge())
                .addOnSuccessListener { Log.d(TAG, " EGG sample num $EEGsampleCount for $testDocName added correctly") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing EGG sample num $EEGsampleCount in document $testDocName", e) }
        }
    }

    private fun updateSampleCount(username: String, testsDone: Int = 1){
        val newTestCount = testsDone
        db.collection(USR_COLLECTION).document(username)
            .update("testsDone", newTestCount)
            .addOnSuccessListener { Log.d(TAG, " Test count updated to $newTestCount") }
            .addOnFailureListener {e-> Log.w(TAG, "Error updating test count", e)}
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