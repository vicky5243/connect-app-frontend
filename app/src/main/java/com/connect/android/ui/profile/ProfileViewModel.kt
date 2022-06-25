package com.connect.android.ui.profile

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.connect.android.models.req.NewTokensReq
import com.connect.android.models.res.GetUser
import com.connect.android.models.res.SuccessRes
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
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    state: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    private val visitedUserId = state.get<Long>("visitedUserId")

    val getPostsData =
        profileRepository.getNewsFeedPosts(authRepository, visitedUserId)
            .cachedIn(viewModelScope)

    private var _onPostsItemClickedPos = MutableLiveData<Int?>()
    val onPostItemClickedPos: LiveData<Int?> get() = _onPostsItemClickedPos

    private var _noPosts = MutableLiveData<Boolean>()
    val noPosts: LiveData<Boolean> get() = _noPosts

    private var _navigateCreatePostFragment = MutableLiveData<Boolean>()
    val navigateCreatePostFragment: LiveData<Boolean> get() = _navigateCreatePostFragment

    private var _navigateSettingsFragmentBoolean = MutableLiveData<Boolean>()
    val navigateSettingsFragmentBoolean: LiveData<Boolean> get() = _navigateSettingsFragmentBoolean

    private var _navigateEditProfileFragment = MutableLiveData<Boolean>()
    val navigateEditProfileFragment: LiveData<Boolean> get() = _navigateEditProfileFragment

    private var _navigatePostFragment = MutableLiveData<Long?>()
    val navigatePostFragment: LiveData<Long?> get() = _navigatePostFragment

    private var _hasFollow = MutableLiveData<Resource<SuccessRes>>()
    val hasFollow: LiveData<Resource<SuccessRes>> get() = _hasFollow

    private var _navigateBackToPreviousDest = MutableLiveData<Boolean>()
    val navigateBackToPreviousDest: LiveData<Boolean> get() = _navigateBackToPreviousDest

    private var _visitedProfileUser: MutableLiveData<Resource<GetUser>> = MutableLiveData()
    val visitedProfileUser: LiveData<Resource<GetUser>> get() = _visitedProfileUser

    init {
        Timber.d("ProfileViewModel init called...")
        _navigateSettingsFragmentBoolean.value = false
        _noPosts.value = false
        _navigateCreatePostFragment.value = false
        _navigateBackToPreviousDest.value = false
        _navigateEditProfileFragment.value = false
        _navigatePostFragment.value = null
        _onPostsItemClickedPos.value = null
        _visitedProfileUser.value = Resource.Loading()
        getVisitedProfileUserDetails(false)
    }

    fun onPostItemClicked(itemPosition: Int) {
        _onPostsItemClickedPos.value = itemPosition
    }

    fun onPostItemClickedDone() {
        _onPostsItemClickedPos.value = null
    }

    fun showNoPostsMsg() {
        _noPosts.value = true
    }

    fun showNoPostsMsgDone() {
        _noPosts.value = false
    }

    fun navigateToPost(uid: Long) {
        _navigatePostFragment.value = uid
    }

    fun navigateToPostDone() {
        _navigatePostFragment.value = null
    }

    fun navigateToCreatePost() {
        _navigateCreatePostFragment.value = true
    }

    fun doneNavigateToCreatePost() {
        _navigateCreatePostFragment.value = false
    }

    fun navigateToSettings() {
        _navigateSettingsFragmentBoolean.value = true
    }

    fun doneNavigateToSettings() {
        _navigateSettingsFragmentBoolean.value = false
    }

    fun navigateToEditProfile() {
        _navigateEditProfileFragment.value = true
    }

    fun doneNavigateToEditProfile() {
        _navigateEditProfileFragment.value = false
    }

    fun navigateToPreviousDest() {
        _navigateBackToPreviousDest.value = true
    }

    fun doneNavigateToPreviousDest() {
        _navigateBackToPreviousDest.value = false
    }

    fun getVisitedProfileUserDetails(hasRetryBtnClicked: Boolean) {
        // If we already fetched the user details
        // then, there is not need to make network request for same user
        if (visitedProfileUser.value?.data != null) return
        // Making network request to fetch user details
        Timber.d("Network request, getVisitedProfileUserDetails called...")
        // Retry btn clicked to fetch details again
        if (hasRetryBtnClicked)
            _visitedProfileUser.value = Resource.Loading()

        // Intern connectivity check
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                val accessToken = authRepository.getAccessToken()
                if (accessToken != null) {
                    try {
                        val response = profileRepository.getUserById(
                            accessToken,
                            visitedUserId!!
                        )
                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                _visitedProfileUser.postValue(Resource.Success(body.data.user))
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
                                            val res = profileRepository.getUserById(
                                                _token,
                                                visitedUserId
                                            )
                                            if (res.isSuccessful) {
                                                res.body()?.let { body ->
                                                    _visitedProfileUser.postValue(
                                                        Resource.Success(
                                                            body.data.user
                                                        )
                                                    )
                                                }
                                            } else {
                                                val errRes =
                                                    RetrofitError.convertErrorBody(response.errorBody())
                                                errRes?.let { _errResMsg ->
                                                    _visitedProfileUser.postValue(
                                                        Resource.Failure(
                                                            _errResMsg.error.message
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // There is no refresh token
                                        _visitedProfileUser.postValue(Resource.Failure("Something went wrong. Please try again."))
                                    }
                                } else {
                                    _visitedProfileUser.postValue(Resource.Failure(it.error.message))
                                }
                            }
                        }
                    } catch (throwable: Throwable) {
                        when (throwable) {
                            is IOException -> _visitedProfileUser.postValue(Resource.Failure("An unknown network error has occurred."))
                            else -> _visitedProfileUser.postValue(Resource.Failure("Something went wrong. Please try again. ${throwable.message}"))
                        }
                    }
                } else {
                    // There is no access token
                    _visitedProfileUser.postValue(Resource.Failure("Something went wrong. Please try again."))
                }
            } else {
                // Not internet connection
                _visitedProfileUser.postValue(Resource.Failure("No Internet Connection."))
            }
        }
    }

    fun followTheUser() {
        _hasFollow.postValue(Resource.Loading())
        // Intern connectivity check
        val hasInternetConnection = NetworkConnectivity.hasInternetConnection(getApplication())
        viewModelScope.launch(Dispatchers.IO) {
            if (hasInternetConnection) {
                // Internet connection has available
                val accessToken = authRepository.getAccessToken()
                if (accessToken != null) {
                    try {
                        val response = profileRepository.followAnUser(
                            accessToken,
                            visitedUserId!!
                        )
                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                _hasFollow.postValue(Resource.Success(body))
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
                                            val res = profileRepository.followAnUser(
                                                _token,
                                                visitedUserId
                                            )
                                            if (res.isSuccessful) {
                                                res.body()?.let { body ->
                                                    _hasFollow.postValue(
                                                        Resource.Success(
                                                            body
                                                        )
                                                    )
                                                }
                                            } else {
                                                val errRes =
                                                    RetrofitError.convertErrorBody(response.errorBody())
                                                errRes?.let { _errResMsg ->
                                                    _hasFollow.postValue(
                                                        Resource.Failure(
                                                            _errResMsg.error.message
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // There is no refresh token
                                        _hasFollow.postValue(Resource.Failure("Something went wrong. Please try again."))
                                    }
                                } else {
                                    _hasFollow.postValue(Resource.Failure(it.error.message))
                                }
                            }
                        }
                    } catch (throwable: Throwable) {
                        when (throwable) {
                            is IOException -> _hasFollow.postValue(Resource.Failure("An unknown network error has occurred."))
                            else -> _hasFollow.postValue(Resource.Failure("Something went wrong. Please try again. ${throwable.message}"))
                        }
                    }
                } else {
                    // There is no access token
                    _hasFollow.postValue(Resource.Failure("Something went wrong. Please try again."))
                }
            } else {
                // Not internet connection
                _hasFollow.postValue(Resource.Failure("No Internet Connection."))
            }
        }
    }
}