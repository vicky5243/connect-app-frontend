package com.connect.android.ui.profile.edit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.connect.android.R
import com.connect.android.databinding.FragmentEditProfileBinding
import com.connect.android.models.res.User
import com.connect.android.utils.Constants
import com.connect.android.utils.Resource
import com.connect.android.utils.getFileName
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding

    private val editProfileViewModel by viewModels<EditProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)

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
         * Observing if current user profile details has been fetched or not
         */
        fetchedVisitedProfileUser()

        /**
         * Observing to navigate back to previous destination
         */
        navigateBackToPreviousDestination()

        /**
         * Observing, has been profile updated or not
         */
        hasProfileUpdated()

        /**
         * Observing, opening the gallery to pick image
         */
        openGallery()
    }

    private fun openGallery() {
        editProfileViewModel.openGallery.observe(viewLifecycleOwner) {
            if (it) {
                pickImageFile()
                editProfileViewModel.navigateToGalleryDone()
            }
        }
    }

    private val contract = registerForActivityResult(ActivityResultContracts.GetContent()) {
        Timber.d("URI: $it")
        /**
         * Binding user profile pic
         */
        it?.let { uri ->
            val parcelFileDescriptor =
                requireContext().contentResolver.openFileDescriptor(uri, "r", null)
            val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
            val file =
                File(requireContext().cacheDir, requireContext().contentResolver.getFileName(uri))
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            editProfileViewModel.saveImageFile(file)
            Glide.with(requireContext())
                .load(uri)
                .into(binding.editProfileImageCiv)
        }
    }

    private fun pickImageFile() {
        Timber.d("pickImageFile called!")

        contract.launch("image/*")
    }

    private fun hasProfileUpdated() {
        editProfileViewModel.profileUpdated.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    Timber.d("Loading...")
                    binding.editProfileLpi.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    Timber.d("Succeed ${it.data?.message}")
                    binding.editProfileLpi.visibility = View.GONE
                    binding.editProfileToolbar.menu.findItem(R.id.save_profile).isVisible = true
                    it.data?.let { success ->
                        Toast.makeText(requireContext(), success.message, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Failure -> {
                    Timber.d("Failure ${it.message}")
                    binding.editProfileLpi.visibility = View.GONE
                    binding.editProfileToolbar.menu.findItem(R.id.save_profile).isVisible = true
                    it.message?.let { error ->
                        val action =
                            EditProfileFragmentDirections.actionGlobalCustomDialog(
                                getString(R.string.errorDialogTitle),
                                error
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun fetchedVisitedProfileUser() {
        editProfileViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            when (currentUser) {
                is Resource.Loading -> {
                    Timber.d("Loading...")
                    showProgressIndicator()
                }
                is Resource.Success -> {
                    currentUser.data?.let { user ->
                        setTheUserDetailsToViews(user)
                    }
                }
                is Resource.Failure -> {
                    currentUser.message?.let { errorMessage ->
                        showErrorDialog(errorMessage)
                    }
                }
            }
        }
    }

    private fun setTheUserDetailsToViews(user: User) {
        // Hide all views
        binding.editProfilePd.visibility = View.GONE
        binding.editProfileContainerLl.visibility = View.GONE
        // Show user details
        bindingUserDataDetails(user)
        binding.editProfileScrollView.visibility = View.VISIBLE
    }

    private fun bindingUserDataDetails(user: User) {
        /**
         * Binding user profile pic
         */
        Glide.with(requireContext())
            .load("${Constants.PROFILE_IMAGE_URL}/${user.profilePhotoUrl}")
            .into(binding.editProfileImageCiv)

        if (user.fullname != null) {
            binding.editProfileNameEt.setText(user.fullname)
        }

        if (user.username != null) {
            binding.editProfileUsernameEt.setText(user.username)
        }

        if (user.relationshipStatus != null) {
            binding.editProfileRelationshipEt.setText(user.relationshipStatus)
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        val action =
            EditProfileFragmentDirections.actionGlobalCustomDialog(
                getString(R.string.errorDialogTitle),
                errorMessage
            )
        findNavController().navigate(action)

        // Hide all views
        binding.editProfilePd.visibility = View.GONE
        binding.editProfileScrollView.visibility = View.GONE
        // Show retry btn
        binding.editProfileContainerLl.visibility = View.VISIBLE
    }

    private fun showProgressIndicator() {
        // Hide all views
        binding.editProfileScrollView.visibility = View.GONE
        binding.editProfileContainerLl.visibility = View.GONE
        // Show progress indicator
        binding.editProfilePd.visibility = View.VISIBLE
    }

    private fun navigateBackToPreviousDestination() {
        editProfileViewModel.navigateBackToPreviousDest.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                editProfileViewModel.doneNavigateToPreviousDest()
            }
        }
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = editProfileViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun mToolbarSetup() {
        /**
         * Inflating menu
         */
        binding.editProfileToolbar.inflateMenu(R.menu.edit_profile_bar_menu)

        /**
         * Menu item click events
         */
        binding.editProfileToolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.save_profile) {
                it.isVisible = false
                editProfileViewModel.saveEditProfile()
            }
            false
        }

        // Show the navigation up icon
        binding.editProfileToolbar.setNavigationIcon(R.drawable.ic_back)
        // Click event on navigation up
        binding.editProfileToolbar.setNavigationOnClickListener {
            editProfileViewModel.navigateToPreviousDest()
        }
        binding.editProfileToolbar.title = "Edit Profile"
    }
}