package com.example.chatapp.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.data.Resource
import com.example.chatapp.databinding.FragmentSignUpBinding
import com.example.chatapp.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var profileImageBitmap: Bitmap

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
        with(binding) {
            tvSignIn.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
            }
            btSignUp.setOnClickListener {
                if (isValidSignUpDetails()){
                    signUp(
                        etInputNickName.text.toString(),
                        etInputEmail.text.toString(),
                        etInputPassword.text.toString(),
                        profileImageBitmap
                    )
                }

            }
            ivProfile.setOnClickListener {
                chooseImage()
            }

        }

    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        launchSomeActivity.launch(intent)
    }

    private var launchSomeActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode
            == Activity.RESULT_OK
        ) {
            val data = result.data

            if (data != null
                && data.data != null
            ) {
                binding.tvAddImage.visibility = View.INVISIBLE
                val selectedImageUri: Uri? = data.data
                profileImageBitmap = selectedImageUri?.let { getCapturedImage(it) }!!
                binding.ivProfile.setImageBitmap(
                    profileImageBitmap
                )
            }
        }
    }

    private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                activity?.applicationContext?.contentResolver,
                selectedPhotoUri
            )
            else -> {
                val source = ImageDecoder.createSource(
                    activity?.applicationContext!!.contentResolver,
                    selectedPhotoUri
                )
                ImageDecoder.decodeBitmap(source)
            }
        }
        return bitmap
    }

    private fun signUp(name: String, email: String, password: String, profileImageBitmap: Bitmap) {

        viewModel.signupUser(name, email, password, profileImageBitmap)
    }

    private fun isValidSignUpDetails(): Boolean {
        return if (binding.tvAddImage.isVisible){
            showToast("Input Profile Image")
            false
        }else if (binding.etInputNickName.text.trim().isEmpty()) {
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

    private fun setLoadingObserver() {
        lifecycleScope.launchWhenStarted {
            viewModel.signUpFlow.collect {
                when (it) {
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