package com.connect.android.database

import androidx.room.*
import com.connect.android.models.res.User

@Dao
interface ConnectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user_table")
    suspend fun getUser(): User?

    @Query("SELECT accessToken FROM user_table")
    suspend fun getToken(): String?

    @Query("SELECT refreshToken FROM user_table")
    suspend fun getRefreshToken(): String?

    @Query("SELECT profilePhotoUrl FROM user_table")
    suspend fun getProfilePhotoUrl(): String?

    @Query("DELETE FROM user_table")
    suspend fun clear()

    /**
     * Updating only token and refreshToken
     * By user id
     */
    @Query("UPDATE user_table SET accessToken = :accessToken, refreshToken = :refreshToken WHERE id =:uid")
    suspend fun update(accessToken: String, refreshToken: String, uid: Long)
}