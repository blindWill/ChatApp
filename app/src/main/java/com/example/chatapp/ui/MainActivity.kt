package com.example.chatapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.chatapp.R
import com.example.chatapp.viewmodels.MainScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainScreenViewModel by viewModels()
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
    }

    override fun onPause() {
        super.onPause()
        if (navController.currentDestination?.id != R.id.signInFragment && navController.currentDestination?.id != R.id.signUpFragment){
            val currentTimeInMillis = System.currentTimeMillis()
            viewModel.setUserAvailability(false, currentTimeInMillis)
        }
    }

    override fun onResume() {
        super.onResume()
        if (navController.currentDestination?.id != R.id.signInFragment && navController.currentDestination?.id != R.id.signUpFragment){
            viewModel.setUserAvailability(true, 0)
        }
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.mainScreenFragment){
            finish()
        }else{
            super.onBackPressed()
        }
    }
}