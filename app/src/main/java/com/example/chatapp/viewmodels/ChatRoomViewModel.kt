package com.example.chatapp.viewmodels

import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.Message
import com.example.chatapp.data.Resource
import com.example.chatapp.repositories.DbRepository
import com.example.chatapp.utils.Constants
import com.example.chatapp.utils.Constants.KEY_COLLECTION_CHAT
import com.example.chatapp.utils.Constants.KEY_COLLECTION_CHATROOM
import com.example.chatapp.utils.Constants.KEY_RECEIVER_UID
import com.example.chatapp.utils.Constants.KEY_TIMESTAMP
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val dbRepo: DbRepository,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ViewModel() {

    val getMessagesLiveData = MutableLiveData<Resource<List<Message>>>()

    fun sendMessage(receiverUid: String, message: String, currentTimeStamp: Long) =
        viewModelScope.launch {
            dbRepo.addMessageToDatabase(
                receiverUid,
                message,
                currentTimeStamp,
                getChatRoomId(receiverUid)
            )
        }

    fun getMessages(receiverUid: String) = viewModelScope.launch {
        try {
            db.collection(KEY_COLLECTION_CHAT).document(getChatRoomId(receiverUid))
                .collection(KEY_COLLECTION_CHATROOM)
                .addSnapshotListener { value, _ ->
                    val messageList = mutableListOf<Message>()
                    for (document in value!!) {
                        val message = Message(
                            id = document.id,
                            senderUid = document.data[Constants.KEY_SENDER_UID].toString(),
                            receiverUid = document.data[KEY_RECEIVER_UID].toString(),
                            dateTime = millisToHoursMinutes(
                                document.data[KEY_TIMESTAMP].toString().toLong()
                            ),
                            message = document.data[Constants.KEY_MESSAGE].toString()
                        )
                        messageList.add(message)

                    }
                    getMessagesLiveData.postValue(Resource.Success(messageList))
                }
        } catch (e: Exception) {
            getMessagesLiveData.postValue(Resource.Failure(e))
        }
    }

    private fun millisToHoursMinutes(millis: Long): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.US)
        return formatter.format(millis).toString()
    }

    private fun getChatRoomId(receiverUid: String): String {
        return if (auth.currentUser?.uid!! > receiverUid) {
            auth.currentUser?.uid + receiverUid
        } else {
            receiverUid + auth.currentUser?.uid
        }
    }
}