package com.connect.android.ui.profile.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.connect.android.models.req.NewTokensReq
import com.connect.android.models.res.SuccessRes
import com.connect.android.models.res.User
import com.connect.android.repositories.AuthRepository
import com.connect.android.repositories.ProfileRepository
import com.connect.android.utils.NetworkConnectivity
import com.connect.android.utils.Resource
import com.connect.android.utils.RetrofitError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    application: Application
) : AndroidViewModel(application) {

    private var _navigateBackToPreviousDest = MutableLiveData<Boolean>()
    val navigateBackToPreviousDest: LiveData<Boolean> get() = _navigateBackToPreviousDest

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> get() = _currentUser

    private val _profileUpdated = MutableLiveData<Resource<SuccessRes>>()
    val profileUpdated: LiveData<Resource<SuccessRes>> get() = _profileUpdated

    private var _openGallery = MutableLiveData<Boolean>()
    val openGallery: LiveData<Boolean> get() = _openGallery

    var fullname: MutableLiveData<String?> = MutableLiveData()
    var username: MutableLiveData<String?> = MutableLiveData()
    var relationshipStatus: MutableLiveData<String?> = MutableLiveData()
    private var imageFile: MutableLiveData<File?> = MutableLiveData()

    init {
        fullname.value = null
        username.value = null
        relationshipStatus.value = null
        _openGallery.value = false
        _navigateBackToPreviousDest.value = false
        imageFile.value = null
        // Loading...
        _currentUser.postValue(Resource.Loading())
        getCurrentUser(false)
    }

    fun saveImageFile(file: File) {
        imageFile.value = file
    }

    fun navigateToGallery() {
        _openGallery.value = true
    }

    fun navigateToGalleryDone() {
        _openGallery.value = false
    }

    fun getCurrentUser(hasRetryBtnClicked: Boolean) {
        // Retry btn clicked to fetch details again
        if (hasRetryBtnClicked) _currentUser.value = Resource.Loading()

        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                val accessToken = authRepository.getAccessToken()
                if (accessToken != null) {
                    try {
                        val response = authRepository.getCurrentUser(accessToken)
                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                _currentUser.postValue(Resource.Success(body.data.user))
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
                                            val res = authRepository.getCurrentUser(_token)
                                            if (res.isSuccessful) {
                                                res.body()?.let { body ->
                                                    _currentUser.postValue(
                                                        Resource.Success(body.data.user)
                                                    )
                                                }
                                            } else {
                                                val errRes =
                                                    RetrofitError.convertErrorBody(response.errorBody())
                                                errRes?.let { _errResMsg ->
                                                    _currentUser.postValue(
                                                        Resource.Failure(
                                                            _errResMsg.error.message
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // There is no refresh token
                                        _currentUser.postValue(Resource.Failure("Something went wrong. Please try again."))
                                    }
                                } else {
                                    _currentUser.postValue(Resource.Failure(it.error.message))
                                }
                            }
                        }
                    } catch (throwable: Throwable) {
                        when (throwable) {
                            is IOException -> _currentUser.postValue(Resource.Failure("An unknown network error has occurred."))
                            else -> _currentUser.postValue(Resource.Failure("Something went wrong. Please try again. ${throwable.message}"))
                        }
                    }
                } else {
                    // There is no access token
                    _currentUser.postValue(Resource.Failure("Something went wrong. Please try again."))
                }
            } else {
                // Not internet connection
                _currentUser.postValue(Resource.Failure("No Internet Connection."))
            }
        }
    }

    fun navigateToPreviousDest() {
        _navigateBackToPreviousDest.value = true
    }

    fun doneNavigateToPreviousDest() {
        _navigateBackToPreviousDest.value = false
    }

    fun saveEditProfile() {
        Timber.d("savedEditProfile called!")
        _profileUpdated.value = Resource.Loading()
        if (username.value.isNullOrEmpty()) {
            _profileUpdated.value = Resource.Failure("Invalid username. Please try again.")
            return
        }
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                val accessToken = authRepository.getAccessToken()
                if (accessToken != null) {
                    try {
                        val fullnameBody =
                            fullname.value?.toRequestBody("multipart/form-data".toMediaType())
                        val relationshipStatusBody =
                            relationshipStatus.value?.toRequestBody("multipart/form-data".toMediaType())
                        val usernameBody =
                            username.value?.toRequestBody("multipart/form-data".toMediaType())

                        val requestFile = imageFile.value?.asRequestBody("image/${imageFile.value!!.extension}".toMediaTypeOrNull())

                        var fileBody: MultipartBody.Part? = null

                        if (requestFile != null) {
                            fileBody = MultipartBody.Part.createFormData("file", imageFile.value!!.name, requestFile)
                        }

                        val response = profileRepository.editProfile(
                            token = accessToken,
                            fullname = fullnameBody,
                            relationshipStatus = relationshipStatusBody,
                            username = usernameBody,
                            file = fileBody
                        )
                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                _profileUpdated.postValue(Resource.Success(body))
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
                                            val res = profileRepository.editProfile(
                                                token = _token,
                                                fullname = fullnameBody,
                                                relationshipStatus = relationshipStatusBody,
                                                username = usernameBody,
                                                file = fileBody
                                            )
                                            if (res.isSuccessful) {
                                                res.body()?.let { body ->
                                                    _profileUpdated.postValue(
                                                        Resource.Success(body)
                                                    )
                                                }
                                            } else {
                                                val errRes =
                                                    RetrofitError.convertErrorBody(response.errorBody())
                                                errRes?.let { _errResMsg ->
                                                    _profileUpdated.postValue(
                                                        Resource.Failure(
                                                            _errResMsg.error.message
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // There is no refresh token
                                        _profileUpdated.postValue(Resource.Failure("Something went wrong. Please try again."))
                                    }
                                } else {
                                    _profileUpdated.postValue(Resource.Failure(it.error.message))
                                }
                            }
                        }
                    } catch (throwable: Throwable) {
                        Timber.d("catch: ${throwable.message}")
                        when (throwable) {
                            is IOException -> _profileUpdated.postValue(Resource.Failure("An unknown network error has occurred."))
                            else -> _profileUpdated.postValue(Resource.Failure("Something went wrong. Please try again."))
                        }
                    }
                } else {
                    // There is no access token
                    _profileUpdated.postValue(Resource.Failure("Something went wrong. Please try again."))
                }
            } else {
                // Not internet connection
                _profileUpdated.postValue(Resource.Failure("No Internet Connection."))
            }
        }
    }
}