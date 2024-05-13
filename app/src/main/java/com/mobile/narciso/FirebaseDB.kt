package com.mobile.narciso

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreHandler {

    private val TAG1 = "FirestoreHandlerUsr"
    private val TAG2 = "FirestoreHandlerTest"
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    fun addUser(username: String, mail: String){
        val userData = hashMapOf(
            "username" to username,
            "email" to mail,
        )
        db.collection("users").document(username)
            .set(userData)
            .addOnSuccessListener { Log.d(TAG1, "Username successfully added") }
            .addOnFailureListener {e->Log.w(TAG1, "Error writing username in document", e)}
    }

    fun addData(username: String, data: Any){
        db.collection("data").document()
            .set(data)
            .addOnSuccessListener { Log.d(TAG2, " Test data successfully added") }
            .addOnFailureListener {e->Log.w(TAG2, "Error writing test data document", e)}

    }


}
