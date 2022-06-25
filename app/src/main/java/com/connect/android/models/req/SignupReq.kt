package com.connect.android.models.req

data class SignupReq(
    private val username: String,
    private val email: String,
    private val password: String,
    private val code: Int,
    private val id: Int
)