package com.connect.android.models.res

import com.google.gson.annotations.SerializedName

data class CommentRes(
    val data: CommentResData
)

data class CommentResData(
    val comment: CommentData
)

data class CommentData(
    @SerializedName("id")
    val commentId: Long,
    val commentText: String,
    @SerializedName("createdAt")
    val commentCreatedAt: String
)