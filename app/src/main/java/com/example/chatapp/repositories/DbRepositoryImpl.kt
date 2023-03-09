package com.example.chatapp.repositories

import com.example.chatapp.data.Resource
import com.example.chatapp.data.User
import com.example.chatapp.utils.Constants
import com.example.chatapp.utils.Constants.KEY_AVAILABILITY
import com.example.chatapp.utils.Constants.KEY_CHATROOM_NAMES
import com.example.chatapp.utils.Constants.KEY_CHATROOM_USERS_ID
import com.example.chatapp.utils.Constants.KEY_COLLECTION_CHAT
import com.example.chatapp.utils.Constants.KEY_COLLECTION_CHATROOM
import com.example.chatapp.utils.Constants.KEY_COLLECTION_USERS
import com.example.chatapp.utils.Constants.KEY_EMAIL
import com.example.chatapp.utils.Constants.KEY_FCM_TOKEN
import com.example.chatapp.utils.Constants.KEY_LAST_SEEN_TIMESTAMP
import com.example.chatapp.utils.Constants.KEY_LATEST_MESSAGE
import com.example.chatapp.utils.Constants.KEY_MESSAGE
import com.example.chatapp.utils.Constants.KEY_NAME
import com.example.chatapp.utils.Constants.KEY_PROFILE_IMAGES_URL
import com.example.chatapp.utils.Constants.KEY_PROFILE_IMAGE_URL
import com.example.chatapp.utils.Constants.KEY_RECEIVER_UID
import com.example.chatapp.utils.Constants.KEY_SENDER_UID
import com.example.chatapp.utils.Constants.KEY_TIMESTAMP
import com.example.chatapp.utils.Constants.KEY_UID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DbRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val messaging: FirebaseMessaging
) : DbRepository {

    override suspend fun addUserToDatabase() {
        val currentUser = auth.currentUser
        val filename = auth.currentUser?.uid
        val storageRef = storage.getReference("${Constants.KEY_PROFILE_IMAGES}$filename")
        val isUserAlreadyInDatabase =
            db.collection(KEY_COLLECTION_USERS).document("${currentUser?.uid}").get().await()
                .exists()
        if (!isUserAlreadyInDatabase) {
            val user = hashMapOf(
                KEY_NAME to "${currentUser?.displayName}",
                KEY_EMAIL to "${currentUser?.email}",
                KEY_UID to "${currentUser?.uid}",
                KEY_PROFILE_IMAGE_URL to "${storageRef.downloadUrl.await()}"
            )
            db.collection(KEY_COLLECTION_USERS)
                .document("${currentUser?.uid}")
                .set(user)
        }

    }

    override suspend fun getUsersFromDatabase(): Resource<List<User>> {
        return try {
            val usersList = ArrayList<User>()
            val task = db.collection(KEY_COLLECTION_USERS)
                .whereNotEqualTo(KEY_UID, "${auth.currentUser?.uid}").get().await()
            for (document in task) {
                val user = User(
                    document.data[KEY_NAME].toString(),
                    document.data[KEY_EMAIL].toString(),
                    document.data[KEY_UID].toString(),
                    document.data[KEY_PROFILE_IMAGE_URL].toString()
                )
                usersList.add(user)
            }
            Resource.Success(usersList)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun addMessageToDatabase(
        receiverUid: String,
        message: String,
        dateTime: Long,
        chatRoomId: String
    ) {
        val messageInfo = hashMapOf(
            KEY_SENDER_UID to "${auth.currentUser?.uid}",
            KEY_RECEIVER_UID to receiverUid,
            KEY_MESSAGE to message,
            KEY_TIMESTAMP to dateTime
        )
        val isChatRoomInfoAlreadyExists =
            db.collection(KEY_COLLECTION_CHAT).document(chatRoomId).get().await().exists()
        if (!isChatRoomInfoAlreadyExists) {
            setChatroomInfo(receiverUid, message, dateTime, chatRoomId)
        } else {
            updateChatroomInfo(message, dateTime, chatRoomId)
        }
        db.collection(KEY_COLLECTION_CHAT).document(chatRoomId)
            .collection(KEY_COLLECTION_CHATROOM)
            .document(dateTime.toString()).set(messageInfo)
    }

    private suspend fun updateChatroomInfo(
        message: String,
        dateTime: Long,
        chatRoomId: String
    ) {
        val chatRoomUpdate = mapOf(
            KEY_LATEST_MESSAGE to message,
            KEY_TIMESTAMP to dateTime
        )
        db.collection(KEY_COLLECTION_CHAT).document(chatRoomId).update(chatRoomUpdate)
    }

    private suspend fun setChatroomInfo(
        receiverUid: String,
        message: String,
        dateTime: Long,
        chatRoomId: String
    ) {
        val friendName =
            db.collection(KEY_COLLECTION_USERS).document(receiverUid).get().await().data?.get(
                KEY_NAME
            ).toString()
        val currentUserName =
            db.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid).get()
                .await().data?.get(KEY_NAME).toString()
        val currentUserStorageRef =
            storage.getReference("${Constants.KEY_PROFILE_IMAGES}${auth.currentUser!!.uid}")
        val receiverStorageRef = storage.getReference("${Constants.KEY_PROFILE_IMAGES}$receiverUid")
        val chatroomInfo = hashMapOf(
            KEY_CHATROOM_NAMES to listOf(currentUserName, friendName),
            KEY_CHATROOM_USERS_ID to listOf(auth.currentUser?.uid, receiverUid),
            KEY_PROFILE_IMAGES_URL to listOf(
                currentUserStorageRef.downloadUrl.await(),
                receiverStorageRef.downloadUrl.await()
            ),
            KEY_LATEST_MESSAGE to message,
            KEY_TIMESTAMP to dateTime
        )
        db.collection(KEY_COLLECTION_CHAT).document(chatRoomId).set(chatroomInfo)
    }

    override suspend fun setUserAvailability(isUserAvailable: Boolean, lastSeenTimeStamp: Long) {
        val availabilityInfo = mapOf(
            KEY_AVAILABILITY to isUserAvailable,
            KEY_LAST_SEEN_TIMESTAMP to lastSeenTimeStamp
        )
        db.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid)
            .update(availabilityInfo)
    }

    override suspend fun updateToken() {
        // FirebaseMessaging.getInstance().subscribeToTopic("/topics/myTopic")
        messaging.token.addOnCompleteListener {
            db.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid).update(
                KEY_FCM_TOKEN, it.result
            )
        }
    }

    override suspend fun deleteToken() {
        db.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid)
            .update(KEY_FCM_TOKEN, FieldValue.delete())
    }

    override suspend fun getReceiverToken(receiverUid: String): String {
        val task = db.collection(KEY_COLLECTION_USERS).document(receiverUid).get().await()
        return task[KEY_FCM_TOKEN].toString()
    }
}