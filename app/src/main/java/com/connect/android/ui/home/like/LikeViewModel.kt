package com.connect.android.ui.home.like

import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.connect.android.repositories.AuthRepository
import com.connect.android.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LikeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    state: SavedStateHandle,
) : ViewModel() {

    private val pid = state.get<Long>("pid")

    val likesData =
        pid?.let {
            profileRepository.getLikesOfAPost(authRepository, it)
                .cachedIn(viewModelScope)
        }

    private var _noLikes = MutableLiveData<Boolean>()
    val noLikes: LiveData<Boolean> get() = _noLikes

    private var _noInternet = MutableLiveData<Boolean>()
    val noInternet: LiveData<Boolean> get() = _noInternet

    private var _unknownError = MutableLiveData<Boolean>()
    val unknownError: LiveData<Boolean> get() = _unknownError

    private val _navigateToProfileFragment = MutableLiveData<Long?>()
    val navigateToProfileFragment: LiveData<Long?> get() = _navigateToProfileFragment

    private val _navigateToHomeBack = MutableLiveData<Boolean>()
    val navigateToHomeBack: LiveData<Boolean> get() = _navigateToHomeBack

    init {
        _noLikes.value = false
        _noInternet.value = false
        _unknownError.value = false
        _navigateToProfileFragment.value = null
        _navigateToHomeBack.value = false
    }

    fun navigateToProfile(id: Long) {
        _navigateToProfileFragment.value = id
    }

    fun navigateToProfileDone() {
        _navigateToProfileFragment.value = null
    }

    fun showNoUsersMsg() {
        _noLikes.value = true
    }

    fun showNoUsersMsgDone() {
        _noLikes.value = false
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
}