package com.example.chatapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.data.Message
import com.example.chatapp.data.User
import com.example.chatapp.databinding.ReceivedMessageItemBinding
import com.example.chatapp.databinding.SentMessageItemBinding
import com.example.chatapp.databinding.UserItemBinding
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class ChatAdapter ():
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        const val SENT_MESSAGE = 1
        const val RECEIVED_MESSAGE = 2
    }

    inner class SentMessageViewHolder(private val binding: SentMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) {
            with(binding) {
                tvMessage.text = item.message
                tvDateTime.text= item.dateTime

            }

        }

    }

    inner class ReceivedMessageViewHolder(private val binding: ReceivedMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) {
            with(binding) {
                tvMessage.text = item.message
                tvDateTime.text= item.dateTime
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SENT_MESSAGE) {
            return SentMessageViewHolder(
                SentMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
        return ReceivedMessageViewHolder(
            ReceivedMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (differ.currentList[position].senderUid == auth.currentUser?.uid){
            (holder as SentMessageViewHolder).bind(differ.currentList[position])
        } else {
            (holder as ReceivedMessageViewHolder).bind(differ.currentList[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (differ.currentList[position].senderUid == auth.currentUser?.uid){
            SENT_MESSAGE
        } else {
            RECEIVED_MESSAGE
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    private val differCallback = object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

}