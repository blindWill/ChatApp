package com.example.chatapp.ui

import android.os.Bundle
import com.example.chatapp.data.Resource
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
import com.example.chatapp.databinding.FragmentSignUpBinding
import com.example.chatapp.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setLoadingObserver()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun setListeners() {
        with(binding){
            tvSignIn.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
            }
            btSignUp.setOnClickListener {
                signUp(etInputNickName.text.toString(), etInputEmail.text.toString(), etInputPassword.text.toString())
            }
        }

    }

    private fun signUp(name: String, email: String, password: String) {
        if (!isValidSignUpDetails()) {
            return
        }
        viewModel.signupUser(name, email, password)
    }
    private fun isValidSignUpDetails(): Boolean {
        return if (binding.etInputNickName.text.trim().isEmpty()) {
            showToast("Enter Nickname")
            false
        } else if (binding.etInputEmail.text.trim().isEmpty()) {
            showToast("Enter Email")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etInputEmail.text).matches()) {
            showToast("Enter valid Email")
            false
        } else if (binding.etInputPassword.text.trim().isEmpty()) {
            showToast("Enter password")
            false
        } else if (binding.etInputPassword.text.toString().length < 6) {
            showToast("Password length must be more then 6 symbols")
            false
        } else if (binding.etConfirmPassword.text.trim().isEmpty()) {
            showToast("Confirm password")
            false
        } else if (binding.etInputPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
            showToast("Password & Confirm password must be equal")
            false
        } else {
            true
        }
    }

    private fun setLoadingObserver(){
        lifecycleScope.launchWhenStarted {
            viewModel.signUpFlow.collect{
                when(it){
                    is Resource.Loading -> {
                        binding.btSignUp.visibility = View.GONE
                        binding.pbSignUp.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        viewModel.sendEmailVerification()
                        findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
                        binding.btSignUp.visibility = View.VISIBLE
                        binding.pbSignUp.visibility = View.INVISIBLE
                        showToast("Verification email sent to ${viewModel.currentUser?.email}")
                    }
                    is Resource.Failure -> {
                        binding.btSignUp.visibility = View.VISIBLE
                        binding.pbSignUp.visibility = View.INVISIBLE
                        showToast("Authentication failed: ${it.exception}")
                    }
                    else -> {
                        binding.btSignUp.visibility = View.VISIBLE
                        binding.pbSignUp.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }
}