package com.connect.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.connect.android.databinding.CommentedByItemLayoutBinding
import com.connect.android.models.res.Comment

class CommentAdapter(private val onItemClickListener: OnCommentItemClickListener) :
    PagingDataAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CommentedByItemLayoutBinding.inflate(layoutInflater, parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class CommentViewHolder constructor(private val binding: CommentedByItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            /**
             * Post created by user profile photo clicked
             */
            binding.itemCommentedByCiv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        onItemClickListener.onItemUserProfilePhotoClicked(
                            item.commentedBy.userId
                        )
                    }
                }
            }
        }

        fun bind(item: Comment) {
            binding.comment = item
            binding.executePendingBindings()
        }
    }

    interface OnCommentItemClickListener {
        fun onItemUserProfilePhotoClicked(uid: Long)
    }
}

class CommentDiffUtil : DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.commentId == newItem.commentId
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }
}