package com.example.chatapp.data

data class UserLatestMessage(
    var friendsName: String,
    var friendsUid: String,
    var latestMessage: String,
    var timestamp: Long
) : java.io.Serializable {
}