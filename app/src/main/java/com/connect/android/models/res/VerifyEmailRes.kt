package com.connect.android.models.res

data class VerifyEmailRes(
    val data: VerifyEmailData
)

data class VerifyEmailData(
    val id: Int,
    val email: String,
    val code: Int,
    val isVerified: Boolean
)