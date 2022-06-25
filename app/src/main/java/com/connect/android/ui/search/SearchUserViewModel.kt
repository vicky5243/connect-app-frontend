package com.connect.android.ui.search

import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.connect.android.repositories.AuthRepository
import com.connect.android.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchUserViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val DEFAULT_QUERY = ""
    }

    var currentQuery = MutableLiveData(DEFAULT_QUERY)

    val usersData = currentQuery.switchMap { queryString ->
        profileRepository.searchUser(authRepository, queryString).cachedIn(viewModelScope)
    }

    fun performSearchUsers(query: String) {
        Timber.d("performSearchUsers $query")
        currentQuery.value = query
    }

    private var _noUsers = MutableLiveData<Boolean>()
    val noUsers: LiveData<Boolean> get() = _noUsers

    private var _noInternet = MutableLiveData<Boolean>()
    val noInternet: LiveData<Boolean> get() = _noInternet

    private var _unknownError = MutableLiveData<Boolean>()
    val unknownError: LiveData<Boolean> get() = _unknownError

    private val _navigateToProfileFragment = MutableLiveData<Long?>()
    val navigateToProfileFragment: LiveData<Long?> get() = _navigateToProfileFragment

    private val _navigateToHomeBack = MutableLiveData<Boolean>()
    val navigateToHomeBack: LiveData<Boolean> get() = _navigateToHomeBack

    init {
        _noUsers.value = false
        _noInternet.value = false
        _unknownError.value = false
        _navigateToProfileFragment.value = null
        _navigateToHomeBack.value = false
    }

    fun onSearchUserClicked(id: Long) {
        _navigateToProfileFragment.value = id
    }

    fun onSearchUserNavigated() {
        _navigateToProfileFragment.value = null
    }

    fun showNoUsersMsg() {
        _noUsers.value = true
    }

    fun showNoUsersMsgDone() {
        _noUsers.value = false
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