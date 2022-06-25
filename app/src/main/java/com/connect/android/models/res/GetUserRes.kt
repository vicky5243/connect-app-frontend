package com.connect.android.models.res

import com.google.gson.annotations.SerializedName
import java.util.*

data class GetUserRes(
    val data: GetUserResData
)

data class GetUserResData(
    val user: GetUser
)

data class GetUser(
    val id: Long,
    val username: String,
    val fullname: String? = null,
    val relationshipStatus: String? = null,
    val profilePhotoUrl: String,
    val createdAt: Date,
    val isFollower: Boolean,
    val isFollowee: Boolean,
    val numFollowers: Long,
    val numFollowees: Long,
    val numPosts: Long,
    val posts: List<UserPosts>
)

data class UserPosts(
    @SerializedName("id")
    val postId: Long,
    @SerializedName("title")
    val postTitle: String? = null,
    @SerializedName("description")
    val postDescription: String? = null,
    @SerializedName("imageUrl")
    val postImageUrl: String,
    @SerializedName("createdAt")
    val postCreatedAt: Date,
)