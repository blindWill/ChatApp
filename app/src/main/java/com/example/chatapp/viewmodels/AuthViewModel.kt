package com.example.chatapp.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.Resource
import com.example.chatapp.repositories.AuthRepository
import com.example.chatapp.repositories.DbRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val dbRepo: DbRepository
) : ViewModel() {

    private val _signInFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signInFlow: StateFlow<Resource<FirebaseUser>?> = _signInFlow

    private val _signUpFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signUpFlow: StateFlow<Resource<FirebaseUser>?> = _signUpFlow

    val currentUser: FirebaseUser?
        get() = authRepo.currentUser

    init {
        if (authRepo.currentUser != null) {//authRepo.currentUser
            _signInFlow.value = Resource.Success(authRepo.currentUser!!)
        }
    }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        _signInFlow.value = Resource.Loading
        val result = authRepo.signIn(email, password)
        _signInFlow.value = result
    }

    fun signupUser(name: String, email: String, password: String, profileImage: Bitmap) = viewModelScope.launch {
        _signUpFlow.value = Resource.Loading
        val result = authRepo.signUp(name, email, password, profileImage)
        _signUpFlow.value = result
    }

    fun sendEmailVerification() = viewModelScope.launch {
        authRepo.sendEmailVerification()
    }

    fun logout() {
        authRepo.logout()
        _signInFlow.value = null
        _signUpFlow.value = null
    }

    fun addUserToDb() = viewModelScope.launch(Dispatchers.IO) {
        dbRepo.addUserToDatabase()
    }

}