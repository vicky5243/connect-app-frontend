package com.connect.android.models.res

import com.google.gson.annotations.SerializedName

data class LikeUserRes(
    val data: LikeUserData
)

data class LikeUserData(
    val count: Int,
    val likes: List<Like>,
    val currentPage: Int,
    val totalPages: Int
)

data class Like(
    @SerializedName("id")
    val likeId: Long,
    @SerializedName("createdAt")
    val likeCreatedAt: String,
    @SerializedName("user")
    val likedBy: SearchUser
)