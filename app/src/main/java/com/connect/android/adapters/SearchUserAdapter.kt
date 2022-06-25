package com.connect.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.connect.android.databinding.SearchUserItemLayoutBinding
import com.connect.android.models.res.SearchUser

class SearchUserAdapter(private val clickListener: SearchUserListener) :
    PagingDataAdapter<SearchUser, SearchUserAdapter.SearchUserViewHolder>(SearchUserDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        return SearchUserViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, clickListener)
        }
    }

    class SearchUserViewHolder constructor(private val binding: SearchUserItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchUser, clickListener: SearchUserListener) {
            binding.searchUser = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): SearchUserViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SearchUserItemLayoutBinding.inflate(layoutInflater, parent, false)
                return SearchUserViewHolder(binding)
            }
        }
    }
}

class SearchUserDiffUtil : DiffUtil.ItemCallback<SearchUser>() {
    override fun areItemsTheSame(oldItem: SearchUser, newItem: SearchUser): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: SearchUser, newItem: SearchUser): Boolean {
        return oldItem == newItem
    }
}

class SearchUserListener(val clickListener: (searchUserId: Long) -> Unit) {
    fun onClick(searchUser: SearchUser) = clickListener(searchUser.userId)
}