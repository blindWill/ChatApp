package com.example.chatapp.repositories

import androidx.lifecycle.MutableLiveData
import com.example.chatapp.data.Message
import com.example.chatapp.data.Resource
import com.example.chatapp.data.User

interface DbRepository  {

    suspend fun addUserToDatabase()

    suspend fun getUsersFromDatabase(): Resource<List<User>>

    suspend fun addMessageToDatabase(receiverUid: String, message: String, dateTime: Long, chatRoomId: String)

    suspend fun setUserAvailability(isUserAvailable: Boolean, lastSeenTimeStamp: Long)

    suspend fun updateToken()

    suspend fun deleteToken()

    suspend fun getReceiverToken(receiverUid: String): String

}