package com.connect.android.ui.auth.signup

import android.app.Application
import androidx.lifecycle.*
import com.connect.android.models.req.SentEmailVerificationReq
import com.connect.android.models.res.SuccessRes
import com.connect.android.repositories.AuthRepository
import com.connect.android.utils.NetworkConnectivity
import com.connect.android.utils.Resource
import com.connect.android.utils.RetrofitError
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    application: Application
) : AndroidViewModel(application) {

    private var _hasEmailSent = MutableLiveData<Resource<SuccessRes>>()
    val hasEmailSent: LiveData<Resource<SuccessRes>> get() = _hasEmailSent

    var email: MutableLiveData<String?> = MutableLiveData()

    private var _showEmailSentSuccessToast = MutableLiveData<Boolean>()
    val showEmailSentSuccessToast: LiveData<Boolean> get() = _showEmailSentSuccessToast


    val isSignupNextBtnEnabled = Transformations.switchMap(email) { _email ->
        if (_email.isNullOrEmpty()) {
            MutableLiveData(false)
        } else {
            MutableLiveData(true)
        }
    }

    private var _isSignUpNextBtnClicked = MutableLiveData<Boolean>()
    val isSignupBtnNextClicked: LiveData<Boolean> get() = _isSignUpNextBtnClicked

    init {
        email.value = null
        _isSignUpNextBtnClicked.value = false
        _showEmailSentSuccessToast.value = false
    }

    fun showToast() {
        _showEmailSentSuccessToast.value = true
    }

    fun showToastComplete() {
        _showEmailSentSuccessToast.value = false
    }

    fun sendEmailVerificationCode() {
        Timber.d("Sign up btn clicked.")
        _isSignUpNextBtnClicked.value = true
        if (email.value.isNullOrEmpty()) {
            Timber.d("Filed is empty")
            // Email field is empty
            return
        }

        _hasEmailSent.postValue(Resource.Loading())
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
                            _hasEmailSent.postValue(Resource.Success(body))
                        }
                    } else {
                        val errorRes = RetrofitError.convertErrorBody(response.errorBody())
                        errorRes?.let {
                            _hasEmailSent.postValue(Resource.Failure(it.error.message))
                        }
                    }
                } catch (throwable: Throwable) {
                    when (throwable) {
                        is IOException -> _hasEmailSent.postValue(Resource.Failure("An unknown network error has occurred."))
                        else -> _hasEmailSent.postValue(Resource.Failure("Something went wrong. Please try again."))
                    }
                } finally {
                    _isSignUpNextBtnClicked.postValue(false)
                }
            } else {
                // Not internet connection
                _hasEmailSent.postValue(Resource.Failure("No Internet Connection."))
                _isSignUpNextBtnClicked.postValue(false)
            }
        }
    }
}