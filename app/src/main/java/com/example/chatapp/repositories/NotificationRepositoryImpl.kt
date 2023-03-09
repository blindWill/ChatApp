package com.example.chatapp.repositories

import com.example.chatapp.api.NotificationAPI
import com.example.chatapp.data.PushNotification
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(private val notificationApi: NotificationAPI): NotificationRepo {

    override suspend fun postNotification(notification: PushNotification){
        notificationApi.postNotification(notification)
    }
}