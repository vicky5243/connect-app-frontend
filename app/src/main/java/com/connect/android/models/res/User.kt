package com.connect.android.models.res

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "user_table")
@Parcelize
data class User(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val username: String? = null,
    val fullname: String? = null,
    val relationshipStatus: String? = null,
    val profilePhotoUrl: String? = null,
    val createdAt: Date? = null,
    var refreshToken: String? = null,
    var accessToken: String? = null
) : Parcelable
