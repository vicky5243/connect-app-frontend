package com.connect.android.ui.profile.settings.changepassword

import android.app.Application
import androidx.lifecycle.*
import com.connect.android.models.req.ChangePasswordReq
import com.connect.android.models.req.NewTokensReq
import com.connect.android.repositories.AuthRepository
import com.connect.android.repositories.ProfileRepository
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
class ChangePasswordViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    application: Application
) : AndroidViewModel(application) {

    private var _isPasswordChanged: MutableLiveData<Resource<String>?> = MutableLiveData()
    val isPasswordChanged: LiveData<Resource<String>?> get() = _isPasswordChanged

    var oldPassword: MutableLiveData<String?> = MutableLiveData()
    var newPassword: MutableLiveData<String?> = MutableLiveData()
    var confirmNewPassword: MutableLiveData<String?> = MutableLiveData()

    val isCpBtnEnabled = Transformations.switchMap(oldPassword) { _oldPassword ->
        if (_oldPassword.isNullOrEmpty()) {
            MutableLiveData(false)
        } else {
            Transformations.switchMap(newPassword) { _newPassword ->
                if (_newPassword.isNullOrEmpty()) {
                    MutableLiveData(false)
                } else {
                    Transformations.switchMap(confirmNewPassword) { _confirmNewPassword ->
                        if (_confirmNewPassword.isNullOrEmpty()) {
                            MutableLiveData(false)
                        } else {
                            MutableLiveData(true)
                        }
                    }
                }
            }
        }
    }

    private var _isCpBtnClicked = MutableLiveData<Boolean>()
    val isCpBtnClicked: LiveData<Boolean> get() = _isCpBtnClicked

    // Navigate back to Settings Fragment
    private var _navigateSettingsFragmentBoolean = MutableLiveData<Boolean>()
    val navigateSettingsFragmentBoolean: LiveData<Boolean> get() = _navigateSettingsFragmentBoolean

    init {
        _navigateSettingsFragmentBoolean.value = false
        oldPassword.value = null
        newPassword.value = null
        confirmNewPassword.value = null
        _isCpBtnClicked.value = false
    }

    fun clearEditTextValues() {
        oldPassword.value = null
        newPassword.value = null
        confirmNewPassword.value = null
    }

    fun doneShowSuccessMessage() {
        _isPasswordChanged.value = null
    }

    fun showErrorCompleted() {
        _isPasswordChanged.value = null
        _isCpBtnClicked.value = false
    }

    private fun fieldsNotEmpty() =
        !oldPassword.value.isNullOrEmpty() && !newPassword.value.isNullOrEmpty() && !confirmNewPassword.value.isNullOrEmpty()

    fun changePassword() {
        Timber.d("Change password btn clicked.")
        _isCpBtnClicked.value = true
        if (!fieldsNotEmpty()) {
            // Some or all fields are missing
            return
        }

        // New password and confirm new password should match
        if (newPassword.value != confirmNewPassword.value) {
            _isPasswordChanged.value =
                Resource.Failure("Your new password and confirm password do not match. Please try again.")
            return
        }

        // Loading...
        _isPasswordChanged.postValue(Resource.Loading())

        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                val accessToken = authRepository.getAccessToken()
                if (accessToken != null) {
                    try {
                        val response = profileRepository.changePassword(
                            accessToken,
                            ChangePasswordReq(
                                oldPassword.value!!,
                                newPassword.value!!,
                                confirmNewPassword.value!!
                            )
                        )
                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                _isPasswordChanged.postValue(Resource.Success(body.message))
                            }
                        } else {
                            val errorRes = RetrofitError.convertErrorBody(response.errorBody())
                            errorRes?.let {
                                if (it.error.message == "Token Expired" && it.error.code == 401) {
                                    val refreshToken = authRepository.getRefreshToken()
                                    if (refreshToken != null) {
                                        val newToken =
                                            authRepository.getNewTokens(NewTokensReq(refreshToken))
                                        newToken?.let { _token ->
                                            val res = profileRepository.changePassword(
                                                _token,
                                                ChangePasswordReq(
                                                    oldPassword.value!!,
                                                    newPassword.value!!,
                                                    confirmNewPassword.value!!
                                                )
                                            )
                                            if (res.isSuccessful) {
                                                res.body()?.let { body ->
                                                    _isPasswordChanged.postValue(
                                                        Resource.Success(
                                                            body.message
                                                        )
                                                    )
                                                }
                                            } else {
                                                val errRes =
                                                    RetrofitError.convertErrorBody(response.errorBody())
                                                errRes?.let { _errResMsg ->
                                                    _isPasswordChanged.postValue(
                                                        Resource.Failure(
                                                            _errResMsg.error.message
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // There is no refresh token
                                        _isPasswordChanged.postValue(Resource.Failure("Something went wrong. Please try again."))
                                    }
                                } else {
                                    _isPasswordChanged.postValue(Resource.Failure(it.error.message))
                                }
                            }
                        }
                    } catch (throwable: Throwable) {
                        when (throwable) {
                            is IOException -> _isPasswordChanged.postValue(Resource.Failure("An unknown network error has occurred."))
                            else -> _isPasswordChanged.postValue(Resource.Failure("Something went wrong. Please try again. ${throwable.message}"))
                        }
                    }
                } else {
                    // There is no access token
                    _isPasswordChanged.postValue(Resource.Failure("Something went wrong. Please try again."))
                }
            } else {
                // Not internet connection
                _isPasswordChanged.postValue(Resource.Failure("No Internet Connection."))
            }
        }
    }

    fun navigateToSettings() {
        _navigateSettingsFragmentBoolean.value = true
    }

    fun doneNavigateToSettings() {
        _navigateSettingsFragmentBoolean.value = false
    }
}