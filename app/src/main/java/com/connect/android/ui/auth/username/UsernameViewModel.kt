package com.connect.android.ui.auth.username

import android.app.Application
import androidx.lifecycle.*
import com.connect.android.database.UserDatastore
import com.connect.android.models.req.SignupReq
import com.connect.android.models.res.AuthRes
import com.connect.android.repositories.AuthRepository
import com.connect.android.utils.NetworkConnectivity
import com.connect.android.utils.Resource
import com.connect.android.utils.RetrofitError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    application: Application,
    private val userDatastore: UserDatastore
) : AndroidViewModel(application) {
    private var _hasAccountCreated = MutableLiveData<Resource<AuthRes>>()
    val hasAccountCreated: LiveData<Resource<AuthRes>> get() = _hasAccountCreated

    var username: MutableLiveData<String?> = MutableLiveData()
    var password: MutableLiveData<String?> = MutableLiveData()

    val isSignUpBtnEnabled = Transformations.switchMap(username) { _username ->
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

    private var _isSignUpBtnClicked = MutableLiveData<Boolean>()
    val isSignUpBtnClicked: LiveData<Boolean> get() = _isSignUpBtnClicked

    init {
        username.value = null
        password.value = null
        _isSignUpBtnClicked.value = false
    }

    fun createTheUserAccount() {
        Timber.d("createTheUserAccount")
        _isSignUpBtnClicked.value = true
        if (username.value.isNullOrEmpty() || password.value.isNullOrEmpty()) {
            // Username and Password fields are empty
            return
        }

        _hasAccountCreated.postValue(Resource.Loading())
        // Get id, email, code first
        val id = runBlocking { userDatastore.id.first() }
        val email = runBlocking { userDatastore.email.first() }
        val code = runBlocking { userDatastore.code.first() }
        if (id == null || email == null || code == null) return
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                try {
                    val response =
                        authRepository.signupWithEmailAndPassword(
                            SignupReq(
                                username.value!!,
                                email,
                                password.value!!,
                                code,
                                id
                            )
                        )
                    Timber.d("response $response")
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
                            // deleting userdata store all keys related to verified email
                            userDatastore.deleteId()
                            userDatastore.deleteEmail()
                            userDatastore.deleteCode()
                            userDatastore.deleteIsVerified()
                            Timber.d("body $body")
                            _hasAccountCreated.postValue(Resource.Success(body))
                        }
                    } else {
                        val errorRes = RetrofitError.convertErrorBody(response.errorBody())
                        errorRes?.let {
                            _hasAccountCreated.postValue(Resource.Failure(it.error.message))
                        }
                    }
                } catch (throwable: Throwable) {
                    when (throwable) {
                        is IOException -> _hasAccountCreated.postValue(Resource.Failure("An unknown network error has occurred."))
                        else -> _hasAccountCreated.postValue(Resource.Failure("Something went wrong. Please try again."))
                    }
                } finally {
                    _isSignUpBtnClicked.postValue(false)
                }
            } else {
                // Not internet connection
                _hasAccountCreated.postValue(Resource.Failure("No Internet Connection."))
                _isSignUpBtnClicked.postValue(false)
            }
        }
    }
}