package com.example.chatapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.data.User
import com.example.chatapp.data.UserLatestMessage
import com.example.chatapp.databinding.UserLatestMessageItemBinding

class UsersLatestMessageAdapter(val onClickListener: (item: UserLatestMessage) -> Unit) :
    RecyclerView.Adapter<UsersLatestMessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            UserLatestMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsersLatestMessageAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ViewHolder(private val binding: UserLatestMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserLatestMessage) {
            with(binding) {
                tvName.text = item.friendsName
                tvLatestMessage.text = item.latestMessage
                root.setOnClickListener {
                    onClickListener(item)
                }
            }

        }

    }

    private val differCallback = object : DiffUtil.ItemCallback<UserLatestMessage>() {
        override fun areItemsTheSame(oldItem: UserLatestMessage, newItem: UserLatestMessage): Boolean {
            return oldItem.friendsUid == newItem.friendsUid
        }

        override fun areContentsTheSame(oldItem: UserLatestMessage, newItem: UserLatestMessage): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}