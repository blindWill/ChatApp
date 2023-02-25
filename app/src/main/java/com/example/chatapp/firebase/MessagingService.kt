package com.example.chatapp.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService: FirebaseMessagingService() {

    @Override
    override fun onNewToken(token: String) {
        super.onNewToken(token)
       // Log.d("FCM", "Token: $token")
    }

    @Override
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        //Log.d("FCM", "RemoteMessage: ${message.notifica/
    }
}