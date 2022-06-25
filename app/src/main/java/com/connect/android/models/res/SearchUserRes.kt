package com.connect.android.models.res

import com.google.gson.annotations.SerializedName

data class SearchUserRes(
    val data: SearchUserData
)

data class SearchUserData(
    val count: Int,
    val users: List<SearchUser>,
    val currentPage: Int,
    val totalPages: Int
)

data class SearchUser(
    @SerializedName("id")
    val userId: Long,
    val username: String,
    val fullname: String? = null,
    val profilePhotoUrl: String
)