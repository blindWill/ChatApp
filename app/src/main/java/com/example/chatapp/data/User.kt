package com.example.chatapp.data

data class User(
    var name: String,
    var email: String,
    var uid: String,
    var profileImageUrl: String
    ) : java.io.Serializable {
}