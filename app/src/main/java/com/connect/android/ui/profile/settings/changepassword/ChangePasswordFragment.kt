package com.connect.android.ui.profile.settings.changepassword

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.connect.android.R
import com.connect.android.databinding.FragmentChangePasswordBinding
import com.connect.android.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private val changePasswordViewModel by viewModels<ChangePasswordViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_change_password, container, false)

        // Setting up the viewModel
        viewModelSetup()

        // Toolbar setup
        mToolbarSetup()

        // Observing live data
        observingData()

        // Keyboard listeners
        androidKeyListenerEvents()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        /**
         * Username and Password values set to null if already values have
         */
        changePasswordViewModel.oldPassword.value = null
        changePasswordViewModel.newPassword.value = null
        changePasswordViewModel.confirmNewPassword.value = null
    }

    private fun observingData() {
        /**
         * Navigate back (Settings fragment)
         */
        navigateToSettingsFragment()

        /**
         * Observing that password has been changed successfully or not
         * handling api request states
         * LOADING : boolean
         * SUCCESS : Message data class
         * FAILURE : error message
         */
        isPasswordChangedIn()

        /**
         * Observing has change password btn enabled or not
         * if button is disabled set alpha = 0.1f
         * else set alpha = 1f
         */
        isChangePasswordBtnEnabled()

        /**
         * Observing has change password btn clicked or not
         * if change password btn clicked then disable all fields and set alpha = 0.7f
         * else enable all fields and set alpha = 1f
         */
        isChangePasswordBtnClicked()
    }

    private fun isChangePasswordBtnClicked() {
        changePasswordViewModel.isCpBtnClicked.observe(viewLifecycleOwner) { btnClicked ->
            if (btnClicked) {
                binding.cpOldPasswordEt.isEnabled = false
                binding.cpNewPasswordEt.isEnabled = false
                binding.cpConfirmPasswordEt.isEnabled = false
                binding.cpOldPasswordEt.alpha = 0.7f
                binding.cpNewPasswordEt.alpha = 0.7f
                binding.cpConfirmPasswordEt.alpha = 0.7f
            } else {
                binding.cpOldPasswordEt.isEnabled = true
                binding.cpNewPasswordEt.isEnabled = true
                binding.cpConfirmPasswordEt.isEnabled = true
                binding.cpOldPasswordEt.alpha = 1f
                binding.cpNewPasswordEt.alpha = 1f
                binding.cpConfirmPasswordEt.alpha = 1f
            }
        }
    }

    private fun isChangePasswordBtnEnabled() {
        changePasswordViewModel.isCpBtnEnabled.observe(viewLifecycleOwner) {
            if (it) {
                binding.cpBtn.alpha = 1f
            } else {
                binding.cpBtn.alpha = 0.1f
            }
        }
    }

    private fun isPasswordChangedIn() {
        changePasswordViewModel.isPasswordChanged.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    // Show loading
                    Timber.d("Loading...")
                    showProgressIndicator()
                }
                is Resource.Success -> {
                    // User is authenticated
                    // Send to welcome activity
                    Timber.d("Success... ${it.data}")
                    hideProgressIndicator()
                    Toast.makeText(requireContext(), "${it.data}", Toast.LENGTH_LONG).show()
                    changePasswordViewModel.clearEditTextValues()
                    changePasswordViewModel.doneShowSuccessMessage()
                }
                else -> {
                    // Show error message
                    Timber.d("Failure... ${it?.message}")
                    hideProgressIndicator()
                    it?.message?.let { errorMessage ->
                        val action =
                            ChangePasswordFragmentDirections.actionGlobalCustomDialog(
                                getString(R.string.errorDialogTitle),
                                errorMessage
                            )
                        findNavController().navigate(action)
                        changePasswordViewModel.showErrorCompleted()
                    }
                }
            }
        }
    }

    private fun navigateToSettingsFragment() {
        changePasswordViewModel.navigateSettingsFragmentBoolean.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                changePasswordViewModel.doneNavigateToSettings()
            }
        }
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = changePasswordViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun mToolbarSetup() {
        binding.cpToolbar.setNavigationIcon(R.drawable.ic_back)

        binding.cpToolbar.title = getString(R.string.change_password)

        binding.cpToolbar.setNavigationOnClickListener {
            changePasswordViewModel.navigateToSettings()
        }
    }

    private fun androidKeyListenerEvents() {
        /**
         * When user submit the form from keyboard enter pressed
         * and button is enabled then, submit the form
         */
        binding.cpConfirmPasswordEt.setOnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                if (changePasswordViewModel.isCpBtnEnabled.value == true) {
                    changePasswordViewModel.changePassword()
                }
            }
            false
        }
    }

    private fun hideProgressIndicator() {
        binding.cpPd.visibility = View.GONE
        binding.cpBtn.visibility = View.VISIBLE
    }

    private fun showProgressIndicator() {
        binding.cpPd.visibility = View.VISIBLE
        binding.cpBtn.visibility = View.GONE
    }
}