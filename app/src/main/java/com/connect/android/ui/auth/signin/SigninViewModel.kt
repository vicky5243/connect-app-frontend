package com.connect.android.ui.auth.signin

import android.app.Application
import androidx.lifecycle.*
import com.connect.android.database.UserDatastore
import com.connect.android.models.req.SigninReq
import com.connect.android.models.res.AuthRes
import com.connect.android.repositories.AuthRepository
import com.connect.android.utils.NetworkConnectivity
import com.connect.android.utils.Resource
import com.connect.android.utils.RetrofitError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SigninViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    application: Application,
    private val userDatastore: UserDatastore
) : AndroidViewModel(application) {

    private var _navigateToSignUpFragment = MutableLiveData<Boolean>()
    val navigateToSignUpFragment: LiveData<Boolean>
        get() = _navigateToSignUpFragment

    private var _isSignedInUser: MutableLiveData<Resource<AuthRes>?> = MutableLiveData()
    val isSignedInUser: LiveData<Resource<AuthRes>?>
        get() = _isSignedInUser

    var username: MutableLiveData<String?> = MutableLiveData()
    var password: MutableLiveData<String?> = MutableLiveData()

    val isSignInBtnEnabled = Transformations.switchMap(username) { _username ->
        if (_username.isNullOrEmpty()) {
            MutableLiveData(false)
        } else {
            Transformations.switchMap(password) { _password ->
                if (_password.isNullOrEmpty()) {
                    MutableLiveData(false)
                } else {
                    MutableLiveData(true)
                }
            }
        }
    }

    private var _isSignInBtnClicked = MutableLiveData<Boolean>()
    val isSignInBtnClicked: LiveData<Boolean>
        get() = _isSignInBtnClicked

    init {
        username.value = null
        password.value = null
        _isSignInBtnClicked.value = false
        _navigateToSignUpFragment.value = false
    }

    fun showErrorCompleted() {
        _isSignedInUser.value = null
        _isSignInBtnClicked.value = false
    }

    fun navigateToSignUpFragment() {
        _navigateToSignUpFragment.value = true
    }

    fun doneNavigateToSignupFragment() {
        _navigateToSignUpFragment.value = false
    }

    private fun fieldsNotEmpty() =
        !username.value.isNullOrEmpty() && !password.value.isNullOrEmpty()

    fun signInExistsAccount() {
        Timber.d("Sign in btn clicked.")
        _isSignInBtnClicked.value = true
        if (!fieldsNotEmpty()) {
            // Some or all fields are missing
            return
        }

        _isSignedInUser.postValue(Resource.Loading())
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                try {
                    val response = authRepository.signinWithUsernameOrEmailAndPassword(
                        SigninReq(
                            username.value!!,
                            password.value!!
                        )
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { body ->
                            // Clear the any old records saved in room db
                            authRepository.clearDatabase()
                            // Save user to room database
                            val user = body.data.user
                            user.refreshToken = body.data.refreshToken
                            user.accessToken = body.data.accessToken
                            authRepository.saveUserInRoomDatabase(user)
                            // save the token in user datastore
                            userDatastore.updateToken(body.data.accessToken)
                            _isSignedInUser.postValue(Resource.Success(body))

                        }
                    } else {
                        val errorRes = RetrofitError.convertErrorBody(response.errorBody())
                        errorRes?.let {
                            _isSignedInUser.postValue(Resource.Failure(it.error.message))
                        }
                    }
                } catch (throwable: Throwable) {
                    when (throwable) {
                        is IOException -> _isSignedInUser.postValue(Resource.Failure("An unknown network error has occurred. ${throwable.message}"))
                        else -> _isSignedInUser.postValue(Resource.Failure("Something went wrong. Please try again. ${throwable.message}"))
                    }
                }
            } else {
                // Not internet connection
                _isSignedInUser.postValue(Resource.Failure("No Internet Connection."))
            }
        }
    }
}