package com.connect.android.ui.profile.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(): ViewModel() {
    // Navigate back to Profile Fragment
    private var _navigateProfileFragmentBoolean = MutableLiveData<Boolean>()
    val navigateProfileFragmentBoolean: LiveData<Boolean> get() = _navigateProfileFragmentBoolean

    // Navigate to ChangePassword Fragment
    private var _navigateChangePasswordFragmentBoolean = MutableLiveData<Boolean>()
    val navigateChangePasswordFragmentBoolean: LiveData<Boolean> get() = _navigateChangePasswordFragmentBoolean

    // Navigate to Theme Fragment
    private var _navigateThemeFragmentBoolean = MutableLiveData<Boolean>()
    val navigateThemeFragmentBoolean: LiveData<Boolean> get() = _navigateThemeFragmentBoolean

    // Navigate to Logout Fragment
    private var _navigateLogoutFragmentBoolean = MutableLiveData<Boolean>()
    val navigateLogoutFragmentBoolean: LiveData<Boolean> get() = _navigateLogoutFragmentBoolean


    init {
        _navigateProfileFragmentBoolean.value = false
        _navigateChangePasswordFragmentBoolean.value = false
        _navigateThemeFragmentBoolean.value = false
        _navigateLogoutFragmentBoolean.value = false
    }

    fun navigateToProfile() {
        _navigateProfileFragmentBoolean.value = true
    }

    fun doneNavigateToProfile() {
        _navigateProfileFragmentBoolean.value = false
    }

    fun navigateToChangePassword() {
        _navigateChangePasswordFragmentBoolean.value = true
    }

    fun doneNavigateToChangePassword() {
        _navigateChangePasswordFragmentBoolean.value = false
    }

    fun navigateToTheme() {
        _navigateThemeFragmentBoolean.value = true
    }

    fun doneNavigateToTheme() {
        _navigateThemeFragmentBoolean.value = false
    }

    fun navigateToLogout() {
        _navigateLogoutFragmentBoolean.value = true
    }

    fun doneNavigateToLogout() {
        _navigateLogoutFragmentBoolean.value = false
    }
}