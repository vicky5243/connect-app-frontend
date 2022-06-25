package com.connect.android.ui.auth.signin

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
import com.connect.android.databinding.FragmentSigninBinding
import com.connect.android.models.res.User
import com.connect.android.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SigninFragment : Fragment() {

    private lateinit var binding: FragmentSigninBinding
    private val signinViewModel by viewModels<SigninViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signin, container, false)

        // Setting up the viewModel
        viewModelSetup()

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
        signinViewModel.username.value = null
        signinViewModel.password.value = null
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = signinViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun observingData() {
        /**
         * Observing has user successfully signed in or not
         * handling user api request states
         * LOADING : boolean
         * SUCCESS : User data class
         * FAILURE : error message
         */
        isUserSignedIn()

        /**
         * Observing has sign in btn enabled or not
         * if button is disabled set alpha = 0.1f
         * else set alpha = 1f
         */
        isSignInBtnEnabled()

        /**
         * Observing has sign in btn clicked or not
         * if sign in btn clicked then disable all fields and set alpha = 0.7f
         * else enable all fields and set alpha = 1f
         */
        isSignInBtnClicked()


        /**
         * Observing has clicked to navigate to signup fragment or not
         * Opening the sign up fragment
         */
        hasClickedToNavigate()
    }

    private fun sendUserToWelcomePage(user: User) {
        val action = SigninFragmentDirections.actionSigninFragmentToHomeFragment2(user)
        findNavController().navigate(action)
    }

    private fun androidKeyListenerEvents() {
        /**
         * When user submit the form from keyboard enter pressed
         * and button is enabled then, submit the form
         */
        binding.signinPasswordEt.setOnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                if (signinViewModel.isSignInBtnEnabled.value == true) {
                    signinViewModel.signInExistsAccount()
                }
            }
            false
        }
    }

    private fun isUserSignedIn() {
        signinViewModel.isSignedInUser.observe(viewLifecycleOwner) {
            Timber.d("isSignedInUser: $it")
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
                    it.data?.data?.user?.let { user ->
                        sendUserToWelcomePage(user)
                    }
                }
                else -> {
                    // Show error message
                    Timber.d("Failure... ${it?.message}")
                    hideProgressIndicator()
                    it?.message?.let { errorMessage ->
                        val action =
                            SigninFragmentDirections.actionGlobalCustomDialog(
                                getString(R.string.errorDialogTitle),
                                errorMessage
                            )
                        findNavController().navigate(action)
                        signinViewModel.showErrorCompleted()
                    }
                }
            }
        }
    }

    private fun isSignInBtnEnabled() {
        signinViewModel.isSignInBtnEnabled.observe(viewLifecycleOwner) {
            if (it) {
                binding.signinBtn.alpha = 1f
            } else {
                binding.signinBtn.alpha = 0.1f
            }
        }
    }

    private fun isSignInBtnClicked() {
        signinViewModel.isSignInBtnClicked.observe(viewLifecycleOwner) { btnClicked ->
            if (btnClicked) {
                binding.signinUsernameEmailEt.isEnabled = false
                binding.signinPasswordEt.isEnabled = false
                binding.signinUsernameEmailEt.alpha = 0.7f
                binding.signinPasswordEt.alpha = 0.7f
            } else {
                binding.signinUsernameEmailEt.isEnabled = true
                binding.signinPasswordEt.isEnabled = true
                binding.signinUsernameEmailEt.alpha = 1f
                binding.signinPasswordEt.alpha = 1f
            }
        }
    }

    private fun hasClickedToNavigate() {
        signinViewModel.navigateToSignUpFragment.observe(viewLifecycleOwner) {
            if (it) {
                if (signinViewModel.isSignInBtnClicked.value == false) {
                    //Todo App crashing when I'm tapping so many times
                    val action = SigninFragmentDirections.actionSigninFragmentToSignupFragment()
                    findNavController().navigate(action)
                    signinViewModel.doneNavigateToSignupFragment()
                }
            }
        }
    }

    private fun hideProgressIndicator() {
        binding.signinPd.visibility = View.GONE
        binding.signinBtn.visibility = View.VISIBLE
    }

    private fun showProgressIndicator() {
        binding.signinPd.visibility = View.VISIBLE
        binding.signinBtn.visibility = View.GONE
    }
}