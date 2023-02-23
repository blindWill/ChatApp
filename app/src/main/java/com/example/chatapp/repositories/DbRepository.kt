package com.example.chatapp.repositories

interface DbRepository  {

    suspend fun addUserToDatabase()
}