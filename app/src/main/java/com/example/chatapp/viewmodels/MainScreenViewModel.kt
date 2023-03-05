package com.example.chatapp.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.UserLatestMessage
import com.example.chatapp.data.Resource
import com.example.chatapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ViewModel() {

    val getLatestMessagesLiveData = MutableLiveData<Resource<List<UserLatestMessage>>>()

    fun getRecentConversations() = viewModelScope.launch {
        try {
            db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereArrayContains(Constants.KEY_CHATROOM_USERS_ID, auth.currentUser!!.uid)
                .addSnapshotListener { value, _ ->
                    val userLatestMessageList = mutableListOf<UserLatestMessage>()
                    for (document in value!!) {
                        val userLatestMessage = UserLatestMessage(
                            latestMessage = document[Constants.KEY_LATEST_MESSAGE].toString(),
                            friendsUid = getFriendsId(document),
                            friendsName = getFriendsName(document),
                            timestamp = document[Constants.KEY_TIMESTAMP].toString().toLong(),
                            profileImageUrl = getProfileImageUrl(document)
                        )
                        userLatestMessageList.add(userLatestMessage)
                    }
                    userLatestMessageList.sortByDescending { it.timestamp }
                    getLatestMessagesLiveData.postValue(Resource.Success(userLatestMessageList))
                }
        } catch (e: java.lang.Exception) {
            getLatestMessagesLiveData.postValue(Resource.Failure(e))
        }
    }

    private fun getProfileImageUrl(document: QueryDocumentSnapshot): String{
        val usersProfileImagesUrl: List<String> = document[Constants.KEY_PROFILE_IMAGES_URL] as List<String>
        return if (usersProfileImagesUrl[0] == auth.currentUser?.photoUrl.toString()){
            usersProfileImagesUrl[1]
        }else{
            usersProfileImagesUrl[0]
        }
    }

    private fun getFriendsId(document: QueryDocumentSnapshot): String {
        val usersId: List<String> = document[Constants.KEY_CHATROOM_USERS_ID] as List<String>

        return if (usersId[0] == auth.currentUser?.uid) {
            usersId[1]
        } else {
            usersId[0]
        }

    }

    private fun getFriendsName(document: QueryDocumentSnapshot): String {
        val usersNames: List<String> = document[Constants.KEY_CHATROOM_NAMES] as List<String>

        return if (usersNames[0] == auth.currentUser?.displayName) {
            usersNames[1]
        } else {
            usersNames[0]
        }

    }

}