package com.connect.android.ui.auth.verifyemail

import android.app.Application
import androidx.lifecycle.*
import com.connect.android.database.UserDatastore
import com.connect.android.models.req.SentEmailVerificationReq
import com.connect.android.models.req.VerifyEmailReq
import com.connect.android.models.res.SuccessRes
import com.connect.android.models.res.VerifyEmailRes
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
class EmailVerifyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    application: Application,
    state: SavedStateHandle,
    private val userDatastore: UserDatastore
) : AndroidViewModel(application) {

    private val enteredEmail = state.get<String>("email")

    private var _hasEmailResent = MutableLiveData<Resource<SuccessRes>>()
    val hasEmailResent: LiveData<Resource<SuccessRes>> get() = _hasEmailResent

    private var _showEmailSentSuccessToast = MutableLiveData<Boolean>()
    val showEmailSentSuccessToast: LiveData<Boolean> get() = _showEmailSentSuccessToast

    private var _hasVerifiedEmail = MutableLiveData<Resource<VerifyEmailRes>>()
    val hasVerifiedEmail: LiveData<Resource<VerifyEmailRes>> get() = _hasVerifiedEmail

    var code: MutableLiveData<String?> = MutableLiveData()
    var email: MutableLiveData<String> = MutableLiveData()

    private var _navigateToUsernameFragment = MutableLiveData<Boolean>()
    val navigateToUsernameFragment: LiveData<Boolean> get() = _navigateToUsernameFragment

    val isVerifyEmailNextBtnEnabled = Transformations.switchMap(code) { _code ->
        if (_code.isNullOrEmpty()) {
            MutableLiveData(false)
        } else {
            MutableLiveData(true)
        }
    }

    private var _isVerifyEmailNextBtnClicked = MutableLiveData<Boolean>()
    val isVerifyEmailNextBtnClicked: LiveData<Boolean> get() = _isVerifyEmailNextBtnClicked

    init {
        code.value = null
        _showEmailSentSuccessToast.value = false
        _isVerifyEmailNextBtnClicked.value = false
        _navigateToUsernameFragment.value = false
    }

    fun showToast() {
        _showEmailSentSuccessToast.value = true
    }

    fun showToastComplete() {
        _showEmailSentSuccessToast.value = false
    }

    fun navigateToFragment() {
        _navigateToUsernameFragment.value = true
    }

    fun navigateToFragmentDone() {
        _navigateToUsernameFragment.value = false
    }

    fun verifyEmail() {
        Timber.d("verifyEmail called")
        _isVerifyEmailNextBtnClicked.value = true
        if (code.value.isNullOrEmpty() || enteredEmail.isNullOrEmpty()) {
            // Code field and provided email is empty
            return
        }

        _hasVerifiedEmail.postValue(Resource.Loading())
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                try {
                    val response = authRepository.verifyEmail(
                        VerifyEmailReq(
                            enteredEmail,
                            code.value!!
                        )
                    )
                    Timber.d("response $response")
                    if (response.isSuccessful) {
                        response.body()?.let { body ->
                            // Save to preferences data store id, email, code, isVerified
                            userDatastore.updateId(body.data.id)
                            userDatastore.updateEmail(body.data.email)
                            userDatastore.updateCode(body.data.code)
                            userDatastore.updateIsVerified(body.data.isVerified)
                            Timber.d("body $body")
                            _hasVerifiedEmail.postValue(Resource.Success(body))
                        }
                    } else {
                        val errorRes = RetrofitError.convertErrorBody(response.errorBody())
                        errorRes?.let {
                            _hasVerifiedEmail.postValue(Resource.Failure(it.error.message))
                        }
                    }
                } catch (throwable: Throwable) {
                    when (throwable) {
                        is IOException -> _hasVerifiedEmail.postValue(Resource.Failure("An unknown network error has occurred."))
                        else -> _hasVerifiedEmail.postValue(Resource.Failure("Something went wrong. Please try again."))
                    }
                } finally {
                    _isVerifyEmailNextBtnClicked.postValue(false)
                }
            } else {
                // Not internet connection
                _hasVerifiedEmail.postValue(Resource.Failure("No Internet Connection."))
                _isVerifyEmailNextBtnClicked.postValue(false)
            }
        }
    }

    fun resendConfirmationCode() {
        Timber.d("Resending confirmation code..")
        if (email.value.isNullOrEmpty()) {
            Timber.d("Filed is empty")
            // Email field is empty
            return
        }

        _hasEmailResent.postValue(Resource.Loading())
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                try {
                    val response =
                        authRepository.sendEmailConfirmationCode(SentEmailVerificationReq(email.value!!))
                    Timber.d("response: $response")
                    if (response.isSuccessful) {
                        response.body()?.let { body ->
                            Timber.d("body: $body")
                            _hasEmailResent.postValue(Resource.Success(body))
                        }
                    } else {
                        val errorRes = RetrofitError.convertErrorBody(response.errorBody())
                        errorRes?.let {
                            _hasEmailResent.postValue(Resource.Failure(it.error.message))
                        }
                    }
                } catch (throwable: Throwable) {
                    when (throwable) {
                        is IOException -> _hasEmailResent.postValue(Resource.Failure("An unknown network error has occurred."))
                        else -> _hasEmailResent.postValue(Resource.Failure("Something went wrong. Please try again."))
                    }
                }
            } else {
                // Not internet connection
                _hasEmailResent.postValue(Resource.Failure("No Internet Connection."))
            }
        }
    }
}