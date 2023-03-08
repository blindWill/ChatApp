package com.example.chatapp.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.example.chatapp.viewmodels.MainScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity: AppCompatActivity() {

    private val viewModel: MainScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
       // FirebaseMessaging.getInstance().subscribeToTopic("/topics/myTopic")
    }

    override fun onPause() {
        super.onPause()
        val currentTimeInMillis = System.currentTimeMillis()
        viewModel.setUserAvailability(false, currentTimeInMillis)
    }

    override fun onResume() {
        super.onResume()
        viewModel.setUserAvailability(true, 0)
    }
}