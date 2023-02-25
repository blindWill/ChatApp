package com.example.chatapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.data.User
import com.example.chatapp.databinding.FragmentChatRoomBinding

class ChatRoomFragment : Fragment() {

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() =_binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()

        val user = arguments?.getSerializable("user") as User
        binding.tvReceiverName.text = user.name
    }

    private fun setListeners(){
        binding.ivBack.setOnClickListener {
            findNavController().navigate(R.id.action_chatRoomFragment_to_mainScreenFragment)
        }
        binding.ivSend.setOnClickListener {

        }
    }
}