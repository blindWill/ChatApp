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
import com.example.chatapp.data.*
import com.example.chatapp.databinding.FragmentChatRoomBinding
import com.example.chatapp.ui.adapters.ChatAdapter
import com.example.chatapp.viewmodels.ChatRoomViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatRoomFragment : Fragment() {

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatRoomViewModel by viewModels()

    private lateinit var receiverUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        getUserReceiver()
        setListeners()
        setObservers()
        getMessages()
        viewModel.checkReceiverAvailability(receiverUid)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getUserReceiver() {
        val receiverName = arguments?.getString("receiverName")
        receiverUid = arguments?.getString("receiverUid")!!
        val receiverProfileImageUrl = arguments?.getString("receiverProfileImageUrl")
        Glide.with(this).load(receiverProfileImageUrl).into(binding.ivProfile)
        binding.tvReceiverName.text = receiverName
    }


    private fun getMessages() {
        viewModel.getMessages(receiverUid)
    }

    private fun setListeners() {

        with(binding) {
            ivBack.setOnClickListener {
                findNavController().navigate(R.id.action_chatRoomFragment_to_mainScreenFragment)
            }
            ivSend.setOnClickListener {
                if (etInputMessage.text.isNotEmpty()) {
                    val currentTimeInMillis = System.currentTimeMillis()
                    viewModel.sendMessage(
                        receiverUid,
                        etInputMessage.text.toString(),
                        currentTimeInMillis
                    )
                    sendNotificationIfReceiverIsNotOnline()
                    }
                    etInputMessage.text.clear()
                }

            }

        }

    private fun sendNotificationIfReceiverIsNotOnline(){
        if (binding.tvLastSeen.text != resources.getString(R.string.online)){
                viewModel.sendNotification(binding.etInputMessage.text.toString(), receiverUid)
        }
    }

    private fun setLayoutChangeListener(messageList: List<Message>) {
        binding.rvChat.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (messageList.isNotEmpty()) {
                binding.rvChat.postDelayed({
                    binding.rvChat.smoothScrollToPosition(
                        messageList.size - 1
                    )
                }, 100)
            }
        }
    }

    private fun setObservers() {
        viewModel.getMessagesLiveData.observe(viewLifecycleOwner) {
            //Log.d("TAG", "5 ${it.toString()}")
            when (it) {
                is Resource.Success -> {
                    (binding.rvChat.adapter as ChatAdapter).differ.submitList(it.result)
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.rvChat.visibility = View.VISIBLE
                    setLayoutChangeListener(it.result)
                }
                is Resource.Failure -> {
                    showToast("Getting messages failed: ${it.exception}")
                }
                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvChat.visibility = View.INVISIBLE
                }
            }
        }
        viewModel.getReceiverAvailability.observe(viewLifecycleOwner){
            when(it) {
                is Resource.Success -> {
                    if (it.result.isUserAvailable){
                        binding.tvLastSeen.setText(R.string.online)
                    } else {
                        val lastSeen = resources.getString(R.string.last_seen_at).plus(" ${it.result.lastSeenDate}")
                        binding.tvLastSeen.text = lastSeen
                    }
                }
                else -> {}
            }
        }
    }

    private fun setupRecycler() {
        binding.rvChat.adapter = ChatAdapter()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}