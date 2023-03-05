package com.example.chatapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.data.Resource
import com.example.chatapp.databinding.FragmentMainScreenBinding
import com.example.chatapp.ui.adapters.ChatAdapter
import com.example.chatapp.ui.adapters.UsersLatestMessageAdapter
import com.example.chatapp.viewmodels.AuthViewModel
import com.example.chatapp.viewmodels.MainScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainScreenFragment : Fragment() {

    private var _binding: FragmentMainScreenBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    private val mainScreenViewModel: MainScreenViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        getProfileDetails()
        setListeners()
        setObservers()
        mainScreenViewModel.getRecentConversations()

    }

    private fun setListeners() {
        with(binding) {
            ivLogout.setOnClickListener {
                authViewModel.logout()
                findNavController().navigate(R.id.action_mainScreenFragment_to_signInFragment)
            }
            fabAdd.setOnClickListener {
                findNavController().navigate(R.id.action_mainScreenFragment_to_usersFragment)
            }
        }
    }

    private fun setObservers() {
        mainScreenViewModel.getLatestMessagesLiveData.observe(viewLifecycleOwner) {
            //Log.d("TAG", "5 ${it.toString()}")
            when (it) {
                is Resource.Success -> {
                    (binding.rvUsers.adapter as UsersLatestMessageAdapter).differ.submitList(it.result)
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.rvUsers.visibility = View.VISIBLE
                    //setLayoutChangeListener(it.result)
                }
                is Resource.Failure -> {
                    showToast("Getting messages failed: ${it.exception}")
                }
                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvUsers.visibility = View.INVISIBLE
                }
            }


        }
    }

    private fun setupRecycler() {
        // binding.rvChat.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvUsers.adapter = UsersLatestMessageAdapter(requireContext()) {
            val bundle = Bundle().apply {
                putSerializable("receiverName", it.friendsName)
                putSerializable("receiverUid", it.friendsUid)
                putSerializable("receiverProfileImageUrl", it.profileImageUrl)
            }
            findNavController().navigate(R.id.action_mainScreenFragment_to_chatRoomFragment, bundle)
        }
    }

    private fun getProfileDetails() {
        val user = authViewModel.currentUser
        Glide.with(this).load(user?.photoUrl).into(binding.ivProfile)
        binding.tvNickname.text = user!!.displayName.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}