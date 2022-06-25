package com.connect.android.ui.profile.createpost

import android.app.Application
import androidx.lifecycle.*
import com.connect.android.models.req.NewTokensReq
import com.connect.android.models.res.SuccessRes
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
class CreatePostViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    application: Application
) : AndroidViewModel(application) {

    private var _navigateBackToPreviousDest = MutableLiveData<Boolean>()
    val navigateBackToPreviousDest: LiveData<Boolean> get() = _navigateBackToPreviousDest

    private var _openGallery = MutableLiveData<Boolean>()
    val openGallery: LiveData<Boolean> get() = _openGallery

    private var _hasPostCreated: MutableLiveData<Resource<SuccessRes>> = MutableLiveData()
    val hasPostCreated: LiveData<Resource<SuccessRes>> get() = _hasPostCreated

    var title: MutableLiveData<String> = MutableLiveData()
    var desc: MutableLiveData<String> = MutableLiveData()
    private var imageFile: MutableLiveData<File?> = MutableLiveData()

    val hasPostBtnEnabled = Transformations.switchMap(imageFile) { imageUri ->
        if (imageUri == null) {
            MutableLiveData(false)
        } else {
            MutableLiveData(true)
        }
    }

    private var _hasPostBtnClicked = MutableLiveData<Boolean>()
    val hasPostBtnClicked: LiveData<Boolean> get() = _hasPostBtnClicked

    private var _showCreatePostSuccessToast = MutableLiveData<String?>()
    val showCreatePostSuccessToast: LiveData<String?> get() = _showCreatePostSuccessToast

    init {
        _navigateBackToPreviousDest.value = false
        _openGallery.value = false
        title.value = ""
        desc.value = ""
        _showCreatePostSuccessToast.value = null
        imageFile.value = null
        _hasPostBtnClicked.value = false
    }

    fun showToast(message: String) {
        title.value = ""
        desc.value = ""
        imageFile.value = null
        _showCreatePostSuccessToast.value = message
    }

    fun showToastDone() {
        _showCreatePostSuccessToast.value = null
    }

    fun navigateToGallery() {
        _openGallery.value = true
    }

    fun navigateToGalleryDone() {
        _openGallery.value = false
    }

    fun navigateToPreviousDest() {
        _navigateBackToPreviousDest.value = true
    }

    fun doneNavigateToPreviousDest() {
        _navigateBackToPreviousDest.value = false
    }

    fun saveImageFile(file: File) {
        imageFile.value = file
    }

    fun createPost() {
        Timber.d("createPost called!")
        _hasPostBtnClicked.value = true
        if (imageFile.value == null) {
            // Some or all fields are missing
            return
        }

        _hasPostCreated.postValue(Resource.Loading())
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                val accessToken = authRepository.getAccessToken()
                if (accessToken != null) {
                    try {
                        val titleBody =
                            title.value!!.toRequestBody("multipart/form-data".toMediaType())
                        val descBody =
                            desc.value!!.toRequestBody("multipart/form-data".toMediaType())

                        val requestFile = imageFile.value!!.asRequestBody("image/${imageFile.value!!.extension}".toMediaTypeOrNull())
                        val fileBody =
                            MultipartBody.Part.createFormData("file", imageFile.value!!.name, requestFile)

                        val response =
                            profileRepository.createPost(accessToken, titleBody, descBody, fileBody)

                        if (response.isSuccessful) {
                            Timber.d("create post res success: $response")
                            response.body()?.let { body ->
                                _hasPostCreated.postValue(Resource.Success(body))
                            }
                        } else {
                            Timber.d("create post res not success: $response")
                            val errorRes = RetrofitError.convertErrorBody(response.errorBody())
                            errorRes?.let {
                                if (it.error.message == "Token Expired" && it.error.code == 401) {
                                    Timber.d("Create post token expired")
                                    val refreshToken = authRepository.getRefreshToken()
                                    if (refreshToken != null) {
                                        val newToken =
                                            authRepository.getNewTokens(NewTokensReq(refreshToken))
                                        newToken?.let { _token ->
                                            val res = profileRepository.createPost(_token, titleBody, descBody, fileBody)
                                            if (res.isSuccessful) {
                                                res.body()?.let { body ->
                                                    _hasPostCreated.postValue(
                                                        Resource.Success(body)
                                                    )
                                                }
                                            } else {
                                                val errRes =
                                                    RetrofitError.convertErrorBody(response.errorBody())
                                                errRes?.let { _errResMsg ->
                                                    _hasPostCreated.postValue(
                                                        Resource.Failure(
                                                            _errResMsg.error.message
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // There is no refresh token
                                        _hasPostCreated.postValue(Resource.Failure("Something went wrong. Please try again."))
                                    }
                                } else {
                                    _hasPostCreated.postValue(Resource.Failure(it.error.message))
                                }
                            }
                        }
                    } catch (throwable: Throwable) {
                        Timber.d("catch: ${throwable.message}")
                        when (throwable) {
                            is IOException -> _hasPostCreated.postValue(Resource.Failure("An unknown network error has occurred."))
                            else -> _hasPostCreated.postValue(Resource.Failure("Something went wrong. Please try again."))
                        }
                    }
                } else {
                    // There is no access token
                    _hasPostCreated.postValue(Resource.Failure("Something went wrong. Please try again."))
                }
            } else {
                // Not internet connection
                _hasPostCreated.postValue(Resource.Failure("No Internet Connection."))
            }
        }
    }
}