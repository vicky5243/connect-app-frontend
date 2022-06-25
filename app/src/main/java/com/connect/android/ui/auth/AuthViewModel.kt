package com.connect.android.ui.auth

import android.app.Application
import androidx.lifecycle.*
import com.connect.android.database.UserDatastore
import com.connect.android.models.req.NewTokensReq
import com.connect.android.models.res.User
import com.connect.android.repositories.AuthRepository
import com.connect.android.utils.NetworkConnectivity
import com.connect.android.utils.RetrofitError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    application: Application,
    private val userDatastore: UserDatastore
) : AndroidViewModel(application) {

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _navigateToUsernameFragment: MutableLiveData<Boolean> = MutableLiveData()
    val navigateToUsernameFragment: LiveData<Boolean> get() = _navigateToUsernameFragment

    private val _navigateToWelcomePage: MutableLiveData<User?> = MutableLiveData()
    val navigateToWelcomePage: LiveData<User?> get() = _navigateToWelcomePage

    init {
        _navigateToUsernameFragment.value = false
        _navigateToWelcomePage.value = null
        isAuthenticated()
    }

    fun navigateToUsernameFragmentDone() {
        _navigateToUsernameFragment.value = false
    }

    private fun isAuthenticated() {
        val isVerified = runBlocking { userDatastore.isVerified.first() }
        Timber.d("isAuthenticated: $isVerified")
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            // Get stored user from room db
            val user = authRepository.getUser()
            // User has already signed up or signed
            // then, navigate to WelcomeActivity
            if (user?.refreshToken != null) {
                if (hasInternetConnection) {
                    // Internet connection has available
                    val accessToken = authRepository.getAccessToken()
                    if (accessToken != null) {
                        try {
                            val response = authRepository.getCurrentUser(accessToken)
                            if (response.isSuccessful) {
                                response.body()?.let { body ->
                                    // Update user to room database
                                    val fetchedUser = body.data.user
                                    fetchedUser.refreshToken = user.refreshToken
                                    fetchedUser.accessToken = accessToken
                                    authRepository.saveUserInRoomDatabase(fetchedUser)
                                    // update the token in user datastore
                                    userDatastore.updateToken(accessToken)
                                    _navigateToWelcomePage.postValue(body.data.user)
                                }
                            } else {
                                val errorRes = RetrofitError.convertErrorBody(response.errorBody())
                                errorRes?.let {
                                    if (it.error.message == "Token Expired" && it.error.code == 401) {
                                        val refreshToken = authRepository.getRefreshToken()
                                        if (refreshToken != null) {
                                            val newToken =
                                                authRepository.getNewTokens(
                                                    NewTokensReq(
                                                        refreshToken
                                                    )
                                                )
                                            newToken?.let { _token ->
                                                val res = authRepository.getCurrentUser(_token)
                                                if (res.isSuccessful) {
                                                    res.body()?.let { body ->
                                                        // Update user to room database
                                                        val newRefreshTokenSQLite = authRepository.getRefreshToken()
                                                        val fetchedUser = body.data.user
                                                        fetchedUser.refreshToken = newRefreshTokenSQLite
                                                        fetchedUser.accessToken = _token
                                                        authRepository.saveUserInRoomDatabase(
                                                            fetchedUser
                                                        )
                                                        // update the token in user datastore
                                                        userDatastore.updateToken(_token)
                                                        _navigateToWelcomePage.postValue(body.data.user)
                                                        _navigateToWelcomePage.postValue(body.data.user)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (throwable: Throwable) {
                            Timber.d(throwable.message)
                            _navigateToWelcomePage.postValue(user)
                        }
                    }
                } else {
                    // No Internet Connection
                    _navigateToWelcomePage.postValue(user)
                }


            } else {
                // There is no user stored in room db
                Timber.d("Not User: $user")
                /**
                 * Check has user already tried to signed up account and email has been also verified
                 * and, left in the middle of account creation
                 */
                if (isVerified == true) {
                    _navigateToUsernameFragment.postValue(true)
                }
            }
            _isLoading.postValue(false)
        }
    }
}