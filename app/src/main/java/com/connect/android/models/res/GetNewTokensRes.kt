package com.connect.android.models.res

data class GetNewTokensRes(
    val data: GetNewTokensData
)

data class GetNewTokensData(
    val id: Long,
    val accessToken: String,
    val refreshToken: String
)