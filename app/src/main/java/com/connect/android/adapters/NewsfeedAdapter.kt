package com.connect.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.connect.android.databinding.NewsfeedItemLayoutBinding
import com.connect.android.models.res.NewsfeedPost
import timber.log.Timber

class NewsfeedAdapter(private val onItemClickListener: OnItemClickListener) :
    PagingDataAdapter<NewsfeedPost, NewsfeedAdapter.NewsfeedViewHolder>(DataDifferentiator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsfeedViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = NewsfeedItemLayoutBinding.inflate(layoutInflater, parent, false)
        return NewsfeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsfeedViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class NewsfeedViewHolder(private val binding: NewsfeedItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            /**
             * Post created by user profile photo clicked
             */
            binding.postUserProfilePicCiv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        onItemClickListener.onItemUserProfilePhotoClicked(
                            item.postCreatedByUser.postCreatedById
                        )
                    }
                }
            }

            /**
             * Post created by user username clicked
             */
            binding.postUserUsernameTv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        onItemClickListener.onItemUserUsernameClicked(
                            item.postCreatedByUser.postCreatedById
                        )
                    }
                }
            }

            /**
             * Like or Unlike a post
             */
            binding.postLikeUnlikeIv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        item.hasLiked = !item.hasLiked
                        if (item.hasLiked) {
                            item.numLikes += 1
                        } else {
                            item.numLikes -= 1
                        }
                        notifyItemChanged(position)
                        onItemClickListener.onItemLikeOrUnlikeClicked(
                            item.postId
                        )
                    }
                }
            }

            /**
             * Post's comment image view clicked
             */
            binding.postCommentIv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        onItemClickListener.onItemAllCommentsClicked(
                            item.postId,
                            item.numComments.toLong()
                        )
                    }
                }
            }

            /**
             * Post's num likes clicked
             */
            binding.postNumLikesTv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        onItemClickListener.onItemAllLikesClicked(
                            item.postId,
                            item.numLikes.toLong()
                        )
                    }
                }
            }

            /**
             * Post's num comments clicked
             */
            binding.postAllCommentsTv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        onItemClickListener.onItemAllCommentsClicked(
                            item.postId,
                            item.numComments.toLong()
                        )
                    }
                }
            }
        }

        fun bind(item: NewsfeedPost) {
            Timber.d("bind called!")
            binding.newsfeedPosts = item
            binding.executePendingBindings()
        }
    }

    interface OnItemClickListener {
        fun onItemUserProfilePhotoClicked(uid: Long)
        fun onItemUserUsernameClicked(uid: Long)
        fun onItemAllLikesClicked(pid: Long, numLikes: Long)
        fun onItemLikeOrUnlikeClicked(pid: Long)
        fun onItemAllCommentsClicked(pid: Long, numComments: Long)
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