package com.connect.android.models.req

data class VerifyEmailReq(
    val email: String,
    val code: String
)