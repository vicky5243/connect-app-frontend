package com.connect.android.models.res

data class AuthRes(
    val data: AuthData
)

data class AuthData(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)