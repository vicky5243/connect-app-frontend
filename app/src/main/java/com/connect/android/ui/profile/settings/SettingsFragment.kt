package com.connect.android.ui.profile.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.connect.android.R
import com.connect.android.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val settingsViewModel by viewModels<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)

        // Setting up the viewModel
        viewModelSetup()

        // Toolbar setup
        mToolbarSetup()

        // Observing live data
        observingData()

        return binding.root
    }

    private fun observingData() {
        /**
         * Navigate back (Profile fragment)
         */
        navigateToProfileFragment()

        /**
         * Navigate ChangePassword fragment
         */
        navigateToChangePasswordFragment()

        /**
         * Navigate Theme fragment
         */
        navigateToThemeFragment()

        /**
         * Navigate Logout fragment
         */
        navigateToLogoutFragment()
    }

    private fun navigateToLogoutFragment() {
        settingsViewModel.navigateLogoutFragmentBoolean.observe(viewLifecycleOwner) {
            if (it) {
                val action = SettingsFragmentDirections.actionGlobalCustomDialog(
                    title = getString(R.string.log_out_of_connect),
                    message = null,
                    isLogoutAction = true
                )
                findNavController().navigate(action)
                settingsViewModel.doneNavigateToLogout()
            }
        }
    }

    private fun navigateToChangePasswordFragment() {
        settingsViewModel.navigateChangePasswordFragmentBoolean.observe(viewLifecycleOwner) {
            if (it) {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToChangePasswordFragment()
                findNavController().navigate(action)
                settingsViewModel.doneNavigateToChangePassword()
            }
        }
    }

    private fun navigateToThemeFragment() {
        settingsViewModel.navigateThemeFragmentBoolean.observe(viewLifecycleOwner) {
            if (it) {
                val action = SettingsFragmentDirections.actionSettingsFragmentToThemeFragment()
                findNavController().navigate(action)
                settingsViewModel.doneNavigateToTheme()
            }
        }
    }

    private fun navigateToProfileFragment() {
        settingsViewModel.navigateProfileFragmentBoolean.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                settingsViewModel.doneNavigateToProfile()
            }
        }
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = settingsViewModel
    }

    private fun mToolbarSetup() {
        binding.settingsToolbar.setNavigationIcon(R.drawable.ic_back)

        binding.settingsToolbar.title = getString(R.string.settings)

        binding.settingsToolbar.setNavigationOnClickListener {
            settingsViewModel.navigateToProfile()
        }
    }
}