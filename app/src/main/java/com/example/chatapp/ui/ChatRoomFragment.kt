package com.example.chatapp.ui

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.HandlerCompat.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.data.Message
import com.example.chatapp.data.Resource
import com.example.chatapp.data.User
import com.example.chatapp.databinding.FragmentChatRoomBinding
import com.example.chatapp.ui.adapters.ChatAdapter
import com.example.chatapp.viewmodels.ChatRoomViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatRoomFragment : Fragment() {

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatRoomViewModel by viewModels()

    private lateinit var userReceiver: User

    //    @Inject
//    lateinit var chatAdapter: ChatAdapter
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
        setupRecycler()
        getUserReceiver()
        setListeners()
        setObservers()
        getMessages()
    }

    private fun getUserReceiver() {
        userReceiver = arguments?.getSerializable("user") as User
        binding.tvReceiverName.text = userReceiver.name
    }


    private fun getMessages() {
        viewModel.getMessages(userReceiver.uid)
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
                        userReceiver.uid,
                        etInputMessage.text.toString(),
                        currentTimeInMillis
                    )
                    etInputMessage.text.clear()
                }

            }

        }

    }

    private fun setLayoutChangeListener(messageList: List<Message>){
        binding.rvChat.addOnLayoutChangeListener(object: View.OnLayoutChangeListener{
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                if (messageList.isNotEmpty()){
                    binding.rvChat.postDelayed({
                        binding.rvChat.smoothScrollToPosition(
                            messageList.size - 1
                        )
                    }, 100)
                }
            }
        })
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
    }

    private fun setupRecycler() {
        // binding.rvChat.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvChat.adapter = ChatAdapter(FirebaseAuth.getInstance())
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}