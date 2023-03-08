package com.example.chatapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.data.Resource
import com.example.chatapp.databinding.FragmentUsersBinding
import com.example.chatapp.ui.adapters.UsersAdapter
import com.example.chatapp.viewmodels.UsersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UsersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setListeners()
        setObservers()
        viewModel.getUsers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigate(R.id.action_usersFragment_to_mainScreenFragment)
        }
    }

    private fun setupRecycler() {
        binding.rvUsers.adapter = UsersAdapter(requireContext()) {
            val bundle = Bundle().apply {
                putSerializable("receiverName", it.name)
                putSerializable("receiverUid", it.uid)
                putSerializable("receiverProfileImageUrl", it.profileImageUrl)
            }
            findNavController().navigate(
                R.id.action_usersFragment_to_chatRoomFragment,
                bundle
            )
        }
    }

    private fun setObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.getUsersFlow.collect {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvUsers.visibility = View.INVISIBLE
                    }
                    is Resource.Success -> {
                        (binding.rvUsers.adapter as UsersAdapter).differ.submitList(it.result)
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.rvUsers.visibility = View.VISIBLE
                    }
                    is Resource.Failure -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.rvUsers.visibility = View.VISIBLE
                        showToast("Getting users failed: ${it.exception}")
                    }
                    else -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.rvUsers.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}