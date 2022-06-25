package com.connect.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.connect.android.databinding.LikedByItemLayoutBinding
import com.connect.android.models.res.Like
import com.connect.android.models.res.SearchUser

class LikeAdapter(private val clickListener: LikedByListener) :
    PagingDataAdapter<Like, LikeAdapter.LikeViewHolder>(LikeDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        return LikeViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, clickListener)
        }
    }

    class LikeViewHolder constructor(private val binding: LikedByItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Like, clickListener: LikedByListener) {
            binding.like = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): LikeViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LikedByItemLayoutBinding.inflate(layoutInflater, parent, false)
                return LikeViewHolder(binding)
            }
        }
    }
}

class LikeDiffUtil : DiffUtil.ItemCallback<Like>() {
    override fun areItemsTheSame(oldItem: Like, newItem: Like): Boolean {
        return oldItem.likeId == newItem.likeId
    }

    override fun areContentsTheSame(oldItem: Like, newItem: Like): Boolean {
        return oldItem == newItem
    }
}

class LikedByListener(val clickListener: (likedById: Long) -> Unit) {
    fun onClick(searchUser: SearchUser) = clickListener(searchUser.userId)
}