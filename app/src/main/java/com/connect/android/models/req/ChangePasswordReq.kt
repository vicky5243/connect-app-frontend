package com.connect.android.models.req

data class ChangePasswordReq(
    val oldPassword: String,
    val newPassword: String,
    val confirmNewPassword: String
)