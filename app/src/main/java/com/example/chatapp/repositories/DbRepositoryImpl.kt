package com.example.chatapp.repositories

import com.example.chatapp.data.utils.Constants.KEY_COLLECTION_USERS
import com.example.chatapp.data.utils.Constants.KEY_EMAIL
import com.example.chatapp.data.utils.Constants.KEY_NAME
import com.example.chatapp.data.utils.Constants.KEY_UID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DbRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : DbRepository {

    override suspend fun addUserToDatabase() {
        val currentUser = auth.currentUser

        val isUserAlreadyInDatabase = db.collection(KEY_COLLECTION_USERS).document("${currentUser?.uid}").get().await().exists()
        if (!isUserAlreadyInDatabase){
            val user = hashMapOf(
                KEY_NAME to "${currentUser?.displayName}",
                KEY_EMAIL to "${currentUser?.email}",
                KEY_UID to "${currentUser?.uid}"
            )
            db.collection(KEY_COLLECTION_USERS)
                .document("${currentUser?.uid}")
                .set(user)
        }

    }

}