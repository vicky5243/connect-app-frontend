package com.connect.android.ui.home.comment

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.connect.android.models.req.CommentReq
import com.connect.android.models.req.NewTokensReq
import com.connect.android.models.res.CommentRes
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
class CommentViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    state: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    private val pid = state.get<Long>("pid")

    val commentsData =
        pid?.let {
            profileRepository.getCommentsOfAPost(authRepository, it)
                .cachedIn(viewModelScope)
        }

    private var _onlineUserProfilePhotoUrl = MutableLiveData<String?>()
    val onlineUserProfilePhotoUrl: LiveData<String?> get() = _onlineUserProfilePhotoUrl

    private var _noComments = MutableLiveData<Boolean>()
    val noComments: LiveData<Boolean> get() = _noComments

    private var _noInternet = MutableLiveData<Boolean>()
    val noInternet: LiveData<Boolean> get() = _noInternet

    private var _unknownError = MutableLiveData<Boolean>()
    val unknownError: LiveData<Boolean> get() = _unknownError

    private val _navigateToProfileFragment = MutableLiveData<Long?>()
    val navigateToProfileFragment: LiveData<Long?> get() = _navigateToProfileFragment

    private val _navigateToHomeBack = MutableLiveData<Boolean>()
    val navigateToHomeBack: LiveData<Boolean> get() = _navigateToHomeBack

    private var _hasCommented: MutableLiveData<Resource<CommentRes>> = MutableLiveData()
    val hasCommented: LiveData<Resource<CommentRes>> get() = _hasCommented

    private var _showCommentedSuccessToast = MutableLiveData<Boolean>()
    val showCommentedSuccessToast: LiveData<Boolean> get() = _showCommentedSuccessToast

    private var _showCommentedUnSuccessToast = MutableLiveData<String?>()
    val showCommentedUnSuccessToast: LiveData<String?> get() = _showCommentedUnSuccessToast

    var commentText: MutableLiveData<String?> = MutableLiveData()

    val isCommentPostTvEnabled = Transformations.switchMap(commentText) { _commentText ->
        if (_commentText.isNullOrEmpty()) {
            MutableLiveData(false)
        } else {
            MutableLiveData(true)
        }
    }

    private var _isCommentPostTvClicked = MutableLiveData<Boolean>()
    val isCommentPostTvClicked: LiveData<Boolean> get() = _isCommentPostTvClicked

    init {
        _onlineUserProfilePhotoUrl.value = null
        getOnlineUserProfileDP()
        _noComments.value = false
        _noInternet.value = false
        _unknownError.value = false
        _navigateToProfileFragment.value = null
        _navigateToHomeBack.value = false
        commentText.value = null
        _isCommentPostTvClicked.value = false
        _showCommentedSuccessToast.value = false
        _showCommentedUnSuccessToast.value = null
    }

    private fun getOnlineUserProfileDP() {
        viewModelScope.launch(Dispatchers.IO) {
            val url = profileRepository.getOnlineUserProfilePhotoUrl()
            _onlineUserProfilePhotoUrl.postValue((url))
        }
    }

    fun showToast() {
        _showCommentedSuccessToast.value = true
    }

    fun showToastDone() {
        _showCommentedSuccessToast.value = false
    }

    fun showErrorToast(errorMsg: String) {
        _showCommentedUnSuccessToast.value = errorMsg
    }

    fun showErrorToastDone() {
        _showCommentedUnSuccessToast.value = null
    }

    fun navigateToProfile(id: Long) {
        _navigateToProfileFragment.value = id
    }

    fun navigateToProfileDone() {
        _navigateToProfileFragment.value = null
    }

    fun showNoUsersMsg() {
        _noComments.value = true
    }

    fun showNoUsersMsgDone() {
        _noComments.value = false
    }

    fun showNoInternetMsg() {
        _noInternet.value = true
    }

    fun showNoInternetMsgDone() {
        _noInternet.value = false
    }

    fun showUnknownErrorMsg() {
        _unknownError.value = true
    }

    fun showUnknownErrorMsgDone() {
        _unknownError.value = false
    }

    fun navigateToHome() {
        _navigateToHomeBack.value = true
    }

    fun navigateToHomeDone() {
        _navigateToHomeBack.value = false
    }

    fun commentOnAPost() {
        Timber.d("commentOnAPost tv clicked.")
        _isCommentPostTvClicked.value = true
        if (commentText.value.isNullOrEmpty()) {
            Timber.d("Filed is empty")
            // Email field is empty
            return
        }

        _hasCommented.postValue(Resource.Loading())
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                val accessToken = authRepository.getAccessToken()
                if (accessToken != null) {
                    try {
                        val response =
                            profileRepository.commentOnAPost(
                                accessToken,
                                CommentReq(commentText.value!!),
                                pid!!
                            )
                        Timber.d("response: $response")
                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                Timber.d("body: $body")
                                _hasCommented.postValue(Resource.Success(body))
                                // Reset the et to blank
                                commentText.postValue("")
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
                                            val res =
                                                profileRepository.commentOnAPost(
                                                    _token,
                                                    CommentReq(commentText.value!!),
                                                    pid
                                                )
                                            if (res.isSuccessful) {
                                                res.body()?.let { body ->
                                                    _hasCommented.postValue(Resource.Success(body))
                                                    // Reset the et to blank
                                                    commentText.postValue("")
                                                }
                                            } else {
                                                val errRes =
                                                    RetrofitError.convertErrorBody(response.errorBody())
                                                errRes?.let { _errResMsg ->
                                                    _hasCommented.postValue(
                                                        Resource.Failure(
                                                            _errResMsg.error.message
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // There is no refresh token
                                        //Todo could be delete the user data from room
                                        _hasCommented.postValue(Resource.Failure("Something went wrong. Please try again."))
                                    }
                                } else {
                                    _hasCommented.postValue((Resource.Failure(it.error.message)))
                                }
                            }
                        }
                    } catch (throwable: Throwable) {
                        when (throwable) {
                            is IOException -> _hasCommented.postValue(Resource.Failure("An unknown network error has occurred."))
                            else -> _hasCommented.postValue(Resource.Failure("Something went wrong. Please try again."))
                        }
                    } finally {
                        _isCommentPostTvClicked.postValue(false)
                    }
                } else {
                    // There is no access token
                    //Todo could be delete the user data from room
                    _hasCommented.postValue(Resource.Failure("Something went wrong. Please try again."))
                }
            } else {
                // Not internet connection
                _hasCommented.postValue(Resource.Failure("No Internet Connection."))
                _isCommentPostTvClicked.postValue(false)
            }
        }
    }
}