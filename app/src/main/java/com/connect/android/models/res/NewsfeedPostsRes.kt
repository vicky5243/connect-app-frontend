package com.connect.android.models.res

import com.google.gson.annotations.SerializedName

data class NewsfeedPostsRes(
    val data: NewsfeedPostsData
)

data class NewsfeedPostsData(
    val count: Int,
    val posts: List<NewsfeedPost>,
    val currentPage: Int,
    val totalPages: Int
)

data class NewsfeedPost(
    @SerializedName("id")
    val postId: Long,
    @SerializedName("title")
    val postTitle: String? = null,
    @SerializedName("description")
    val postDescription: String? = null,
    @SerializedName("imageUrl")
    val postImageUrl: String,
    @SerializedName("likes")
    var numLikes: Int,
    @SerializedName("comments")
    val numComments: Int,
    @SerializedName("hasLiked")
    var hasLiked: Boolean,
    @SerializedName("createdAt")
    val postCreatedAt: String,
    @SerializedName("user")
    val postCreatedByUser: PostCreatedBy
)

data class PostCreatedBy(
    @SerializedName("id")
    val postCreatedById: Long,
    @SerializedName("username")
    val postCreatedByUsername: String,
    @SerializedName("profilePhotoUrl")
    val postCreatedByProfilePhotoUrl: String,
)