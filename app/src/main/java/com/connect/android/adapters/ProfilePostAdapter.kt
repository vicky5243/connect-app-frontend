package com.connect.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.connect.android.models.res.NewsfeedPost
import com.connect.android.databinding.ProfilePostItemLayoutBinding

class ProfilePostAdapter(private val onItemClickListener: OnItemClickListener) :
    PagingDataAdapter<NewsfeedPost, ProfilePostAdapter.ProfilePostViewHolder>(DataDifferentiator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ProfilePostItemLayoutBinding.inflate(layoutInflater, parent, false)
        return ProfilePostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class ProfilePostViewHolder(private val binding: ProfilePostItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            /**
             * On post image clicked
             */
            binding.profileItemPostRl.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClicked(position)
                }
            }
        }

        fun bind(item: NewsfeedPost) {
            binding.newsfeedPosts = item
            binding.executePendingBindings()
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(itemPosition: Int)
    }

    object DataDifferentiator : DiffUtil.ItemCallback<NewsfeedPost>() {
        override fun areItemsTheSame(oldItem: NewsfeedPost, newItem: NewsfeedPost): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: NewsfeedPost, newItem: NewsfeedPost): Boolean {
            return oldItem == newItem
        }
    }
}