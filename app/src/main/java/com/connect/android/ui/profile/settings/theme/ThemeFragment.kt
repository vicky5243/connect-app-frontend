package com.connect.android.ui.profile.settings.theme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.connect.android.R
import com.connect.android.database.UserDatastore
import com.connect.android.databinding.FragmentThemeBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ThemeFragment : Fragment() {

    private lateinit var binding: FragmentThemeBinding
    private lateinit var userDatastore: UserDatastore

    private val themeViewModel by viewModels<ThemeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_theme, container, false)

        // Toolbar setup
        mToolbarSetup()

        // Reading user preferences datastore to set theme mode
        setThemeModeFromUserDatastore()

        // Observing live data
        observingData()

        // Click events
        clickEvents()

        return binding.root
    }

    private fun clickEvents() {
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                R.id.theme_light_rb -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.theme_dark_rb -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            Timber.d("themeMode: $themeMode")
            themeViewModel.saveThemeModeToDatastore(themeMode)
            AppCompatDelegate.setDefaultNightMode(themeMode)
        }
    }

    private fun setThemeModeFromUserDatastore() {
        userDatastore = UserDatastore(requireContext())
        userDatastore.theme.asLiveData().observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    AppCompatDelegate.MODE_NIGHT_NO -> {
                        // Light theme mode
                        binding.themeRadioGroup.check(R.id.theme_light_rb)
                    }
                    AppCompatDelegate.MODE_NIGHT_YES -> {
                        // Dark theme mode
                        binding.themeRadioGroup.check(R.id.theme_dark_rb)
                    }
                    else -> {
                        // System default theme mode
                        binding.themeRadioGroup.check(R.id.theme_default_rb)
                    }
                }
            }
        }
    }

    private fun observingData() {
        /**
         * Navigate back (Settings fragment)
         */
        navigateToSettingsFragment()
    }

    private fun navigateToSettingsFragment() {
        themeViewModel.navigateSettingsFragmentBoolean.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                themeViewModel.doneNavigateToSettings()
            }
        }
    }

    private fun mToolbarSetup() {
        binding.themeToolbar.setNavigationIcon(R.drawable.ic_back)

        binding.themeToolbar.title = getString(R.string.set_theme)

        binding.themeToolbar.setNavigationOnClickListener {
            themeViewModel.navigateToSettings()
        }
    }
}