package com.example.chatapp.ui

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.data.Resource
import com.example.chatapp.databinding.FragmentSignInBinding
import com.example.chatapp.viewmodels.AuthViewModel
import com.example.chatapp.viewmodels.MainScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() =_binding!!

    private val authViewModel: AuthViewModel by viewModels()

    private val mainScreenViewModel: MainScreenViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLoadingObserver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners(){
        with(binding){
            tvSignUp.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            }
            btSignIn.setOnClickListener {
                signIn(etInputEmail.text.toString(), etInputPassword.text.toString())
            }
        }
    }

    private fun signIn(email: String, password: String) {
        if (!isValidSignInDetails()) {
            return
        }
        authViewModel.loginUser(email, password)
    }

    private fun navigateIfEmailVerified(){
        val user = authViewModel.currentUser
        user?.reload()
        if (user != null && user.isEmailVerified) {
            authViewModel.addUserToDb()
            mainScreenViewModel.setUserAvailability(true, 0)
            findNavController().navigate(R.id.action_signInFragment_to_mainScreenFragment)
        }else{
            showToast("Confirm your email address")
        }
    }

    private fun isValidSignInDetails(): Boolean {
        return if (binding.etInputEmail.text.trim().isEmpty()) {
            showToast("Enter Email")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etInputEmail.text).matches()) {
            showToast("Enter valid Email")
            false
        } else if (binding.etInputPassword.text.trim().isEmpty()) {
            showToast("Enter password")
            false
        } else {
            true
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun setLoadingObserver(){
        lifecycleScope.launchWhenStarted {
            authViewModel.signInFlow.collect{
                when(it){
                    is Resource.Loading ->{
                        binding.btSignIn.visibility = View.GONE
                        binding.pbSignIn.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.btSignIn.visibility = View.VISIBLE
                        binding.pbSignIn.visibility = View.INVISIBLE
                        navigateIfEmailVerified()
                    }
                    is Resource.Failure -> {
                        binding.btSignIn.visibility = View.VISIBLE
                        binding.pbSignIn.visibility = View.INVISIBLE
                        showToast("Authentication failed: ${it.exception}")
                    }
                    else -> {
                        binding.btSignIn.visibility = View.VISIBLE
                        binding.pbSignIn.visibility = View.INVISIBLE
                    }
                }

            }
        }
    }

}