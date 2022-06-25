package com.connect.android.network

import com.connect.android.models.req.*
import com.connect.android.models.res.*
import com.connect.android.utils.Constants
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface Api {

    /**
     * POST: Send email confirmation code
     */
    @POST(Constants.SEND_EMAIL)
    suspend fun sendEmailConfirmation(
        @Body sentEmailVerificationReq: SentEmailVerificationReq
    ): Response<SuccessRes>

    /**
     * POST: Verify email through confirmation code that,
     * has been sent to email
     */
    @POST(Constants.VERIFY_EMAIL)
    suspend fun verifyEmail(
        @Body verifyEmailReq: VerifyEmailReq
    ): Response<VerifyEmailRes>

    /**
     * POST: Request for creating account
     */
    @POST(Constants.SIGN_UP)
    suspend fun signup(
        @Body signupReq: SignupReq
    ): Response<AuthRes>

    /**
     * POST: Request for log in existing account
     */
    @POST(Constants.SIGN_IN)
    suspend fun signin(
        @Body signinReq: SigninReq
    ): Response<AuthRes>

    /**
     * POST: Request for new accessToken and refreshToken
     */
    @POST(Constants.NEW_TOKENS)
    suspend fun getNewTokens(
        @Body newTokensReq: NewTokensReq
    ): Response<GetNewTokensRes>

    /**
     * DELETE: Logging out the user
     */
    @DELETE(Constants.LOG_OUT)
    suspend fun logoutTheUser(
        @Header("Authorization") token: String,
        @Body logoutReq: LogoutReq
    ): Response<SuccessRes>

    //##############################################################################################

    /**
     * POST: Create post
     */
    @Multipart
    @POST(Constants.CREATE_POST)
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") desc: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<SuccessRes>

    /**
     * GET: Get Newsfeed posts
     */
    @GET(Constants.GET_NEWSFEED_POSTS)
    suspend fun getNewsfeedPosts(
        @Header("Authorization") token: String,
        @Query("page") page: Int
    ): Response<NewsfeedPostsRes>

    /**
     * POST: Like or Unlike a Post
     */
    @POST(Constants.LIKE_OR_UNLIKE_A_POST)
    suspend fun likeOrUnlikeAPost(
        @Header("Authorization") token: String,
        @Path("pid") pid: Long
    ): Response<SuccessBooleanRes>

    /**
     * Comment on a post
     */
    @POST(Constants.COMMENT_ON_A_POST)
    suspend fun commentOnAPost(
        @Header("Authorization") token: String,
        @Body commentReq: CommentReq,
        @Path("pid") pid: Long
    ): Response<CommentRes>

    /**
     * GET: Request for likes users of a post
     */
    @GET(Constants.LIKES_OF_A_POST)
    suspend fun getLikesOfAPost(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("pid") pid: Long,
    ): Response<LikeUserRes>

    /**
     * GET: Request for comments users of a post
     */
    @GET(Constants.COMMENTS_OF_A_POST)
    suspend fun getCommentsOfAPost(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("pid") name: Long,
    ): Response<CommentUserRes>

    //##############################################################################################

    /**
     * GET: Get current user (online user)
     */
    @GET(Constants.CURRENT_USER)
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<CurrentUserRes>

    /**
     * GET: Request for get a user by id
     */
    @GET(Constants.GET_USER_BY_ID)
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Query("uid") uid: Long,
    ): Response<GetUserRes>

    /**
     * GET: Request for search users
     */
    @GET(Constants.SEARCH_USER)
    suspend fun searchUser(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("u") name: String,
    ): Response<SearchUserRes>

    /**
     * PATCH: Request for edit profile
     */
    @Multipart
    @PATCH(Constants.EDIT_PROFILE)
    suspend fun editProfile(
        @Header("Authorization") token: String,
        @Part("fullname") fullname: RequestBody? = null,
        @Part("relationshipStatus") relationshipStatus: RequestBody? = null,
        @Part("username") username: RequestBody? = null,
        @Part file: MultipartBody.Part? = null
    ): Response<SuccessRes>

    /**
     * PATCH: Request for change password
     */
    @PATCH(Constants.CHANGE_PASSWORD)
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body changePasswordReq: ChangePasswordReq
    ): Response<SuccessRes>

    /**
     * POST: Follow or Unfollow an User
     */
    @POST(Constants.FOLLOW_OR_UNFOLLOW)
    suspend fun followAnUser(
        @Header("Authorization") token: String,
        @Path("uid") uid: Long
    ): Response<SuccessRes>

    /**
     * GET: Get posts of an user
     */
    @GET(Constants.GET_NEWSFEED_POSTS)
    suspend fun getPostsOfAnUser(
        @Header("Authorization") token: String,
        @Query("uid") uid: Long,
        @Query("page") page: Int
    ): Response<NewsfeedPostsRes>
}