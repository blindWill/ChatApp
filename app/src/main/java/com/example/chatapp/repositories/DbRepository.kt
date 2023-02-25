package com.example.chatapp.repositories

import com.example.chatapp.data.Resource
import com.example.chatapp.data.User

interface DbRepository  {

    suspend fun addUserToDatabase()

    suspend fun getUsersFromDatabase(): Resource<List<User>>
}