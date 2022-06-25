package com.connect.android.models.res

import com.google.gson.annotations.SerializedName

data class CommentUserRes(
    val data: CommentUserData
)

data class CommentUserData(
    val count: Int,
    val comments: List<Comment>,
    val currentPage: Int,
    val totalPages: Int
)

data class Comment(
    @SerializedName("id")
    val commentId: Long,
    @SerializedName("commentText")
    val commentText: String,
    @SerializedName("createdAt")
    val commentCreatedAt: String,
    @SerializedName("user")
    val commentedBy: SearchUser
)