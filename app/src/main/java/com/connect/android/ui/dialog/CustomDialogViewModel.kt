package com.connect.android.ui.dialog

import androidx.lifecycle.*
import com.connect.android.database.UserDatastore
import com.connect.android.models.req.LogoutReq
import com.connect.android.repositories.AuthRepository
import com.connect.android.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CustomDialogViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val userDatastore: UserDatastore
) : ViewModel() {

    private var _loggedOut = MutableLiveData<Boolean>()
    val loggedOut: LiveData<Boolean> get() = _loggedOut

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        _loggedOut.value = false
        _loading.value = false
    }

    fun logOutTheUser() {
        // Loading...
        _loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val accessToken = authRepository.getAccessToken()
            val refreshToken = authRepository.getRefreshToken()
            if (accessToken != null && refreshToken != null) {
                try {
                    authRepository.loggedTheUserOut(
                        accessToken,
                        LogoutReq(refreshToken)
                    )
                    userDatastore.deleteToken()
                } catch (throwable: Throwable) {
                }
            }
            _loading.postValue(false)
            _loggedOut.postValue(true)
            // Delete the entry from room db
            Timber.d("Deleting...")
            profileRepository.logoutUser()
            Timber.d("Deleted")
        }
    }
}