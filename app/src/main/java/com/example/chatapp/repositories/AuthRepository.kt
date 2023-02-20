package com.example.chatapp.repositories

import com.example.chatapp.data.Resource
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?

    suspend fun signIn(email: String, password: String): Resource<FirebaseUser>

    suspend fun signUp(name: String, email: String, password: String): Resource<FirebaseUser>

    suspend fun sendEmailVerification()

    fun logout()
}