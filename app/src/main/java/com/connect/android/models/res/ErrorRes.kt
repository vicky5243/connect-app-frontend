package com.connect.android.models.res

data class ErrorRes(
    val error: Error
)

data class Error(
    val message: String,
    val code: Int
)