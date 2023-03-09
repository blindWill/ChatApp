package com.example.chatapp.repositories

import com.example.chatapp.api.NotificationAPI
import com.example.chatapp.data.PushNotification
import javax.inject.Inject

interface NotificationRepo {
    suspend fun postNotification(notification: PushNotification)
}