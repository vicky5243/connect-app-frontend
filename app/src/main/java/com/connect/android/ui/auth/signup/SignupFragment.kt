package com.connect.android.ui.auth.signup

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.connect.android.R
import com.connect.android.databinding.FragmentSignupBinding
import com.connect.android.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private val signupViewModel by viewModels<SignupViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)

        // Setting up the viewModel
        viewModelSetup()

        // Observing live data
        observingData()

        // Handling click events
        clickEvents()

        // Keyboard listeners
        androidKeyListenerEvents()

        return binding.root
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = signupViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun observingData() {
        /**
         * Observing has email sent or not
         */
        hasEmailVerificationCodeSent()

        /**
         * If email has sent successfully show the toast
         */
        showSuccessToast()

        /**
         * Observing has sign up btn enabled or not
         * if button is disabled set alpha = 0.1f
         * else set alpha = 1f
         */
        isSignUpBtnEnabled()

        /**
         * Observing has sign up btn clicked or not
         * if sign up btn clicked then disable all fields and set alpha = 0.7f
         * else enable all fields and set alpha = 1f
         */
        isSignUpBtnClicked()
    }

    private fun showSuccessToast() {
        signupViewModel.showEmailSentSuccessToast.observe(viewLifecycleOwner) {
            if (it) {
                // Navigate to verification email ui
                val action =
                    SignupFragmentDirections.actionSignupFragmentToEmailVerificationFragment(
                        signupViewModel.email.value!!
                    )
                findNavController().navigate(action)

                signupViewModel.showToastComplete()
            }
        }
    }

    private fun clickEvents() {
        /**
         * Opening the log in fragment
         */
        binding.signupContainerLl1.setOnClickListener {
            if (signupViewModel.isSignupBtnNextClicked.value == false) {
                findNavController().popBackStack()
            }
        }
    }

    private fun androidKeyListenerEvents() {
        /**
         * When user submit the form from keyboard enter pressed
         * and button is enabled then, submit the form
         */
        binding.signupEmailEt.setOnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                if (signupViewModel.isSignupNextBtnEnabled.value == true) {
                    signupViewModel.sendEmailVerificationCode()
                }
            }
            false
        }
    }

    private fun hasEmailVerificationCodeSent() {
        signupViewModel.hasEmailSent.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    // Show loading
                    showProgressIndicator()
                }
                is Resource.Success -> {
                    hideProgressIndicator()
                    signupViewModel.showToast()
                }
                is Resource.Failure -> {
                    // Show error message
                    hideProgressIndicator()
                    it.message?.let { errorMessage ->
                        val action =
                            SignupFragmentDirections.actionGlobalCustomDialog(
                                getString(R.string.errorDialogTitle),
                                errorMessage
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun isSignUpBtnEnabled() {
        signupViewModel.isSignupNextBtnEnabled.observe(viewLifecycleOwner) {
            if (it) {
                binding.signupNextBtn.alpha = 1f
            } else {
                binding.signupNextBtn.alpha = 0.1f
            }
        }
    }

    private fun isSignUpBtnClicked() {
        signupViewModel.isSignupBtnNextClicked.observe(viewLifecycleOwner) { btnClicked ->
            if (btnClicked) {
                binding.signupEmailEt.isEnabled = false
                binding.signupEmailEt.alpha = 0.7f
            } else {
                binding.signupEmailEt.isEnabled = true
                binding.signupEmailEt.alpha = 1f
            }
        }
    }

    private fun hideProgressIndicator() {
        binding.signupPd.visibility = View.GONE
        binding.signupNextBtn.visibility = View.VISIBLE
    }

    private fun showProgressIndicator() {
        binding.signupPd.visibility = View.VISIBLE
        binding.signupNextBtn.visibility = View.GONE
    }
}