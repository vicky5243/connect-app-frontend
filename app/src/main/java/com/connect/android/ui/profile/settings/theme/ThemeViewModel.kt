package com.connect.android.ui.profile.settings.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connect.android.database.UserDatastore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(private val userDatastore: UserDatastore) : ViewModel() {
    // Navigate back to Settings Fragment
    private var _navigateSettingsFragmentBoolean = MutableLiveData<Boolean>()
    val navigateSettingsFragmentBoolean: LiveData<Boolean> get() = _navigateSettingsFragmentBoolean

    init {
        _navigateSettingsFragmentBoolean.value = false
    }

    fun navigateToSettings() {
        _navigateSettingsFragmentBoolean.value = true
    }

    fun doneNavigateToSettings() {
        _navigateSettingsFragmentBoolean.value = false
    }

    fun saveThemeModeToDatastore(themeMode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            userDatastore.updateTheme(themeMode)
        }
    }
}