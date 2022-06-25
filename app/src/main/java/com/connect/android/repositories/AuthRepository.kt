package com.connect.android.repositories

import com.connect.android.database.ConnectDao
import com.connect.android.models.req.*
import com.connect.android.models.res.User
import com.connect.android.network.Api
import timber.log.Timber
import javax.inject.Inject

class AuthRepository @Inject constructor(private val api: Api, private val connectDao: ConnectDao) {

    //######################## NETWORK REQUESTS ###########################
    /**
     * POST: Request for new accessToken and refreshToken
     */
    suspend fun getNewTokens(newTokensReq: NewTokensReq): String? {
        val response = api.getNewTokens(newTokensReq)
        if (response.isSuccessful) {
            Timber.d("getNewTokens response is successful")
            response.body()?.let {
                // save to local database
                connectDao.update(it.data.accessToken, it.data.refreshToken, it.data.id)
                return it.data.accessToken
            }
        }
        Timber.d("getNewTokens response is unsuccessful")
        return null
    }

    /**
     * POST: Send email confirmation code
     */
    suspend fun sendEmailConfirmationCode(sentEmailVerificationReq: SentEmailVerificationReq) =
        api.sendEmailConfirmation(sentEmailVerificationReq)

    /**
     * POST: Verify email through confirmation code that,
     * has been sent to email
     */
    suspend fun verifyEmail(verifyEmailReq: VerifyEmailReq) =
        api.verifyEmail(verifyEmailReq)

    /**
     * POST: Request for creating account
     */
    suspend fun signupWithEmailAndPassword(signupReq: SignupReq) =
        api.signup(signupReq)

    /**
     * POST: Request for log in existing account
     */
    suspend fun signinWithUsernameOrEmailAndPassword(signinReq: SigninReq) =
        api.signin(signinReq)

    /**
     * DELETE: Logging out the user
     */
    suspend fun loggedTheUserOut(token: String, logoutReq: LogoutReq) =
        api.logoutTheUser("Bearer $token", logoutReq)

    /**
     * GET: Get current user (online user)
     */
    suspend fun getCurrentUser(token: String) =
        api.getCurrentUser("Bearer $token")


    //#################### LOCAL DATABASE REQUESTS ########################
    /**
     * GET: Get access token
     */
    suspend fun getAccessToken() = connectDao.getToken()

    /**
     * GET: Get refresh token
     */
    suspend fun getRefreshToken() = connectDao.getRefreshToken()

    /**
     * Clear the database before save user because,
     * I want only one user should be stored in room db
     */
    suspend fun clearDatabase() = connectDao.clear()

    /**
     * Save authenticated user to room database
     */
    suspend fun saveUserInRoomDatabase(user: User) =
        connectDao.insertUser(user)

    /**
     * Get user if exists in room db
     */
    suspend fun getUser() = connectDao.getUser()
}