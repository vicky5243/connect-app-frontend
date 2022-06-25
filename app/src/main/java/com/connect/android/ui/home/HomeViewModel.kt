package com.connect.android.ui.home

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.connect.android.models.req.NewTokensReq
import com.connect.android.models.res.SuccessBooleanRes
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
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    application: Application
) : AndroidViewModel(application) {

    val newsfeedPostsData =
        profileRepository.getNewsFeedPosts(authRepository, null)
            .cachedIn(viewModelScope)

    private var _noPosts = MutableLiveData<Boolean>()
    val noPosts: LiveData<Boolean> get() = _noPosts

    private var _hasLiked = MutableLiveData<Resource<SuccessBooleanRes>>()
    val hasLiked: LiveData<Resource<SuccessBooleanRes>> get() = _hasLiked

    private var _noInternet = MutableLiveData<Boolean>()
    val noInternet: LiveData<Boolean> get() = _noInternet

    private var _unknownError = MutableLiveData<Boolean>()
    val unknownError: LiveData<Boolean> get() = _unknownError

    private var _navigateToSearchFragment = MutableLiveData<Boolean>()
    val navigateToSearchFragment: LiveData<Boolean> get() = _navigateToSearchFragment

    private var _navigateToLikeFragment = MutableLiveData<Pair<Long, Long>?>()
    val navigateToLikeFragment: LiveData<Pair<Long, Long>?> get() = _navigateToLikeFragment

    private var _navigateToCommentFragment = MutableLiveData<Pair<Long, Long>?>()
    val navigateToCommentFragment: LiveData<Pair<Long, Long>?> get() = _navigateToCommentFragment

    private val _navigateToProfileFragment = MutableLiveData<Long?>()
    val navigateToProfileFragment: LiveData<Long?> get() = _navigateToProfileFragment

    private val _showLikeErrorToast = MutableLiveData<String?>()
    val showLikeErrorToast: LiveData<String?> get() = _showLikeErrorToast

    init {
        _noPosts.value = false
        _noInternet.value = false
        _unknownError.value = false
        _navigateToSearchFragment.value = false
        _navigateToLikeFragment.value = null
        _showLikeErrorToast.value = null
        _navigateToProfileFragment.value = null
        _navigateToCommentFragment.value = null
    }

    fun showErrorToast(message: String) {
        _showLikeErrorToast.value = message
    }

    fun showErrorToastDone() {
        _showLikeErrorToast.value = null
    }

    fun onProfilePhotoClicked(id: Long) {
        _navigateToProfileFragment.value = id
    }

    fun onProfilePhotoClickedDone() {
        _navigateToProfileFragment.value = null
    }

    fun showSearchFragment() {
        _navigateToSearchFragment.value = true
    }

    fun showSearchFragmentDone() {
        _navigateToSearchFragment.value = false
    }

    fun navigateToLike(pid: Long, numLikes: Long) {
        _navigateToLikeFragment.value = Pair(pid, numLikes)
    }

    fun navigateToLikeDone() {
        _navigateToLikeFragment.value = null
    }

    fun navigateToComment(pid: Long, numComments: Long) {
        _navigateToCommentFragment.value = Pair(pid, numComments)
    }

    fun navigateToCommentDone() {
        _navigateToCommentFragment.value = null
    }

    fun showNoPostsMsg() {
        _noPosts.value = true
    }

    fun showNoPostsMsgDone() {
        _noPosts.value = false
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

    fun likeOrUnlikeAPost(pid: Long) {
        Timber.d("likeOrUnlikeAPost clicked.")

        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                val accessToken = authRepository.getAccessToken()
                if (accessToken != null) {
                    try {
                        val response =
                            profileRepository.likeOrUnlikePost(
                                accessToken,
                                pid
                            )
                        Timber.d("response: $response")
                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                Timber.d("body: $body")
                                _hasLiked.postValue(Resource.Success(body))
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
                                                profileRepository.likeOrUnlikePost(
                                                    _token,
                                                    pid
                                                )
                                            if (res.isSuccessful) {
                                                res.body()?.let { body ->
                                                    _hasLiked.postValue(Resource.Success(body))
                                                }
                                            } else {
                                                val errRes =
                                                    RetrofitError.convertErrorBody(response.errorBody())
                                                errRes?.let { _errResMsg ->
                                                    _hasLiked.postValue(
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
                                        _hasLiked.postValue(Resource.Failure("Something went wrong. Please try again."))
                                    }
                                } else {
                                    _hasLiked.postValue((Resource.Failure(it.error.message)))
                                }
                            }
                        }
                    } catch (throwable: Throwable) {
                        when (throwable) {
                            is IOException -> _hasLiked.postValue(Resource.Failure("An unknown network error has occurred."))
                            else -> _hasLiked.postValue(Resource.Failure("Something went wrong. Please try again."))
                        }
                    }
                } else {
                    // There is no access token
                    //Todo could be delete the user data from room
                    _hasLiked.postValue(Resource.Failure("Something went wrong. Please try again."))
                }
            } else {
                // Not internet connection
                _hasLiked.postValue(Resource.Failure("No Internet Connection."))
            }
        }
    }
}