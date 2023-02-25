package com.example.chatapp.data

data class Message(var senderUid: String, var receiverUid: String, var dateTime: String, var message: String) {
}