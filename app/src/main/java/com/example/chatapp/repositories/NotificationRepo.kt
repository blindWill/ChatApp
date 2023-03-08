package com.example.chatapp.repositories

import com.example.chatapp.data.PushNotification
import javax.inject.Inject

class NotificationRepo @Inject constructor(private val notificationApi: NotificationAPI) {

    suspend fun postNotification(notification: PushNotification) = notificationApi.postNotification(notification)
}