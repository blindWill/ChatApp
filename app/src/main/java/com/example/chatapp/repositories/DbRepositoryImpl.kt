package com.example.chatapp.repositories

import com.example.chatapp.data.Resource
import com.example.chatapp.data.User
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

        val isUserAlreadyInDatabase =
            db.collection(KEY_COLLECTION_USERS).document("${currentUser?.uid}").get().await()
                .exists()
        if (!isUserAlreadyInDatabase) {
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

    override suspend fun getUsersFromDatabase(): Resource<List<User>>  {
        return try {
            val usersList = ArrayList<User>()
            val task = db.collection(KEY_COLLECTION_USERS)
                .whereNotEqualTo(KEY_UID, "${auth.currentUser?.uid}").get().await()
            for (document in task) {
                val user = User(
                    document.data[KEY_NAME].toString(),
                    document.data[KEY_EMAIL].toString(),
                    document.data[KEY_UID].toString()
                )
                usersList.add(user)
            }
            Resource.Success(usersList)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }



}