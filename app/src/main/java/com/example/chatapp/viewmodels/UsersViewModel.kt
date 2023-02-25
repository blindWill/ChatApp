package com.example.chatapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.Resource
import com.example.chatapp.data.User
import com.example.chatapp.repositories.DbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val dbRepo: DbRepository
) : ViewModel() {

    private val _getUsersFlow = MutableStateFlow<Resource<List<User>>?>(null)
    val getUsersFlow: StateFlow<Resource<List<User>>?> = _getUsersFlow

    fun getUsers() = viewModelScope.launch {
        _getUsersFlow.value = Resource.Loading
        val result = dbRepo.getUsersFromDatabase()
        _getUsersFlow.value = result
    }

}