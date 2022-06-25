package com.connect.android.ui.auth.username

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.connect.android.R
import com.connect.android.database.UserDatastore
import com.connect.android.databinding.FragmentUsernameBinding
import com.connect.android.models.res.User
import com.connect.android.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsernameFragment : Fragment() {

    private lateinit var binding: FragmentUsernameBinding
    private lateinit var userDatastore: UserDatastore
    private val usernameViewModel by viewModels<UsernameViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_username,
            container,
            false
        )

        // It will store main memory
        // Next time, when we access token it will quickly give me the result and it won't cause an ANR error
        userDatastore = UserDatastore(requireContext())
        lifecycleScope.launch {
            userDatastore.id.first()
            userDatastore.email.first()
            userDatastore.code.first()
        }

        // Setting up the viewModel
        viewModelSetup()

        // Observing live data
        observingData()

        // Keyboard listeners
        androidKeyListenerEvents()

        return binding.root
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = usernameViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun observingData() {
        /**
         * Observing that user account has been created or not
         */
        userAccountCreated()

        /**
         * Observing that signup btn enabled or not
         * if button is disabled set alpha = 0.1f
         * else set alpha = 1f
         */
        isSignUpBtnEnabled()

        /**
         * Observing that signup btn clicked or not
         * if sign up btn clicked then disable all fields and set alpha = 0.7f
         * else enable all fields and set alpha = 1f
         */
        isSignUpBtnClicked()
    }

    private fun userAccountCreated() {
        usernameViewModel.hasAccountCreated.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    // Show loading
                    showProgressIndicator()
                }
                is Resource.Success -> {
                    it.data?.data?.user?.let { user ->
                        hideProgressIndicator()
                        sendUserToWelcomePage(user)
                    }
                }
                is Resource.Failure -> {
                    // Show error message
                    hideProgressIndicator()
                    it.message?.let { errorMessage ->
                        val action =
                            UsernameFragmentDirections.actionGlobalCustomDialog(
                                getString(R.string.errorDialogTitle),
                                errorMessage
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun sendUserToWelcomePage(user: User) {
        val action =
            UsernameFragmentDirections.actionUsernameFragmentToHomeFragment2(user)
        findNavController().navigate(action)
    }

    private fun isSignUpBtnClicked() {
        usernameViewModel.isSignUpBtnClicked.observe(viewLifecycleOwner) { btnClicked ->
            if (btnClicked) {
                binding.usernameEt.isEnabled = false
                binding.usernameEt.alpha = 0.7f
                binding.usernamePasswordEt.isEnabled = false
                binding.usernamePasswordEt.alpha = 0.7f
            } else {
                binding.usernameEt.isEnabled = true
                binding.usernameEt.alpha = 1f
                binding.usernamePasswordEt.isEnabled = true
                binding.usernamePasswordEt.alpha = 1f
            }
        }
    }

    private fun isSignUpBtnEnabled() {
        usernameViewModel.isSignUpBtnEnabled.observe(viewLifecycleOwner) {
            if (it) {
                binding.usernameSignupBtn.alpha = 1f
            } else {
                binding.usernameSignupBtn.alpha = 0.1f
            }
        }
    }

    private fun androidKeyListenerEvents() {
        /**
         * When user submit the form from keyboard enter pressed
         * and button is enabled then, submit the form
         */
        binding.usernamePasswordEt.setOnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                if (usernameViewModel.isSignUpBtnEnabled.value == true) {
                    usernameViewModel.createTheUserAccount()
                }
            }
            false
        }
    }

    private fun hideProgressIndicator() {
        binding.usernamePd.visibility = View.GONE
        binding.usernameSignupBtn.visibility = View.VISIBLE
    }

    private fun showProgressIndicator() {
        binding.usernamePd.visibility = View.VISIBLE
        binding.usernameSignupBtn.visibility = View.GONE
    }
}