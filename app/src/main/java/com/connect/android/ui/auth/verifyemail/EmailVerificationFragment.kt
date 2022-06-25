package com.connect.android.ui.auth.verifyemail

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
import com.connect.android.databinding.FragmentEmailVerificationBinding
import com.connect.android.ui.auth.signup.SignupFragmentDirections
import com.connect.android.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailVerificationFragment : Fragment() {

    private lateinit var binding: FragmentEmailVerificationBinding
    private lateinit var emailVerificationFragmentArgs: EmailVerificationFragmentArgs
    private val emailVerifyViewModel by viewModels<EmailVerifyViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_email_verification,
            container,
            false
        )

        // Get args using by navArgs property delegate
        getArgs()

        // Setting up the viewModel
        viewModelSetup()

        // Observing live data
        observingData()

        // Keyboard listeners
        androidKeyListenerEvents()

        return binding.root
    }

    private fun getArgs() {
        emailVerificationFragmentArgs = EmailVerificationFragmentArgs.fromBundle(requireArguments())
    }

    private fun observingData() {
        /**
         * Observing has email resent or not
         */
        hasEmailVerificationCodeResent()

        /**
         * If email has sent successfully show the toast
         */
        showSuccessToast()

        /**
         * Observing has email has been verified or not
         */
        hasEmailVerified()

        /**
         * email has verified navigate to username fragment
         */
        navigateToUserFragment()

        /**
         * Observing has verify btn enabled or not
         * if button is disabled set alpha = 0.1f
         * else set alpha = 1f
         */
        isVerifyEmailBtnEnabled()

        /**
         * Observing has verify btn clicked or not
         * if sign up btn clicked then disable all fields and set alpha = 0.7f
         * else enable all fields and set alpha = 1f
         */
        isVerifyEmailBtnClicked()
    }

    private fun showSuccessToast() {
        emailVerifyViewModel.showEmailSentSuccessToast.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    "Email has been sent successfully, Please check your mail",
                    Toast.LENGTH_LONG
                ).show()

                emailVerifyViewModel.showToastComplete()
            }
        }
    }

    private fun hasEmailVerificationCodeResent() {
        emailVerifyViewModel.hasEmailResent.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    // Show loading
                    showProgressIndicator()
                }
                is Resource.Success -> {
                    hideProgressIndicator()
                    emailVerifyViewModel.showToast()
                }
                is Resource.Failure -> {
                    // Show error message
                    hideProgressIndicator()
                    it.message?.let { errorMessage ->
                        val action =
                            EmailVerificationFragmentDirections.actionGlobalCustomDialog(
                                getString(R.string.errorDialogTitle),
                                errorMessage
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun navigateToUserFragment() {
        emailVerifyViewModel.navigateToUsernameFragment.observe(viewLifecycleOwner) {
            if (it) {
                val action =
                    EmailVerificationFragmentDirections.actionEmailVerificationFragmentToUsernameFragment()
                findNavController().navigate(action)

                emailVerifyViewModel.navigateToFragmentDone()
            }
        }
    }

    private fun hasEmailVerified() {
        emailVerifyViewModel.hasVerifiedEmail.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    // Show loading
                    showProgressIndicator()
                }
                is Resource.Success -> {
                    it.data?.let { _ ->
                        hideProgressIndicator()
                        emailVerifyViewModel.navigateToFragment()
                    }
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

    private fun isVerifyEmailBtnEnabled() {
        emailVerifyViewModel.isVerifyEmailNextBtnEnabled.observe(viewLifecycleOwner) {
            if (it) {
                binding.emailVerifyBtn.alpha = 1f
            } else {
                binding.emailVerifyBtn.alpha = 0.1f
            }
        }
    }

    private fun isVerifyEmailBtnClicked() {
        emailVerifyViewModel.isVerifyEmailNextBtnClicked.observe(viewLifecycleOwner) { btnClicked ->
            if (btnClicked) {
                binding.emailVerifyCodeEt.isEnabled = false
                binding.emailVerifyCodeEt.alpha = 0.7f
            } else {
                binding.emailVerifyCodeEt.isEnabled = true
                binding.emailVerifyCodeEt.alpha = 1f
            }
        }
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = emailVerifyViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun androidKeyListenerEvents() {
        /**
         * When user submit the form from keyboard enter pressed
         * and button is enabled then, submit the form
         */
        binding.emailVerifyCodeEt.setOnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                if (emailVerifyViewModel.isVerifyEmailNextBtnEnabled.value == true) {
                    emailVerifyViewModel.verifyEmail()
                }
            }
            false
        }
    }

    private fun hideProgressIndicator() {
        binding.emailVerifyPd.visibility = View.GONE
        binding.emailVerifyBtn.visibility = View.VISIBLE
    }

    private fun showProgressIndicator() {
        binding.emailVerifyPd.visibility = View.VISIBLE
        binding.emailVerifyBtn.visibility = View.GONE
    }
}