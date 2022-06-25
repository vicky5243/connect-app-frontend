package com.connect.android.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.connect.android.database.ConnectDao
import com.connect.android.models.req.ChangePasswordReq
import com.connect.android.models.req.CommentReq
import com.connect.android.network.Api
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val api: Api,
    private val connectDao: ConnectDao
) {

    //######################## NETWORK REQUESTS ###########################
    /**
     * POST: Create post
     */
    suspend fun createPost(
        token: String,
        title: RequestBody,
        desc: RequestBody,
        file: MultipartBody.Part
    ) =
        api.createPost("Bearer $token", title, desc, file)

    /**
     * POST: Like or Unlike a Post
     */
    suspend fun likeOrUnlikePost(token: String, pid: Long) =
        api.likeOrUnlikeAPost("Bearer $token", pid)

    /**
     * GET: Request for get a user by id
     */
    suspend fun getUserById(token: String, uid: Long) =
        api.getUserById("Bearer $token", uid)

    /**
     * PATCH: Request for change password
     */
    suspend fun changePassword(token: String, changePasswordReq: ChangePasswordReq) =
        api.changePassword("Bearer $token", changePasswordReq)

    /**
     * PATCH: Request for edit profile
     */
    suspend fun editProfile(
        token: String,
        fullname: RequestBody?,
        relationshipStatus: RequestBody?,
        username: RequestBody?,
        file: MultipartBody.Part?
    ) =
        api.editProfile("Bearer $token", fullname, relationshipStatus, username, file)

    /**
     * GET: Request for search users
     */
    fun searchUser(authRepository: AuthRepository, query: String) =
        Pager(
            config = PagingConfig(
                pageSize = 15,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SearchUsersDataSource(
                    api,
                    query,
                    authRepository
                )
            }
        ).liveData

    /**
     * GET: Fetching user newsfeed
     */
    fun getNewsFeedPosts(authRepository: AuthRepository, uid: Long?) =
        Pager(
            config = PagingConfig(
                pageSize = 15,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                NewsfeedDataSource(
                    api,
                    authRepository,
                    uid
                )
            }
        ).liveData

    /**
     * GET: Request all likes of a post
     */
    fun getLikesOfAPost(authRepository: AuthRepository, pid: Long) =
        Pager(
            config = PagingConfig(
                pageSize = 15,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                LikesUserDataSource(
                    api,
                    pid,
                    authRepository
                )
            }
        ).liveData

    /**
     * GET: Request all comments of a post
     */
    fun getCommentsOfAPost(authRepository: AuthRepository, pid: Long) =
        Pager(
            config = PagingConfig(
                pageSize = 15,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                CommentsUserDataSource(
                    api,
                    pid,
                    authRepository
                )
            }
        ).liveData

    /**
     * POST: Follow or Unfollow an User
     */
    suspend fun followAnUser(token: String, uid: Long) =
        api.followAnUser("Bearer $token", uid)

    /**
     * POST: Comment on a post
     */
    suspend fun commentOnAPost(token: String, commentReq: CommentReq, pid: Long) =
        api.commentOnAPost("Bearer $token", commentReq, pid)

    //#################### LOCAL DATABASE REQUESTS ########################

    /**
     * Clear the database before logout the user because
     */
    suspend fun logoutUser() = connectDao.clear()

    /**
     * GET: Get user profile photo url
     */
    suspend fun getOnlineUserProfilePhotoUrl() = connectDao.getProfilePhotoUrl()

}