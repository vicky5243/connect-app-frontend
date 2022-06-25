package com.connect.android.models.res

data class CurrentUserRes(
    val data: CurrentUserResData
)

data class CurrentUserResData(
    val user: User
)